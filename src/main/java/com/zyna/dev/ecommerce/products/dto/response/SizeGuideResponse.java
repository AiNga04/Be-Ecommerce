package com.zyna.dev.ecommerce.products.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SizeGuideResponse {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
}
