package com.zyna.dev.ecommerce.carts.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class CartItemResponse {
    private Long id;

    private Long productId;
    private String productName;
    private BigDecimal productPrice;

    private Integer quantity;
    private BigDecimal subtotal;
}
