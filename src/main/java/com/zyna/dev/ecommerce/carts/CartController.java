package com.zyna.dev.ecommerce.carts;

import com.zyna.dev.ecommerce.carts.dto.request.AddToCartRequest;
import com.zyna.dev.ecommerce.carts.dto.request.RemoveCartItemsRequest;
import com.zyna.dev.ecommerce.carts.dto.request.UpdateCartItemRequest;
import com.zyna.dev.ecommerce.carts.dto.response.CartItemResponse;
import com.zyna.dev.ecommerce.carts.service.interfaces.CartService;
import com.zyna.dev.ecommerce.common.ApiResponse;
import com.zyna.dev.ecommerce.users.service.interfaces.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    private Long getCurrentUserId(Authentication authentication) {
        String email = (String) authentication.getPrincipal();
        return userService.getUserIdByEmail(email);
    }

    // Xem giỏ hàng của chính mình
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ORDER_WRITE')")
    public ApiResponse<List<CartItemResponse>> getMyCart(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        List<CartItemResponse> items = cartService.getMyCart(userId);
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Lấy giỏ hàng thành công",
                items
        );
    }

    // Thêm sản phẩm vào giỏ
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ORDER_WRITE')") // chỉ user mới được add cart
    public ApiResponse<CartItemResponse> addToCart(
            Authentication authentication,
            @Valid @RequestBody AddToCartRequest request
    ) {
        Long userId = getCurrentUserId(authentication);
        CartItemResponse itemResponse = cartService.addToCart(userId, request);
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Thêm vào giỏ hàng thành công",
                itemResponse
        );
    }

    // Cập nhật số lượng 1 item trong giỏ
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ORDER_WRITE')")
    public ApiResponse<CartItemResponse> updateCartItem(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {
        Long userId = getCurrentUserId(authentication);
        CartItemResponse itemResponse = cartService.updateCartItem(userId, id, request);
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Cập nhật sản phẩm trong giỏ thành công",
                itemResponse
        );
    }

    // Xoá 1 item khỏi giỏ
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ORDER_WRITE')")
    public ApiResponse<Void> removeCartItem(
            Authentication authentication,
            @PathVariable Long id
    ) {
        Long userId = getCurrentUserId(authentication);
        cartService.removeCartItem(userId, id);

        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Đã xóa sản phẩm khỏi giỏ!"
        );
    }

    // Xoá nhiều item khỏi giỏ
    @DeleteMapping("/items")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ORDER_WRITE')")
    public ApiResponse<Void> removeCartItems(
            Authentication authentication,
            @Valid @RequestBody RemoveCartItemsRequest request
    ) {
        Long userId = getCurrentUserId(authentication);
        cartService.removeCartItems(userId, request);

        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Đã xóa các sản phẩm khỏi giỏ!"
        );
    }

    // Xoá toàn bộ giỏ hàng
    @DeleteMapping("/clear")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ORDER_WRITE')")
    public ApiResponse<Void> clearCart(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        cartService.clearCart(userId);

        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Đã xóa toàn bộ giỏ hàng!"
        );
    }
}
