package com.zyna.dev.ecommerce.products.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SizeGuideRequest {
    private String name;
    private String description;
    private String imageUrl;
}
