package com.zyna.dev.ecommerce.carts.service.interfaces;

import com.zyna.dev.ecommerce.carts.dto.request.AddToCartRequest;
import com.zyna.dev.ecommerce.carts.dto.request.RemoveCartItemsRequest;
import com.zyna.dev.ecommerce.carts.dto.request.UpdateCartItemRequest;
import com.zyna.dev.ecommerce.carts.dto.response.CartItemResponse;

import java.util.List;

public interface CartService {
    List<CartItemResponse> getMyCart(Long userId);

    CartItemResponse addToCart(Long userId, AddToCartRequest request);

    CartItemResponse updateCartItem(Long userId, Long cartItemId, UpdateCartItemRequest request);

    void removeCartItem(Long userId, Long cartItemId);

    void removeCartItems(Long userId, RemoveCartItemsRequest request);

    void clearCart(Long userId);
}
