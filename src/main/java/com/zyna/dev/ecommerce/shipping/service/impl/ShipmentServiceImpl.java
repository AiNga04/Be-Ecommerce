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
import com.zyna.dev.ecommerce.products.repository.SizeRepository;
import com.zyna.dev.ecommerce.products.repository.ProductSizeRepository;
import com.zyna.dev.ecommerce.orders.models.OrderItem;
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
    private final SizeRepository sizeRepository;
    private final ProductSizeRepository productSizeRepository;

    // ================= ADMIN / STAFF =================

    @Override
    @Transactional
    public ShipmentInfoResponse assignShipper(Long orderId, Long shipperId, String carrierCode) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng"));

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
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Không tìm thấy shipper"));

        if (!isShipper(shipper)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Người dùng được cung cấp không phải là shipper");
        }

        if (shipper.getStatus() != com.zyna.dev.ecommerce.common.enums.Status.ACTIVE) {
             throw new ApplicationException(HttpStatus.BAD_REQUEST, "Shipper được cung cấp đang bị khóa");
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
        validateTransition(ship.getStatus(), ShipmentStatus.PICKED_UP);

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
        validateTransition(ship.getStatus(), ShipmentStatus.IN_DELIVERY);

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
        validateTransition(ship.getStatus(), ShipmentStatus.DELIVERED);

        LocalDateTime now = LocalDateTime.now();
        ship.setStatus(ShipmentStatus.DELIVERED);
        ship.setDeliveredAt(now);

        Order order = ship.getOrder();
        order.setStatus(OrderStatus.DELIVERED);
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
        validateTransition(ship.getStatus(), ShipmentStatus.FAILED);

        LocalDateTime now = LocalDateTime.now();
        int currentAttempts = (ship.getAttempts() == null ? 0 : ship.getAttempts()) + 1;
        ship.setAttempts(currentAttempts);
        ship.setNote(reason);

        Order order = ship.getOrder();
        if (order.getShippedAt() == null) {
            order.setShippedAt(now);
        }

        if (currentAttempts >= 3) {
            // Quá 3 lần giao thất bại -> Tự động CHUYỂN HOÀN (RETURNED)
            ship.setStatus(ShipmentStatus.RETURNED);
            ship.setReturnedAt(now);
            ship.setNote(reason + " (Tự động chuyển hoàn do giao thất bại " + currentAttempts + " lần)");
            
            order.setStatus(OrderStatus.CANCELED);
            order.setCanceledAt(now);
            restoreStock(order);
            
            // Xóa logic failed ở các fields nếu trước đó có gọi để đảm bảo chuẩn
            ship.setFailedAt(now); 
        } else {
            // Chưa tới 3 lần -> Lưu trạng thái FAILED
            ship.setStatus(ShipmentStatus.FAILED);
            ship.setFailedAt(now);
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
        validateTransition(ship.getStatus(), ShipmentStatus.RETURNED);

        LocalDateTime now = LocalDateTime.now();
        ship.setStatus(ShipmentStatus.RETURNED);
        ship.setReturnedAt(now);
        ship.setNote(reason);
        ship.setReturnRequested(false);

        Order order = ship.getOrder();
        order.setStatus(OrderStatus.CANCELED);
        order.setCanceledAt(now);
        restoreStock(order);
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            order.setPaymentStatus(PaymentStatus.REFUNDED);
        }
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
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng"));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "Đơn hàng này không thuộc về bạn");
        }

        Shipment shipment = getOrCreateShipment(order);

        if (order.getStatus() == OrderStatus.CANCELED || shipment.getStatus() == ShipmentStatus.RETURNED) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Đơn hàng đã được hoàn trả hoặc bị hủy");
        }

        if (shipment.isReturnRequested()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Đã gửi yêu cầu trả hàng trước đó");
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
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn giao hàng"));

        if (!ship.isReturnRequested()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Đơn giao hàng không ở trạng thái yêu cầu trả hàng");
        }

        LocalDateTime now = LocalDateTime.now();
        ship.setStatus(ShipmentStatus.RETURN_APPROVED);
        ship.setNote(reason != null ? reason : "Admin đã duyệt trả hàng");
        ship.setReturnRequested(false);

        Order order = ship.getOrder();
        syncOrderShipping(order, ship);

        shipmentRepository.save(ship);
        orderRepository.save(order);

        return toResponse(ship);
    }
    @Override
    @Transactional
    public ShipmentInfoResponse rejectReturn(Long shipmentId, String reason) {
        Shipment ship = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn giao hàng"));

        if (!ship.isReturnRequested()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Đơn giao hàng không ở trạng thái yêu cầu trả hàng");
        }

        ship.setReturnRequested(false);
        ship.setNote(reason != null ? "Từ chối trả hàng: " + reason : "Admin đã từ chối trả hàng");

        Order order = ship.getOrder();
        syncOrderShipping(order, ship);

        shipmentRepository.save(ship);
        orderRepository.save(order);

        return toResponse(ship);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ShipmentInfoResponse> getMyShipments(int page, int size, ShipmentStatus status) {
        User current = getCurrentUser();
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "assignedAt").and(Sort.by("id")));
        
        if (status != null) {
            return shipmentRepository.findByShipperAndStatus(current, status, pageable)
                    .map(this::toResponse);
        }

        // Mặc định chỉ trả các shipment chưa hoàn tất (không gồm RETURNED/DELIVERED)
        var excluded = java.util.List.of(ShipmentStatus.RETURNED, ShipmentStatus.DELIVERED);
        return shipmentRepository.findByShipperAndStatusNotIn(current, excluded, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public com.zyna.dev.ecommerce.shipping.dto.response.ShipperDashboardStatsResponse getMyDashboardStats(String from, String to) {
        User current = getCurrentUser();
        LocalDateTime startOfDay = java.time.LocalDate.now().atStartOfDay();

        long pendingPickups = shipmentRepository.countByShipperAndStatuses(current, java.util.List.of(ShipmentStatus.ASSIGNED));
        long inProgress = shipmentRepository.countByShipperAndStatuses(current, java.util.List.of(ShipmentStatus.PICKED_UP, ShipmentStatus.IN_DELIVERY));
        long deliveredToday = shipmentRepository.countDeliveredSince(current, ShipmentStatus.DELIVERED, startOfDay);
        long failedToday = shipmentRepository.countFailedSince(current, ShipmentStatus.FAILED, startOfDay);
        java.math.BigDecimal codCollected = shipmentRepository.sumCodCollectedSince(current, startOfDay);

        if (codCollected == null) {
            codCollected = java.math.BigDecimal.ZERO;
        }

        long totalDelivered = shipmentRepository.countByShipperAndStatus(current, ShipmentStatus.DELIVERED);
        long totalFailed = shipmentRepository.countByShipperAndStatus(current, ShipmentStatus.FAILED);
        long totalReturned = shipmentRepository.countByShipperAndStatus(current, ShipmentStatus.RETURNED);

        return com.zyna.dev.ecommerce.shipping.dto.response.ShipperDashboardStatsResponse.builder()
                .pendingPickups(pendingPickups)
                .inProgress(inProgress)
                .deliveredToday(deliveredToday)
                .failedToday(failedToday)
                .codCollectedToday(codCollected)
                .totalDelivered(totalDelivered)
                .totalFailed(totalFailed)
                .totalReturned(totalReturned)
                .chartData(buildChartData(current, from, to))
                .build();
    }

    private java.util.List<com.zyna.dev.ecommerce.shipping.dto.response.ShipperChartData> buildChartData(User shipper, String from, String to) {
        java.util.List<com.zyna.dev.ecommerce.shipping.dto.response.ShipperChartData> result = new java.util.ArrayList<>();
        
        java.time.LocalDate fromDate;
        java.time.LocalDate toDate;
        
        if (from != null && !from.isBlank() && to != null && !to.isBlank()) {
            fromDate = java.time.LocalDate.parse(from);
            toDate = java.time.LocalDate.parse(to);
        } else {
            toDate = java.time.LocalDate.now();
            fromDate = toDate.minusDays(6);
        }

        if (fromDate.isAfter(toDate)) {
            java.time.LocalDate temp = fromDate;
            fromDate = toDate;
            toDate = temp;
        }

        LocalDateTime timeFrom = fromDate.atStartOfDay();

        java.util.List<Shipment> recentShipments = shipmentRepository.findRecentHistoryByShipper(
                shipper,
                java.util.List.of(ShipmentStatus.DELIVERED, ShipmentStatus.FAILED, ShipmentStatus.RETURNED),
                timeFrom
        );

        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(fromDate, toDate);
        if (daysBetween > 365) {
            fromDate = toDate.minusDays(365);
            daysBetween = 365;
        }

        for (int i = 0; i <= daysBetween; i++) {
            java.time.LocalDate targetDate = fromDate.plusDays(i);
            String dateString = targetDate.toString();

            long deliveredCount = 0;
            long failedCount = 0;
            long returnedCount = 0;
            java.math.BigDecimal codCollected = java.math.BigDecimal.ZERO;

            for (Shipment s : recentShipments) {
                if (s.getStatus() == ShipmentStatus.DELIVERED && s.getDeliveredAt() != null && s.getDeliveredAt().toLocalDate().equals(targetDate)) {
                    deliveredCount++;
                    if (s.getOrder() != null && "CASH_ON_DELIVERY".equals(s.getOrder().getPaymentMethod().name())) {
                        codCollected = codCollected.add(s.getOrder().getTotalPrice());
                    }
                }
                if (s.getStatus() == ShipmentStatus.FAILED && s.getFailedAt() != null && s.getFailedAt().toLocalDate().equals(targetDate)) {
                    failedCount++;
                }
                if (s.getStatus() == ShipmentStatus.RETURNED && s.getReturnedAt() != null && s.getReturnedAt().toLocalDate().equals(targetDate)) {
                    returnedCount++;
                }
            }

            result.add(com.zyna.dev.ecommerce.shipping.dto.response.ShipperChartData.builder()
                    .date(dateString)
                    .deliveredCount(deliveredCount)
                    .failedCount(failedCount)
                    .returnedCount(returnedCount)
                    .codCollected(codCollected)
                    .build());
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ShipmentInfoResponse> getMyHistory(int page, int size, ShipmentStatus status) {
        User current = getCurrentUser();
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        
        if (status != null) {
            return shipmentRepository.findByShipperAndStatus(current, status, pageable)
                    .map(this::toResponse);
        }

        // Lịch sử = Đã có kết quả cuối cùng ở lần giao này
        var historyStatuses = java.util.List.of(ShipmentStatus.DELIVERED, ShipmentStatus.FAILED, ShipmentStatus.RETURNED);
        return shipmentRepository.findByShipperAndStatusIn(current, historyStatuses, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentInfoResponse getMyShipmentById(Long shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn giao hàng"));

        User current = getCurrentUser();

        if (shipment.getShipper() == null ||
                !shipment.getShipper().getId().equals(current.getId())) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "Bạn không được phân công cho đơn giao hàng này");
        }

        return toResponse(shipment);
    }

    // ================= ADMIN =================

    @Override
    @Transactional(readOnly = true)
    public Page<ShipmentInfoResponse> getAllShipments(int page, int size, ShipmentStatus status, Long shipperId, Boolean returnRequested) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Shipment> shipments = shipmentRepository.findByAdminFilters(status, shipperId, returnRequested, pageable);
        return shipments.map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentInfoResponse getShipmentById(Long shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn giao hàng"));
        return toResponse(shipment);
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentInfoResponse getShipmentByOrderId(Long orderId) {
        Shipment shipment = shipmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Shipment not found for order " + orderId));
        return toResponse(shipment);
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
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Đơn giao hàng đã hoàn tất");
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
                .orElseThrow(() -> new ApplicationException(HttpStatus.UNAUTHORIZED, "Không tìm thấy người dùng"));
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

    /**
     * Validate shipment status transition:
     *   ASSIGNED        → PICKED_UP
     *   PICKED_UP       → IN_DELIVERY
     *   IN_DELIVERY     → DELIVERED, FAILED
     *   PICKED_UP       → FAILED
     *   FAILED          → IN_DELIVERY (giao lại), RETURNED (auto 3 lần)
     *   RETURN_APPROVED  → RETURNED (shipper lấy hàng về)
     */
    private void validateTransition(ShipmentStatus current, ShipmentStatus target) {
        boolean valid = switch (target) {
            case PICKED_UP     -> current == ShipmentStatus.ASSIGNED;
            case IN_DELIVERY   -> current == ShipmentStatus.PICKED_UP || current == ShipmentStatus.FAILED;
            case DELIVERED      -> current == ShipmentStatus.IN_DELIVERY;
            case FAILED         -> current == ShipmentStatus.PICKED_UP || current == ShipmentStatus.IN_DELIVERY;
            case RETURNED       -> current == ShipmentStatus.RETURN_APPROVED || current == ShipmentStatus.FAILED || current == ShipmentStatus.PICKED_UP || current == ShipmentStatus.IN_DELIVERY;
            default             -> false;
        };
        if (!valid) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST,
                    "Không thể chuyển trạng thái từ " + current + " sang " + target);
        }
    }

    private ShipmentInfoResponse toResponse(Shipment ship) {
        User shipper = ship.getShipper();
        Order order = ship.getOrder();
        return ShipmentInfoResponse.builder()
                .shipmentId(ship.getId())
                .orderId(order.getId())
                .orderCode(order.getCode())
                .status(ship.getStatus())
                .carrier(ship.getCarrier())
                .trackingCode(ship.getTrackingCode())
                .shipperId(shipper != null ? shipper.getId() : null)
                .shipperName(shipper != null ? shipper.getFirstName() + " " + shipper.getLastName() : null)
                .shipperPhone(shipper != null ? shipper.getPhone() : null)
                .shippingName(order.getShippingName())
                .shippingPhone(order.getShippingPhone())
                .shippingAddress(order.getShippingAddress())
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

    private void restoreStock(Order order) {
        if (order.getItems() == null) return;
        for (OrderItem item : order.getItems()) {
            if (item.getSize() == null) continue;
            sizeRepository.findByName(item.getSize()).ifPresent(sizeObj -> {
                productSizeRepository.findByProductAndSize(item.getProduct(), sizeObj).ifPresent(ps -> {
                    ps.setQuantity(ps.getQuantity() + item.getQuantity());
                    productSizeRepository.save(ps);
                });
            });
        }
    }
}
