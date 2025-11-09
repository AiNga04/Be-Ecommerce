package com.zyna.dev.ecommerce.products.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCreateRequest {
    @NotBlank(message = "Product name is required")
    private String name;

    @DecimalMin(value = "0.01", message = "Price must be > 0")
    private BigDecimal price;

    @Min(value = 0, message = "Stock must be >= 0")
    private Integer stock;

    private String description;
    private String imageUrl;
    private String category;
}
