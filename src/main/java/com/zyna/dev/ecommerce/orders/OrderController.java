package com.zyna.dev.ecommerce.orders;

import com.zyna.dev.ecommerce.common.ApiResponse;
import com.zyna.dev.ecommerce.common.enums.OrderStatus;
import com.zyna.dev.ecommerce.common.enums.PaymentStatus;
import com.zyna.dev.ecommerce.common.enums.ShipmentStatus;
import com.zyna.dev.ecommerce.orders.dto.request.CheckoutFromCartRequest;
import com.zyna.dev.ecommerce.orders.dto.request.CheckoutRequest;
import com.zyna.dev.ecommerce.orders.dto.request.UpdateOrderStatusRequest;
import com.zyna.dev.ecommerce.orders.dto.response.OrderResponse;
import com.zyna.dev.ecommerce.orders.service.interfaces.OrderService;
import com.zyna.dev.ecommerce.users.service.interfaces.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    private Long getCurrentUserId(Authentication authentication) {
        String email = (String) authentication.getPrincipal();
        return userService.getUserIdByEmail(email);
    }

    // USER CHECKOUT TỪ LIST ITEM (không qua giỏ cũng được)
    @PostMapping("/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ORDER_WRITE')")
    public ApiResponse<OrderResponse> checkout(
            Authentication authentication,
            @Valid @RequestBody CheckoutRequest request
    ) {
        Long userId = getCurrentUserId(authentication);
        OrderResponse response = orderService.checkout(userId, request);
        return ApiResponse.successfulResponse(
                HttpStatus.CREATED.value(),
                "Order " + displayCode(response) + " created successfully!",
                response
        );
    }

    // USER CHECKOUT TỪ GIỎ HÀNG
    @PostMapping("/checkout/carts")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ORDER_WRITE')")
    public ApiResponse<OrderResponse> checkoutFromCart(
            Authentication authentication,
            @Valid @RequestBody CheckoutFromCartRequest request
    ) {
        Long userId = getCurrentUserId(authentication);
        OrderResponse response = orderService.checkoutFromCart(userId, request);
        return ApiResponse.successfulResponse(
                HttpStatus.CREATED.value(),
                "Order " + displayCode(response) + " created successfully!",
                response
        );
    }

    // USER CHECKOUT TẤT CẢ GIỎ HÀNG
    @PostMapping("/checkout/carts/all")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ORDER_WRITE')")
    public ApiResponse<OrderResponse> checkoutAllFromCart(
            Authentication authentication,
            @Valid @RequestBody CheckoutFromCartRequest request
    ) {
        Long userId = getCurrentUserId(authentication);
        // Force null/empty list to ensure all items are selected
        request.setCartItemIds(null);
        
        OrderResponse response = orderService.checkoutFromCart(userId, request);
        return ApiResponse.successfulResponse(
                HttpStatus.CREATED.value(),
                "Order " + displayCode(response) + " created successfully!",
                response
        );
    }

    // LỊCH SỬ ĐƠN HÀNG CỦA CHÍNH MÌNH (user)
    @GetMapping("/my")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ORDER_READ')")
    public ApiResponse<List<OrderResponse>> getMyOrders(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) PaymentStatus paymentStatus,
            @RequestParam(required = false) ShipmentStatus shipmentStatus
    ) {
        Long userId = getCurrentUserId(authentication);
        Page<OrderResponse> result = orderService.getMyOrders(userId, page, size, status, paymentStatus, shipmentStatus);
        return ApiResponse.successfulPageResponse(
                HttpStatus.OK.value(),
                "Get my orders successfully!",
                result
        );
    }

    // XEM CHI TIẾT 1 ĐƠN HÀNG CỦA CHÍNH MÌNH
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ORDER_READ')")
    public ApiResponse<OrderResponse> getOrder(
            Authentication authentication,
            @PathVariable Long id
    ) {
        Long userId = getCurrentUserId(authentication);
        OrderResponse response = orderService.getOrderByIdForUser(userId, id);
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Get order detail successfully!",
                response
        );
    }

    // USER XÁC NHẬN ĐÃ NHẬN HÀNG
    @PostMapping("/my/{id}/confirm-received")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ORDER_READ')")
    public ApiResponse<OrderResponse> confirmReceived(
            Authentication authentication,
            @PathVariable Long id
    ) {
        Long userId = getCurrentUserId(authentication);
        OrderResponse response = orderService.confirmReceived(userId, id);
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Order confirmed as received!",
                response
        );
    }

    // ================== STAFF / ADMIN – QUẢN LÝ ĐƠN ==================

    // LIST TẤT CẢ ĐƠN HÀNG (cho STAFF / ADMIN)
    @GetMapping("/admin")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ORDER_MANAGE')")
    public ApiResponse<List<OrderResponse>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<OrderResponse> result = orderService.getAllOrders(page, size);
        return ApiResponse.successfulPageResponse(
                HttpStatus.OK.value(),
                "Get all orders successfully!",
                result
        );
    }

    // XEM CHI TIẾT BẤT KỲ ĐƠN HÀNG NÀO (cho STAFF / ADMIN)
    @GetMapping("/admin/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ORDER_MANAGE')")
    public ApiResponse<OrderResponse> getOrderForAdmin(
            @PathVariable Long id
    ) {
        OrderResponse response = orderService.getOrderByIdForAdmin(id);
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Get order detail successfully!",
                response
        );
    }

    // STAFF / ADMIN ĐỔI TRẠNG THÁI ĐƠN HÀNG
    @PatchMapping("/{id}/status")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ORDER_MANAGE')") // chỉ STAFF / ADMIN
    public ApiResponse<OrderResponse> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request
    ) {
        OrderResponse response = orderService.updateOrderStatus(id, request.getStatus());
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Update order status successfully!",
                response
        );
    }

    private String displayCode(OrderResponse response) {
        if (response.getId() == null) return "";
        // ưu tiên code nếu sau này có trong response
        try {
            var codeField = response.getClass().getDeclaredMethod("getCode");
            Object code = codeField.invoke(response);
            if (code != null) {
                return code.toString();
            }
        } catch (Exception ignored) {
        }
        return response.getId().toString();
    }
}
