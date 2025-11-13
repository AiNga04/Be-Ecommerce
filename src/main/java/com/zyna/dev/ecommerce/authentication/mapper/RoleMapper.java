package com.zyna.dev.ecommerce.authentication.mapper;

import com.zyna.dev.ecommerce.authentication.dto.request.RoleRequest;
import com.zyna.dev.ecommerce.authentication.dto.response.PermissionResponse;
import com.zyna.dev.ecommerce.authentication.dto.response.RoleResponse;
import com.zyna.dev.ecommerce.authentication.models.AppRole;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class RoleMapper {

    public AppRole toEntity(RoleRequest dto) {
        return AppRole.builder()
                .code(dto.getCode())
                .description(dto.getDescription())
                .build();
    }

    public void applyUpdate(AppRole entity, RoleRequest dto) {
        if (dto.getCode() != null) entity.setCode(dto.getCode());
        if (dto.getDescription() != null) entity.setDescription(dto.getDescription());
    }

    public RoleResponse toResponse(AppRole entity) {
        return RoleResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .description(entity.getDescription())
                .permissions(entity.getPermissions().stream()
                        .map(p -> PermissionResponse.builder()
                                .id(p.getId())
                                .name(p.getName())
                                .description(p.getDescription())
                                .build())
                        .collect(Collectors.toSet()))
                .build();
    }
}
