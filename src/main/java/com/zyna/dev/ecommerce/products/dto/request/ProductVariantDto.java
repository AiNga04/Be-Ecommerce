package com.zyna.dev.ecommerce.products.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariantDto {
    @NotNull(message = "Size ID is required")
    private Long sizeId;

    @Min(value = 0, message = "Quantity must be >= 0")
    private Integer quantity;
}
