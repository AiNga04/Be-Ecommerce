package com.zyna.dev.ecommerce.authentication.controller;

import com.zyna.dev.ecommerce.authentication.dto.request.RoleAssignPermissionRequest;
import com.zyna.dev.ecommerce.authentication.dto.request.RoleRequest;
import com.zyna.dev.ecommerce.authentication.dto.response.RoleResponse;
import com.zyna.dev.ecommerce.authentication.service.interfaces.RoleService;
import com.zyna.dev.ecommerce.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ApiResponse<RoleResponse> create(@Valid @RequestBody RoleRequest request) {
        RoleResponse result = roleService.create(request);
        return ApiResponse.successfulResponse(
                HttpStatus.CREATED.value(),
                "Tạo vai trò thành công",
                result
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ApiResponse<RoleResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody RoleRequest request
    ) {
        RoleResponse result = roleService.update(id, request);
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Cập nhật vai trò thành công",
                result
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Xóa vai trò thành công"        
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ApiResponse<RoleResponse> get(@PathVariable Long id) {
        return ApiResponse.successfulResponse(
                "Lấy thông tin vai trò thành công",
                roleService.getById(id)
        );
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ApiResponse<List<RoleResponse>> getAll() {
        return ApiResponse.successfulResponse(
                "Lấy danh sách vai trò thành công",
                roleService.getAll()
        );
    }

    @PostMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ApiResponse<RoleResponse> assignPermissions(
            @PathVariable Long id,
            @Valid @RequestBody RoleAssignPermissionRequest request
    ) {
        RoleResponse result = roleService.assignPermissions(id, request);
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Gán quyền hạn cho vai trò thành công",
                result
        );
    }
}
