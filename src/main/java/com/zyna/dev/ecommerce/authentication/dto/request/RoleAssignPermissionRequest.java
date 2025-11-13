package com.zyna.dev.ecommerce.authentication.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleAssignPermissionRequest {

    @NotEmpty(message = "Permission IDs cannot be empty")
    private Set<Long> permissionIds;
}
