package com.zyna.dev.ecommerce.products.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryUpdateRequest {

    private String name;
    private String description;
    private Boolean isActive;
}