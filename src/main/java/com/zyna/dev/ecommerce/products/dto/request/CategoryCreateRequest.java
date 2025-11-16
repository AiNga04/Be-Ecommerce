package com.zyna.dev.ecommerce.products.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryCreateRequest {

    @NotBlank
    private String code;

    @NotBlank
    private String name;

    private String description;
}