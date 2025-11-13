package com.zyna.dev.ecommerce.authentication.controller;

import com.zyna.dev.ecommerce.authentication.dto.request.RoleAssignPermissionRequest;
import com.zyna.dev.ecommerce.authentication.dto.request.RoleRequest;
import com.zyna.dev.ecommerce.authentication.dto.response.RoleResponse;
import com.zyna.dev.ecommerce.authentication.service.interfaces.RoleService;
import com.zyna.dev.ecommerce.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RoleResponse> create(@Valid @RequestBody RoleRequest request) {
        RoleResponse result = roleService.create(request);
        return ApiResponse.successfulResponse(
                HttpStatus.CREATED.value(),
                "Role created successfully!",
                result
        );
    }

    @PutMapping("/{id}")
    public ApiResponse<RoleResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody RoleRequest request
    ) {
        RoleResponse result = roleService.update(id, request);
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Role updated successfully!",
                result
        );
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Role deleted successfully!"
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<RoleResponse> get(@PathVariable Long id) {
        return ApiResponse.successfulResponse(
                "Fetched role successfully!",
                roleService.getById(id)
        );
    }

    @GetMapping
    public ApiResponse<List<RoleResponse>> getAll() {
        return ApiResponse.successfulResponse(
                "Fetched role list!",
                roleService.getAll()
        );
    }

    @PostMapping("/{id}/permissions")
    public ApiResponse<RoleResponse> assignPermissions(
            @PathVariable Long id,
            @Valid @RequestBody RoleAssignPermissionRequest request
    ) {
        RoleResponse result = roleService.assignPermissions(id, request);
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Assigned permissions to role successfully!",
                result
        );
    }
}

