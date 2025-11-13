package com.zyna.dev.ecommerce.authentication.service.interfaces;

import com.zyna.dev.ecommerce.authentication.dto.request.RoleAssignPermissionRequest;
import com.zyna.dev.ecommerce.authentication.dto.request.RoleRequest;
import com.zyna.dev.ecommerce.authentication.dto.response.RoleResponse;

import java.util.List;

public interface RoleService {
    RoleResponse create(RoleRequest request);
    RoleResponse update(Long id, RoleRequest request);
    void delete(Long id);
    RoleResponse getById(Long id);
    List<RoleResponse> getAll();
    RoleResponse assignPermissions(Long roleId, RoleAssignPermissionRequest request);
}
