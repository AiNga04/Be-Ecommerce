package com.zyna.dev.ecommerce.products.criteria;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCriteria {
    private String name;
    private java.util.List<String> category;

    @DecimalMin(value = "0.0", message = "Min price must be >= 0")
    private BigDecimal minPrice;

    @DecimalMin(value = "0.0", message = "Max price must be >= 0")
    private BigDecimal maxPrice;

    @Min(value = 0, message = "Stock must be >= 0")
    private Integer minStock;

    private Boolean isActive;
}
