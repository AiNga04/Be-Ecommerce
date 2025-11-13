package com.zyna.dev.ecommerce.authentication.mapper;

import com.zyna.dev.ecommerce.authentication.dto.request.PermissionRequest;
import com.zyna.dev.ecommerce.authentication.dto.response.PermissionResponse;
import com.zyna.dev.ecommerce.authentication.models.Permission;
import org.springframework.stereotype.Component;

@Component
public class PermissionMapper {

    public Permission toEntity(PermissionRequest dto) {
        return Permission.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .build();
    }

    public void applyUpdate(Permission entity, PermissionRequest dto) {
        if (dto.getName() != null) entity.setName(dto.getName());
        if (dto.getDescription() != null) entity.setDescription(dto.getDescription());
    }

    public PermissionResponse toResponse(Permission entity) {
        return PermissionResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .build();
    }
}
