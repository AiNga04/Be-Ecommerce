package com.zyna.dev.ecommerce.products.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.math.BigDecimal;
import com.zyna.dev.ecommerce.products.dto.request.ProductVariantDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCreateRequest {
    @NotBlank(message = "Product name is required")
    private String name;

    @DecimalMin(value = "0.01", message = "Price must be > 0")
    private BigDecimal price;

    private Long sizeGuideId;

    private List<ProductVariantDto> variants;

    private String description;
    private String imageUrl;
    private String category;
}
