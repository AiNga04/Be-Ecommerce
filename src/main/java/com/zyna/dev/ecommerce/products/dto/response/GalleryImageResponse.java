package com.zyna.dev.ecommerce.products.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GalleryImageResponse {
    private Long id;
    private String url;
}
