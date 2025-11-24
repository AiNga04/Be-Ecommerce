package com.zyna.dev.ecommerce.products.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import com.zyna.dev.ecommerce.products.dto.response.GalleryImageResponse;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private String category;
    private Integer stock;
    private Boolean isActive;
    private BigDecimal ratingAverage;
    private Integer reviewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<GalleryImageResponse> gallery;
}
