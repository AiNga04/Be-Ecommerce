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
                "Tạo quyền hạn thành công",
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
                "Cập nhật quyền hạn thành công",
                result
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_MANAGE')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        permissionService.delete(id);
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Xóa quyền hạn thành công"
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_MANAGE')")
    public ApiResponse<PermissionResponse> get(@PathVariable Long id) {
        return ApiResponse.successfulResponse(
                "Lấy thông tin quyền hạn thành công",
                permissionService.getById(id)
        );
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERMISSION_MANAGE')")
    public ApiResponse<List<PermissionResponse>> getAll() {
        return ApiResponse.successfulResponse(
                "Lấy danh sách quyền hạn thành công",
                permissionService.getAll()
        );
    }
}
