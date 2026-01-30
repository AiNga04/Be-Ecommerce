package com.zyna.dev.ecommerce.carts;

import com.zyna.dev.ecommerce.carts.dto.response.CartItemResponse;
import com.zyna.dev.ecommerce.carts.models.CartItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class CartMapper {

    public CartItemResponse toCartItemResponse(CartItem item) {
        BigDecimal unitPrice = item.getProduct().getPrice();
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

        return CartItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .productPrice(unitPrice)
                .quantity(item.getQuantity())
                .subtotal(subtotal)
                .sizeId(item.getSize() != null ? item.getSize().getId() : null)
                .sizeName(item.getSize() != null ? item.getSize().getName() : null)
                .image(item.getProduct().getImageUrl())
                .build();
    }

    public List<CartItemResponse> toCartItemResponses(List<CartItem> items) {
        return items.stream()
                .map(this::toCartItemResponse)
                .toList();
    }
}
