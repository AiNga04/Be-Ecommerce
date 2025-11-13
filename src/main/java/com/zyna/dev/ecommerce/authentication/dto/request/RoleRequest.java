package com.zyna.dev.ecommerce.authentication.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleRequest {

    @NotBlank(message = "Role code is required")
    @Size(max = 50, message = "Role code must be less than 50 characters")
    private String code;

    @Size(max = 255, message = "Description must be less than 255 characters")
    private String description;
}
