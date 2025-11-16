package com.zyna.dev.ecommerce.orders;

import com.zyna.dev.ecommerce.common.ApiResponse;
import com.zyna.dev.ecommerce.orders.dto.request.CheckoutRequest;
import com.zyna.dev.ecommerce.orders.dto.response.OrderResponse;
import com.zyna.dev.ecommerce.orders.service.interfaces.OrderService;
import com.zyna.dev.ecommerce.users.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;

    private Long getCurrentUserId(Authentication authentication) {
        String email = (String) authentication.getPrincipal();
        return userRepository.findByEmail(email)
                .orElseThrow()
                .getId();
    }

    // CHECKOUT
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
                "Checkout successfully!",
                response
        );
    }

    // LỊCH SỬ ĐƠN HÀNG CỦA CHÍNH MÌNH
    @GetMapping("/my")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ORDER_READ')")
    public ApiResponse<Page<OrderResponse>> getMyOrders(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = getCurrentUserId(authentication);
        Page<OrderResponse> result = orderService.getMyOrders(userId, page, size);
        return ApiResponse.successfulResponse(
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
}
