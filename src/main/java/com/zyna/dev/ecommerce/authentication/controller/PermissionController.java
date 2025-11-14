package com.zyna.dev.ecommerce.authentication.controller;

import com.zyna.dev.ecommerce.authentication.dto.request.PermissionRequest;
import com.zyna.dev.ecommerce.authentication.dto.response.PermissionResponse;
import com.zyna.dev.ecommerce.authentication.service.interfaces.PermissionService;
import com.zyna.dev.ecommerce.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('PERMISSION_MANAGE')")
    public ApiResponse<PermissionResponse> create(@Valid @RequestBody PermissionRequest request) {
        PermissionResponse result = permissionService.create(request);
        return ApiResponse.successfulResponse(
                HttpStatus.CREATED.value(),
                "Permission created successfully!",
                result
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_MANAGE')")
    public ApiResponse<PermissionResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody PermissionRequest request
    ) {
        PermissionResponse result = permissionService.update(id, request);
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Permission updated successfully!",
                result
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_MANAGE')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        permissionService.delete(id);
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Permission deleted successfully!"
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_MANAGE')")
    public ApiResponse<PermissionResponse> get(@PathVariable Long id) {
        return ApiResponse.successfulResponse(
                "Fetched permission successfully!",
                permissionService.getById(id)
        );
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERMISSION_MANAGE')")
    public ApiResponse<List<PermissionResponse>> getAll() {
        return ApiResponse.successfulResponse(
                "Fetched permission list!",
                permissionService.getAll()
        );
    }
}
