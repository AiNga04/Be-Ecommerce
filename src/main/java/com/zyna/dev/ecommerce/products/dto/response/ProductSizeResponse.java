package com.zyna.dev.ecommerce.products.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ProductSizeResponse {
    private Long id; // product_size id
    private Long sizeId;
    private String sizeName;
    private String sizeCode;
    private Integer quantity;
}
