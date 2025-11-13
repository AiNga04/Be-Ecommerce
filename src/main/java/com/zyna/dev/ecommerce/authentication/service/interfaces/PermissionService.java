package com.zyna.dev.ecommerce.authentication.service.interfaces;

import com.zyna.dev.ecommerce.authentication.dto.request.PermissionRequest;
import com.zyna.dev.ecommerce.authentication.dto.response.PermissionResponse;

import java.util.List;

public interface PermissionService {
    PermissionResponse create(PermissionRequest request);
    PermissionResponse update(Long id, PermissionRequest request);
    void delete(Long id);
    PermissionResponse getById(Long id);
    List<PermissionResponse> getAll();
}
