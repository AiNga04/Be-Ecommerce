package com.zyna.dev.ecommerce.shipping.service.impl;

import com.zyna.dev.ecommerce.common.enums.OrderStatus;
import com.zyna.dev.ecommerce.common.enums.PaymentMethod;
import com.zyna.dev.ecommerce.common.enums.PaymentStatus;
import com.zyna.dev.ecommerce.common.enums.ShipmentStatus;
import com.zyna.dev.ecommerce.common.exceptions.ApplicationException;
import com.zyna.dev.ecommerce.orders.models.Order;
import com.zyna.dev.ecommerce.orders.repository.OrderRepository;
import com.zyna.dev.ecommerce.shipping.dto.response.ShipmentInfoResponse;
import com.zyna.dev.ecommerce.shipping.models.Shipment;
import com.zyna.dev.ecommerce.shipping.repository.ShipmentRepository;
import com.zyna.dev.ecommerce.shipping.service.interfaces.ShipmentService;
import com.zyna.dev.ecommerce.users.models.User;
import com.zyna.dev.ecommerce.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    // ================= ADMIN / STAFF =================

    @Override
    @Transactional
    public ShipmentInfoResponse assignShipper(Long orderId, Long shipperId, String carrierCode) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Order not found"));

        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Đơn hàng phải ở trạng thái CONFIRMED (Đã xác nhận) trước khi gán người giao hàng");
        }

        Shipment shipment = shipmentRepository.findByOrder(order).orElse(null);
        if (shipment == null) {
            shipment = Shipment.builder()
                .order(order)
                .status(ShipmentStatus.PENDING_ASSIGN)
                .build();
        } else if (shipment.getShipper() != null) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Shipment đã được gán Shipper");
        }

        User shipper = userRepository.findById(shipperId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Shipper not found"));

        if (!isShipper(shipper)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Provided user is not a shipper");
        }

        if (shipper.getStatus() != com.zyna.dev.ecommerce.common.enums.Status.ACTIVE) {
             throw new ApplicationException(HttpStatus.BAD_REQUEST, "Provided shipper is inactive");
        }

        shipment.setShipper(shipper);
        shipment.setStatus(ShipmentStatus.ASSIGNED);
        shipment.setAssignedAt(LocalDateTime.now());

        String carrier = (carrierCode == null || carrierCode.isBlank())
                ? "MANUAL"
                : carrierCode.toUpperCase(Locale.ROOT);
        shipment.setCarrier(carrier);
        shipment.setTrackingCode(carrier + "-" + String.format("%06d", order.getId()));

        // Chuyển order sang SHIPPING vì đã có người đi giao
        order.setStatus(OrderStatus.SHIPPING);
        order.setShippedAt(LocalDateTime.now());
        
        syncOrderShipping(order, shipment);
        shipment = shipmentRepository.save(shipment);
        orderRepository.save(order);

        return toResponse(shipment);
    }

    // ================= SHIPPER ACTIONS =================

    @Override
    @Transactional
    public ShipmentInfoResponse markPickedUp(Long shipmentId) {
        Shipment ship = getShipmentForCurrentShipper(shipmentId);

        LocalDateTime now = LocalDateTime.now();
        ship.setStatus(ShipmentStatus.PICKED_UP);
        ship.setPickedUpAt(now);

        Order order = ship.getOrder();
        order.setShippedAt(now);
        syncOrderShipping(order, ship);

        shipmentRepository.save(ship);
        orderRepository.save(order);

        return toResponse(ship);
    }

    @Override
    @Transactional
    public ShipmentInfoResponse markOutForDelivery(Long shipmentId) {
        Shipment ship = getShipmentForCurrentShipper(shipmentId);

        LocalDateTime now = LocalDateTime.now();
        ship.setStatus(ShipmentStatus.IN_DELIVERY);

        Order order = ship.getOrder();
        if (order.getStatus() != OrderStatus.CANCELED && order.getShippedAt() == null) {
            order.setShippedAt(now);
        }
        syncOrderShipping(order, ship);

        shipmentRepository.save(ship);
        orderRepository.save(order);

        return toResponse(ship);
    }

    @Override
    @Transactional
    public ShipmentInfoResponse markDelivered(Long shipmentId) {
        Shipment ship = getShipmentForCurrentShipper(shipmentId);

        LocalDateTime now = LocalDateTime.now();
        ship.setStatus(ShipmentStatus.DELIVERED);
        ship.setDeliveredAt(now);

        Order order = ship.getOrder();
        order.setDeliveredAt(now);
        syncOrderShipping(order, ship);

        if (order.getPaymentMethod() == PaymentMethod.CASH_ON_DELIVERY
                && order.getPaymentStatus() != PaymentStatus.PAID) {
            order.setPaymentStatus(PaymentStatus.PAID);
        }

        shipmentRepository.save(ship);
        orderRepository.save(order);

        return toResponse(ship);
    }

    @Override
    @Transactional
    public ShipmentInfoResponse markFailed(Long shipmentId, String reason) {
        Shipment ship = getShipmentForCurrentShipper(shipmentId);

        LocalDateTime now = LocalDateTime.now();
        ship.setStatus(ShipmentStatus.FAILED);
        ship.setFailedAt(now);
        ship.setAttempts((ship.getAttempts() == null ? 0 : ship.getAttempts()) + 1);
        ship.setNote(reason);

        Order order = ship.getOrder();
        if (order.getShippedAt() == null) {
            order.setShippedAt(now);
        }
        syncOrderShipping(order, ship);

        shipmentRepository.save(ship);
        orderRepository.save(order);

        return toResponse(ship);
    }

    @Override
    @Transactional
    public ShipmentInfoResponse markReturned(Long shipmentId, String reason) {
        Shipment ship = getShipmentForCurrentShipper(shipmentId);

        LocalDateTime now = LocalDateTime.now();
        ship.setStatus(ShipmentStatus.RETURNED);
        ship.setReturnedAt(now);
        ship.setNote(reason);
        ship.setReturnRequested(false);

        Order order = ship.getOrder();
        order.setStatus(OrderStatus.CANCELED);
        order.setCanceledAt(now);
        syncOrderShipping(order, ship);

        shipmentRepository.save(ship);
        orderRepository.save(order);

        return toResponse(ship);
    }

    @Override
    @Transactional
    public ShipmentInfoResponse userRequestReturn(Long orderId, String reason) {
        User user = getCurrentUser();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Order not found"));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "You do not own this order");
        }

        Shipment shipment = getOrCreateShipment(order);

        if (order.getStatus() == OrderStatus.CANCELED || shipment.getStatus() == ShipmentStatus.RETURNED) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Order is already canceled or returned");
        }

        if (shipment.isReturnRequested()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Return request already submitted");
        }

        shipment.setReturnRequested(true);
        shipment.setNote(reason);
        syncOrderShipping(order, shipment);

        shipmentRepository.save(shipment);
        orderRepository.save(order);

        return toResponse(shipment);
    }

    @Override
    @Transactional
    public ShipmentInfoResponse approveReturn(Long shipmentId, String reason) {
        Shipment ship = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Shipment not found"));

        if (!ship.isReturnRequested()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Shipment is not in return-requested state");
        }

        LocalDateTime now = LocalDateTime.now();
        ship.setStatus(ShipmentStatus.RETURNED);
        ship.setReturnedAt(now);
        ship.setNote(reason);
        ship.setReturnRequested(false);

        Order order = ship.getOrder();
        order.setStatus(OrderStatus.CANCELED);
        order.setCanceledAt(now);
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            order.setPaymentStatus(PaymentStatus.REFUNDED);
        }
        syncOrderShipping(order, ship);

        shipmentRepository.save(ship);
        orderRepository.save(order);

        return toResponse(ship);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ShipmentInfoResponse> getMyShipments(int page, int size) {
        User current = getCurrentUser();
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "assignedAt").and(Sort.by("id")));
        // Mặc định chỉ trả các shipment chưa hoàn tất (không gồm RETURNED/DELIVERED)
        var excluded = java.util.List.of(ShipmentStatus.RETURNED, ShipmentStatus.DELIVERED);
        return shipmentRepository.findByShipperAndStatusNotIn(current, excluded, pageable)
                .map(this::toResponse);
    }

    // ================= Helper =================

    private Shipment getShipmentForCurrentShipper(Long shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Shipment not found"));

        User current = getCurrentUser();

        if (shipment.getShipper() == null ||
                !shipment.getShipper().getId().equals(current.getId())) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "You are not assigned to this shipment");
        }

        if (shipment.getStatus() == ShipmentStatus.DELIVERED
                || shipment.getStatus() == ShipmentStatus.RETURNED) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Shipment is already completed");
        }

        return shipment;
    }

    private Shipment getOrCreateShipment(Order order) {
        return shipmentRepository.findByOrder(order)
                .orElseGet(() -> shipmentRepository.save(
                        Shipment.builder()
                                .order(order)
                                .status(ShipmentStatus.PENDING_ASSIGN)
                                .build()
                ));
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ApplicationException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private void syncOrderShipping(Order order, Shipment shipment) {
        order.setShippingCarrier(shipment.getCarrier());
        order.setShippingTrackingCode(shipment.getTrackingCode());
    }

    private boolean isShipper(User user) {
        if (user.getRoles() == null) return false;
        return user.getRoles().stream()
                .anyMatch(r -> r.getCode() != null && "SHIPPER".equalsIgnoreCase(r.getCode().trim()));
    }

    private ShipmentInfoResponse toResponse(Shipment ship) {
        return ShipmentInfoResponse.builder()
                .shipmentId(ship.getId())
                .orderId(ship.getOrder().getId())
                .orderCode(ship.getOrder().getCode())
                .status(ship.getStatus())
                .carrier(ship.getCarrier())
                .trackingCode(ship.getTrackingCode())
                .attempts(ship.getAttempts())
                .note(ship.getNote())
                .returnRequested(ship.isReturnRequested())
                .assignedAt(ship.getAssignedAt())
                .pickedUpAt(ship.getPickedUpAt())
                .deliveredAt(ship.getDeliveredAt())
                .failedAt(ship.getFailedAt())
                .returnedAt(ship.getReturnedAt())
                .build();
    }
}
