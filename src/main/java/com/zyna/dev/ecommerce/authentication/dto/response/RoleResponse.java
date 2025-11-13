package com.zyna.dev.ecommerce.authentication.dto.response;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleResponse {

    private Long id;
    private String code;
    private String description;

    private Set<PermissionResponse> permissions;
}
