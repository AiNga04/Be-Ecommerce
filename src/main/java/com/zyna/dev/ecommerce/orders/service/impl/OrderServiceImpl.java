package com.zyna.dev.ecommerce.orders.service.impl;

import com.zyna.dev.ecommerce.address.models.ShippingAddress;
import com.zyna.dev.ecommerce.address.repository.ShippingAddressRepository;
import com.zyna.dev.ecommerce.carts.models.CartItem;
import com.zyna.dev.ecommerce.carts.repository.CartItemRepository;
import com.zyna.dev.ecommerce.common.enums.OrderStatus;
import com.zyna.dev.ecommerce.common.enums.PaymentStatus;
import com.zyna.dev.ecommerce.common.enums.ShipmentStatus;
import com.zyna.dev.ecommerce.common.exceptions.ApplicationException;
import com.zyna.dev.ecommerce.orders.OrderMapper;
import com.zyna.dev.ecommerce.orders.dto.request.CheckoutFromCartRequest;
import com.zyna.dev.ecommerce.orders.dto.request.CheckoutItemRequest;
import com.zyna.dev.ecommerce.orders.dto.request.CheckoutRequest;
import com.zyna.dev.ecommerce.orders.dto.response.OrderResponse;
import com.zyna.dev.ecommerce.orders.models.Order;
import com.zyna.dev.ecommerce.orders.models.OrderItem;
import com.zyna.dev.ecommerce.orders.repository.OrderRepository;
import com.zyna.dev.ecommerce.orders.service.interfaces.OrderService;
import com.zyna.dev.ecommerce.products.models.Product;
import com.zyna.dev.ecommerce.products.repository.ProductRepository;
import com.zyna.dev.ecommerce.shipping.models.Shipment;
import com.zyna.dev.ecommerce.shipping.repository.ShipmentRepository;
import com.zyna.dev.ecommerce.vouchers.dto.request.VoucherApplyRequest;
import com.zyna.dev.ecommerce.vouchers.dto.response.VoucherApplyResponse;
import com.zyna.dev.ecommerce.vouchers.service.interfaces.VoucherService;
import com.zyna.dev.ecommerce.users.models.User;
import com.zyna.dev.ecommerce.users.UserRepository;
import com.zyna.dev.ecommerce.notifications.NotificationService;
import com.zyna.dev.ecommerce.notifications.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;
    private final CartItemRepository cartItemRepository;
    private final ShippingAddressRepository addressRepository;
    private final ShipmentRepository shipmentRepository;
    private final VoucherService voucherService;
    private final NotificationService notificationService;
    private final com.zyna.dev.ecommerce.products.repository.SizeRepository sizeRepository;
    private final com.zyna.dev.ecommerce.products.repository.ProductSizeRepository productSizeRepository;
    @Value("${app.inventory.low-stock.threshold:5}")
    private int lowStockThreshold;

    @Override
    @Transactional
    public OrderResponse checkout(Long userId, CheckoutRequest request) {
        User user = getUser(userId);

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Cart is empty");
        }

        // 1. Chuẩn bị danh sách item, check tồn kho, tính tổng tiền
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal itemsTotal = BigDecimal.ZERO;

        for (CheckoutItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ApplicationException(
                            HttpStatus.NOT_FOUND,
                            "Product not found: " + itemReq.getProductId()
                    ));

            if (itemReq.getSizeId() == null) {
                // Assuming all products must have a size chosen if sizes exist. 
                // Simplification for now: require Size.
                throw new ApplicationException(HttpStatus.BAD_REQUEST, "Size is required for product: " + product.getName());
            }

            var sizeObj = sizeRepository.findById(itemReq.getSizeId())
                        .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Size not found"));

            com.zyna.dev.ecommerce.products.models.ProductSize productSize = productSizeRepository.findByProductAndSize(product, sizeObj)
                    .orElseThrow(() -> new ApplicationException(HttpStatus.BAD_REQUEST, "Product variant unavailable"));

            int requestedQty = itemReq.getQuantity();
            if (requestedQty <= 0) {
                throw new ApplicationException(HttpStatus.BAD_REQUEST, "Quantity must be > 0");
            }

            if (productSize.getQuantity() < requestedQty) {
                throw new ApplicationException(
                        HttpStatus.BAD_REQUEST,
                        "Not enough stock for product: " + product.getName() + " (Size: " + sizeObj.getName() + ")"
                );
            }

            BigDecimal unitPrice = product.getPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(requestedQty));

            String sizeName = sizeObj.getName();

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(requestedQty)
                    .unitPrice(unitPrice)
                    .subtotal(subtotal)
                    .size(sizeName)
                    .build();

            orderItems.add(orderItem);
            itemsTotal = itemsTotal.add(subtotal);
        }

        // 2. Xử lý địa chỉ: dùng addressId hoặc dùng 3 field tay
        String shippingName;
        String shippingPhone;
        String shippingAddress;

        if (request.getShippingAddressId() != null) {
            // lấy địa chỉ từ address book
            ShippingAddress addr = addressRepository.findById(request.getShippingAddressId())
                    .orElseThrow(() ->
                            new ApplicationException(HttpStatus.NOT_FOUND, "Address not found")
                    );

            // check địa chỉ có thuộc về user không
            if (!addr.getUser().getId().equals(user.getId())) {
                throw new ApplicationException(HttpStatus.FORBIDDEN, "You do not own this address");
            }

            shippingName = addr.getReceiverName();
            shippingPhone = addr.getReceiverPhone();
            shippingAddress = addr.getFullAddress();
        } else {
            // nhập tay thì bắt buộc phải đủ 3 field
            if (!StringUtils.hasText(request.getShippingName())
                    || !StringUtils.hasText(request.getShippingPhone())
                    || !StringUtils.hasText(request.getShippingAddress())) {
                throw new ApplicationException(
                        HttpStatus.BAD_REQUEST,
                        "Shipping information is required"
                );
            }

            shippingName = request.getShippingName();
            shippingPhone = request.getShippingPhone();
            shippingAddress = request.getShippingAddress();
        }

        BigDecimal baseShippingFee = BigDecimal.valueOf(30000); // mặc định 30k, không phụ thuộc client gửi
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal shippingDiscount = BigDecimal.ZERO;
        String voucherCode = null;
        String shippingVoucherCode = null;
        boolean discountVoucherUsed = false;
        boolean shippingVoucherUsed = false;

        // Apply vouchers in order; each code may be discount or freeship. Limit 1 of each type.
        if (StringUtils.hasText(request.getVoucherCode())) {
            VoucherApplyResponse res = voucherService.apply(
                    VoucherApplyRequest.builder()
                            .code(request.getVoucherCode().trim())
                            .cartTotal(itemsTotal)
                            .shippingFee(baseShippingFee)
                            .build()
            );
            if (!res.isValid()) {
                throw new ApplicationException(HttpStatus.BAD_REQUEST, res.getMessage());
            }

            BigDecimal discountFromVoucher = res.getDiscountAmount();
            BigDecimal shippingDiscountFromVoucher = res.getShippingDiscount();

            if (discountFromVoucher.compareTo(BigDecimal.ZERO) > 0) {
                if (discountVoucherUsed) {
                    throw new ApplicationException(HttpStatus.BAD_REQUEST, "Discount voucher already applied");
                }
                discountAmount = discountAmount.add(discountFromVoucher);
                discountVoucherUsed = true;
                voucherCode = request.getVoucherCode().trim();
            }

            if (shippingDiscountFromVoucher.compareTo(BigDecimal.ZERO) > 0) {
                if (shippingVoucherUsed) {
                    throw new ApplicationException(HttpStatus.BAD_REQUEST, "Shipping voucher already applied");
                }
                shippingDiscount = shippingDiscount.add(shippingDiscountFromVoucher);
                shippingVoucherUsed = true;
                if (shippingVoucherCode == null) {
                    shippingVoucherCode = request.getVoucherCode().trim();
                }
                if (voucherCode == null) {
                    voucherCode = request.getVoucherCode().trim();
                }
            }
        }

        if (StringUtils.hasText(request.getShippingVoucherCode())) {
            VoucherApplyResponse res = voucherService.apply(
                    VoucherApplyRequest.builder()
                            .code(request.getShippingVoucherCode().trim())
                            .cartTotal(itemsTotal)
                            .shippingFee(baseShippingFee)
                            .build()
            );
            if (!res.isValid()) {
                throw new ApplicationException(HttpStatus.BAD_REQUEST, res.getMessage());
            }

            BigDecimal discountFromVoucher = res.getDiscountAmount();
            BigDecimal shippingDiscountFromVoucher = res.getShippingDiscount();

            if (discountFromVoucher.compareTo(BigDecimal.ZERO) > 0) {
                if (discountVoucherUsed) {
                    throw new ApplicationException(HttpStatus.BAD_REQUEST, "Discount voucher already applied");
                }
                discountAmount = discountAmount.add(discountFromVoucher);
                discountVoucherUsed = true;
                if (voucherCode == null) {
                    voucherCode = request.getShippingVoucherCode().trim();
                }
            }

            if (shippingDiscountFromVoucher.compareTo(BigDecimal.ZERO) > 0) {
                if (shippingVoucherUsed) {
                    throw new ApplicationException(HttpStatus.BAD_REQUEST, "Shipping voucher already applied");
                }
                shippingDiscount = shippingDiscount.add(shippingDiscountFromVoucher);
                shippingVoucherUsed = true;
                shippingVoucherCode = request.getShippingVoucherCode().trim();
            }
        }

        BigDecimal finalCartTotal = itemsTotal.subtract(discountAmount);
        if (finalCartTotal.compareTo(BigDecimal.ZERO) < 0) {
            finalCartTotal = BigDecimal.ZERO;
        }
        BigDecimal shippingFee = baseShippingFee.subtract(shippingDiscount);
        if (shippingFee.compareTo(BigDecimal.ZERO) < 0) {
            shippingFee = BigDecimal.ZERO;
        }
        BigDecimal total = finalCartTotal.add(shippingFee);

        // 3. Tạo Order
        Order order = Order.builder()
                .user(user)
                .totalPrice(total)
                .status(OrderStatus.PENDING)
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(PaymentStatus.UNPAID)
                .shippingFee(shippingFee)
                .discountAmount(discountAmount)
                .shippingDiscount(shippingDiscount)
                .voucherCode(voucherCode)
                .shippingVoucherCode(shippingVoucherCode)
                .shippingName(shippingName)
                .shippingPhone(shippingPhone)
                .shippingAddress(shippingAddress)
                .build();

        if (order.getCode() == null) {
            order.setCode(generateOrderCode());
        }

        // set quan hệ 2 chiều
        for (OrderItem oi : orderItems) {
            oi.setOrder(order);
        }
        order.setItems(orderItems);

        // 4. Giảm stock (ProductSize)
        for (CheckoutItemRequest itemReq : request.getItems()) {
             // Re-fetch to lock? Assuming transaction handles optimistic locking or we accept race conditions for MVP.
             // We need to find the correct ProductSize again to deduct.
             Product p = productRepository.getReferenceById(itemReq.getProductId());
             var s = sizeRepository.getReferenceById(itemReq.getSizeId());
             
             var ps = productSizeRepository.findByProductAndSize(p, s)
                     .orElseThrow(); // Should exist as checked above
             
             ps.setQuantity(ps.getQuantity() - itemReq.getQuantity());
             productSizeRepository.save(ps);
             
             checkLowStockAndNotify(ps);
        }

        // 5. Lưu order
        order = orderRepository.save(order);

        // 6. Notification
        List<java.util.Map<String, Object>> itemDetails = orderItems.stream().map(item -> java.util.Map.<String, Object>of(
                "productName", item.getProduct().getName(),
                "size", item.getSize(),
                "quantity", item.getQuantity(),
                "price", item.getUnitPrice(),
                "subtotal", item.getSubtotal(),
                "image", item.getProduct().getImageUrl() != null ? item.getProduct().getImageUrl() : ""
        )).toList();

        notificationService.sendEmail(
                NotificationType.ORDER_PLACED,
                user,
                java.util.Map.of(
                        "orderCode", order.getCode() != null ? order.getCode() : order.getId(),
                        "total", order.getTotalPrice(),
                        "paymentMethod", order.getPaymentMethod(),
                        "shippingName", order.getShippingName(),
                        "shippingPhone", order.getShippingPhone(),
                        "shippingAddress", order.getShippingAddress(),
                        "items", itemDetails,
                        "discountAmount", order.getDiscountAmount(),
                        "shippingFee", order.getShippingFee()
                )
        );

        // 7. Map sang response
        return orderMapper.toOrderResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Order not found"));

        OrderStatus currentStatus = order.getStatus();

        // 1. Validate status transition
        boolean validTransition = false;
        if (currentStatus == OrderStatus.PENDING) {
            validTransition = (newStatus == OrderStatus.CONFIRMED || newStatus == OrderStatus.CANCELED);
        }

        if (!validTransition) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, 
                "Chỉ được chuyển trạng thái từ PENDING sang CONFIRMED hoặc CANCELED. Các trạng thái khác sẽ do luồng giao nhận xử lý.");
        }

        order.setStatus(newStatus);

        LocalDateTime now = LocalDateTime.now();
        if (newStatus == OrderStatus.CONFIRMED) {
            order.setConfirmedAt(now);
            // Tự động tạo Shipment khi order được xác nhận
            createShipmentIfMissing(order);
        } else if (newStatus == OrderStatus.SHIPPING) {
            order.setShippedAt(now);
        } else if (newStatus == OrderStatus.DELIVERED) {
            order.setDeliveredAt(now);
        } else if (newStatus == OrderStatus.CANCELED) {
            order.setCanceledAt(now);
        }

        // 2. Handle Shipment status when CANCELED
        if (newStatus == OrderStatus.CANCELED) {
             shipmentRepository.findByOrder(order).ifPresent(shipment -> {
                 shipment.setStatus(ShipmentStatus.RETURNED);
                 shipment.setReturnedAt(now);
                 shipmentRepository.save(shipment);
             });
             restoreStock(order);
        }

        order = orderRepository.save(order);
        return orderMapper.toOrderResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse checkoutFromCart(Long userId, CheckoutFromCartRequest request) {
        User user = getUser(userId);

        List<CartItem> cartItems = cartItemRepository.findByUser(user);
        if (cartItems == null || cartItems.isEmpty()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Cart is empty");
        }

        // filter theo cartItemIds (nếu có)
        List<CartItem> selectedItems = cartItems;
        if (request.getCartItemIds() != null && !request.getCartItemIds().isEmpty()) {
            selectedItems = cartItems.stream()
                    .filter(ci -> request.getCartItemIds().contains(ci.getId()))
                    .toList();
        }

        if (selectedItems.isEmpty()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "No cart items selected to checkout");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal itemsTotal = BigDecimal.ZERO;

        // check tồn kho + build order items
        for (CartItem cartItem : selectedItems) {
            Product product = cartItem.getProduct();
            int requestedQty = cartItem.getQuantity();

            if (requestedQty <= 0) continue;

            com.zyna.dev.ecommerce.products.models.Size size = cartItem.getSize();
            if (size == null) {
                // Should not happen for valid cart items but safeguard
                 throw new ApplicationException(HttpStatus.BAD_REQUEST, "Cart Item missing size");
            }

            com.zyna.dev.ecommerce.products.models.ProductSize productSize = productSizeRepository.findByProductAndSize(product, size)
                    .orElseThrow(() -> new ApplicationException(HttpStatus.BAD_REQUEST, "Product variant unavailable for " + product.getName()));

            if (productSize.getQuantity() < requestedQty) {
                throw new ApplicationException(
                        HttpStatus.BAD_REQUEST,
                        "Not enough stock for product: " + product.getName() + " (Size: " + size.getName() + ")"
                );
            }

            BigDecimal unitPrice = product.getPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(requestedQty));

            String sizeName = size.getName();

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(requestedQty)
                    .unitPrice(unitPrice)
                    .subtotal(subtotal)
                    .size(sizeName)
                    .build();

            orderItems.add(orderItem);
            itemsTotal = itemsTotal.add(subtotal);
        }

        if (orderItems.isEmpty()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Cart is empty");
        }

        // Xử lý địa chỉ
        String shippingName = request.getShippingName();
        String shippingPhone = request.getShippingPhone();
        String shippingAddress = request.getShippingAddress();

        if (request.getShippingAddressId() != null) {
            ShippingAddress addr = addressRepository.findByIdAndUser(request.getShippingAddressId(), user)
                    .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Address not found"));

            shippingName = addr.getReceiverName();
            shippingPhone = addr.getReceiverPhone();
            shippingAddress = addr.getFullAddress();
        }

        BigDecimal baseShippingFee = BigDecimal.valueOf(30000); // mặc định 30k, không phụ thuộc client gửi
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal shippingDiscount = BigDecimal.ZERO;
        String voucherCode = null;
        String shippingVoucherCode = null;
        boolean discountVoucherUsed = false;
        boolean shippingVoucherUsed = false;

        if (StringUtils.hasText(request.getVoucherCode())) {
            VoucherApplyResponse res = voucherService.apply(
                    VoucherApplyRequest.builder()
                            .code(request.getVoucherCode().trim())
                            .cartTotal(itemsTotal)
                            .shippingFee(baseShippingFee)
                            .build()
            );
            if (!res.isValid()) {
                throw new ApplicationException(HttpStatus.BAD_REQUEST, res.getMessage());
            }
            BigDecimal discountFromVoucher = res.getDiscountAmount();
            BigDecimal shippingDiscountFromVoucher = res.getShippingDiscount();

            if (discountFromVoucher.compareTo(BigDecimal.ZERO) > 0) {
                if (discountVoucherUsed) {
                    throw new ApplicationException(HttpStatus.BAD_REQUEST, "Discount voucher already applied");
                }
                discountAmount = discountAmount.add(discountFromVoucher);
                discountVoucherUsed = true;
                voucherCode = request.getVoucherCode().trim();
            }

            if (shippingDiscountFromVoucher.compareTo(BigDecimal.ZERO) > 0) {
                if (shippingVoucherUsed) {
                    throw new ApplicationException(HttpStatus.BAD_REQUEST, "Shipping voucher already applied");
                }
                shippingDiscount = shippingDiscount.add(shippingDiscountFromVoucher);
                shippingVoucherUsed = true;
                if (shippingVoucherCode == null) {
                    shippingVoucherCode = request.getVoucherCode().trim();
                }
            }
        }

        if (StringUtils.hasText(request.getShippingVoucherCode())) {
            VoucherApplyResponse res = voucherService.apply(
                    VoucherApplyRequest.builder()
                            .code(request.getShippingVoucherCode().trim())
                            .cartTotal(itemsTotal)
                            .shippingFee(baseShippingFee)
                            .build()
            );
            if (!res.isValid()) {
                throw new ApplicationException(HttpStatus.BAD_REQUEST, res.getMessage());
            }
            BigDecimal discountFromVoucher = res.getDiscountAmount();
            BigDecimal shippingDiscountFromVoucher = res.getShippingDiscount();

            if (discountFromVoucher.compareTo(BigDecimal.ZERO) > 0) {
                if (discountVoucherUsed) {
                    throw new ApplicationException(HttpStatus.BAD_REQUEST, "Discount voucher already applied");
                }
                discountAmount = discountAmount.add(discountFromVoucher);
                discountVoucherUsed = true;
                if (voucherCode == null) {
                    voucherCode = request.getShippingVoucherCode().trim();
                }
            }

            if (shippingDiscountFromVoucher.compareTo(BigDecimal.ZERO) > 0) {
                if (shippingVoucherUsed) {
                    throw new ApplicationException(HttpStatus.BAD_REQUEST, "Shipping voucher already applied");
                }
                shippingDiscount = shippingDiscount.add(shippingDiscountFromVoucher);
                shippingVoucherUsed = true;
                shippingVoucherCode = request.getShippingVoucherCode().trim();
            }
        }

        BigDecimal finalCartTotal = itemsTotal.subtract(discountAmount);
        if (finalCartTotal.compareTo(BigDecimal.ZERO) < 0) {
            finalCartTotal = BigDecimal.ZERO;
        }
        BigDecimal shippingFee = baseShippingFee.subtract(shippingDiscount);
        if (shippingFee.compareTo(BigDecimal.ZERO) < 0) {
            shippingFee = BigDecimal.ZERO;
        }
        BigDecimal total = finalCartTotal.add(shippingFee);

        Order order = Order.builder()
                .user(user)
                .totalPrice(total)
                .status(OrderStatus.PENDING)
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(PaymentStatus.UNPAID)
                .shippingFee(shippingFee)
                .discountAmount(discountAmount)
                .shippingDiscount(shippingDiscount)
                .voucherCode(voucherCode)
                .shippingVoucherCode(shippingVoucherCode)
                .shippingName(shippingName)
                .shippingPhone(shippingPhone)
                .shippingAddress(shippingAddress)
                .build();

        if (order.getCode() == null) {
            order.setCode(generateOrderCode());
        }

        for (OrderItem oi : orderItems) {
            oi.setOrder(order);
        }
        order.setItems(orderItems);

        // trừ stock (ProductSize)
        for (CartItem cartItem : selectedItems) {
             Product p = cartItem.getProduct();
             var s = cartItem.getSize();
             var ps = productSizeRepository.findByProductAndSize(p, s)
                     .orElseThrow(); 
             
             ps.setQuantity(ps.getQuantity() - cartItem.getQuantity());
             productSizeRepository.save(ps);
             checkLowStockAndNotify(ps);
        }

        order = orderRepository.save(order);

        // xoá chỉ các cart item đã checkout
        cartItemRepository.deleteAll(selectedItems);

        // 6. Notification
        List<java.util.Map<String, Object>> itemDetails = orderItems.stream().map(item -> java.util.Map.<String, Object>of(
                "productName", item.getProduct().getName(),
                "size", item.getSize(),
                "quantity", item.getQuantity(),
                "price", item.getUnitPrice(),
                "subtotal", item.getSubtotal(),
                "image", item.getProduct().getImageUrl() != null ? item.getProduct().getImageUrl() : ""
        )).toList();

        notificationService.sendEmail(
                NotificationType.ORDER_PLACED,
                user,
                java.util.Map.of(
                        "orderCode", order.getCode() != null ? order.getCode() : order.getId(),
                        "total", order.getTotalPrice(),
                        "paymentMethod", order.getPaymentMethod(),
                        "shippingName", order.getShippingName(),
                        "shippingPhone", order.getShippingPhone(),
                        "shippingAddress", order.getShippingAddress(),
                        "items", itemDetails,
                        "discountAmount", order.getDiscountAmount(),
                        "shippingFee", order.getShippingFee()
                )
        );

        return orderMapper.toOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByIdForUser(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Order not found"));

        if (!order.getUser().getId().equals(userId)) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "You do not own this order");
        }

        return orderMapper.toOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getMyOrders(Long userId, int page, int size, OrderStatus status, PaymentStatus paymentStatus, ShipmentStatus shipmentStatus) {
        User user = getUser(userId);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> orders = orderRepository.findMyOrders(user, status, paymentStatus, shipmentStatus, pageable);

        // dùng mapper, không tự map trong service nữa
        return orders.map(orderMapper::toOrderResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> orders = orderRepository.findAll(pageable);
        return orders.map(orderMapper::toOrderResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByIdForAdmin(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Order not found"));
        return orderMapper.toOrderResponse(order);
    }


    // ================== PRIVATE HELPER ==================

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private void createShipmentIfMissing(Order order) {
        shipmentRepository.findByOrder(order)
                .orElseGet(() -> shipmentRepository.save(
                        Shipment.builder()
                                .order(order)
                                .status(ShipmentStatus.PENDING_ASSIGN)
                                .build()
                ));
    }

    private Shipment ensureShipmentForOrder(Order order) {
        return shipmentRepository.findByOrder(order)
                .orElseGet(() -> shipmentRepository.save(
                        Shipment.builder()
                                .order(order)
                                .status(ShipmentStatus.PENDING_ASSIGN)
                                .build()
                ));
    }

    private String generateOrderCode() {
        return "ORD-" + System.currentTimeMillis();
    }

    private void checkLowStockAndNotify(com.zyna.dev.ecommerce.products.models.ProductSize productSize) {
        Integer stock = productSize.getQuantity();
        if (stock == null || stock >= lowStockThreshold) {
            return;
        }

        var inventoryUsers = userRepository.findAllByRoles_CodeIgnoreCaseAndIsDeletedFalse("INVENTORY");
        var adminUsers = userRepository.findAllByRoles_CodeIgnoreCaseAndIsDeletedFalse("ADMIN");

        var emails = new java.util.HashSet<String>();
        inventoryUsers.forEach(u -> emails.add(u.getEmail()));
        adminUsers.forEach(u -> emails.add(u.getEmail()));

        if (emails.isEmpty()) {
            return;
        }

        notificationService.sendEmail(
                NotificationType.LOW_STOCK_ALERT,
                emails,
                java.util.Map.of(
                        "productName", productSize.getProduct().getName() + " (Size: " + productSize.getSize().getName() + ")",
                        "stock", stock
                )
        );
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
