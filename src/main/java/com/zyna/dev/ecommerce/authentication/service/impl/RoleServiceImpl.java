// com/zyna/dev/ecommerce/authentication/service/impl/RoleServiceImpl.java
package com.zyna.dev.ecommerce.authentication.service.impl;

import com.zyna.dev.ecommerce.authentication.dto.request.RoleAssignPermissionRequest;
import com.zyna.dev.ecommerce.authentication.dto.request.RoleRequest;
import com.zyna.dev.ecommerce.authentication.dto.response.RoleResponse;
import com.zyna.dev.ecommerce.authentication.mapper.RoleMapper;
import com.zyna.dev.ecommerce.authentication.models.AppRole;
import com.zyna.dev.ecommerce.authentication.models.Permission;
import com.zyna.dev.ecommerce.authentication.repository.AppRoleRepository;
import com.zyna.dev.ecommerce.authentication.repository.PermissionRepository;
import com.zyna.dev.ecommerce.authentication.service.interfaces.RoleService;
import com.zyna.dev.ecommerce.common.exceptions.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final AppRoleRepository appRoleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleMapper roleMapper;

    @Override
    public RoleResponse create(RoleRequest request) {
        if (appRoleRepository.existsByCode(request.getCode())) {
            throw new ApplicationException(HttpStatus.CONFLICT, "Role code already exists!");
        }

        AppRole entity = roleMapper.toEntity(request);
        AppRole saved = appRoleRepository.save(entity);
        return roleMapper.toResponse(saved);
    }

    @Override
    public RoleResponse update(Long id, RoleRequest request) {
        AppRole role = appRoleRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Role not found!"));

        roleMapper.applyUpdate(role, request);
        AppRole saved = appRoleRepository.save(role);
        return roleMapper.toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        AppRole role = appRoleRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Role not found!"));
        appRoleRepository.delete(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getAll() {
        return appRoleRepository.findAll().stream()
                .map(roleMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse getById(Long id) {
        AppRole role = appRoleRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Role not found!"));
        return roleMapper.toResponse(role);
    }

    @Override
    @Transactional
    public RoleResponse assignPermissions(Long roleId, RoleAssignPermissionRequest request) {
        AppRole role = appRoleRepository.findById(roleId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Role not found!"));

        List<Permission> permissions = permissionRepository.findAllById(request.getPermissionIds());
        if (permissions.isEmpty()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "No valid permissions found for given IDs!");
        }

        role.setPermissions(new HashSet<>(permissions));
        AppRole saved = appRoleRepository.save(role);

        return roleMapper.toResponse(saved);
    }
}
