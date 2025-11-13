package com.zyna.dev.ecommerce.authentication.service.impl;

import com.zyna.dev.ecommerce.authentication.dto.request.PermissionRequest;
import com.zyna.dev.ecommerce.authentication.dto.response.PermissionResponse;
import com.zyna.dev.ecommerce.authentication.mapper.PermissionMapper;
import com.zyna.dev.ecommerce.authentication.models.Permission;
import com.zyna.dev.ecommerce.authentication.repository.PermissionRepository;
import com.zyna.dev.ecommerce.authentication.service.interfaces.PermissionService;
import com.zyna.dev.ecommerce.common.exceptions.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    @Override
    public PermissionResponse create(PermissionRequest request) {
        if (permissionRepository.existsByName(request.getName())) {
            throw new ApplicationException(HttpStatus.CONFLICT, "Permission name already exists!");
        }
        Permission entity = permissionMapper.toEntity(request);
        Permission saved = permissionRepository.save(entity);
        return permissionMapper.toResponse(saved);
    }

    @Override
    public PermissionResponse update(Long id, PermissionRequest request) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Permission not found!"));

        permissionMapper.applyUpdate(permission, request);
        Permission saved = permissionRepository.save(permission);
        return permissionMapper.toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Permission not found!"));

        permissionRepository.delete(permission);
    }

    @Override
    public PermissionResponse getById(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Permission not found!"));

        return permissionMapper.toResponse(permission);
    }

    @Override
    public List<PermissionResponse> getAll() {
        return permissionRepository.findAll().stream()
                .map(permissionMapper::toResponse)
                .toList();
    }
}
