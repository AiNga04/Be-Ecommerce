package com.zyna.dev.ecommerce.products.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SizeResponse {
    private Long id;
    private String name;
    private String code;
    private String description;
}
