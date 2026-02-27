package com.zyna.dev.ecommerce.users.controller;

import com.zyna.dev.ecommerce.common.ApiResponse;
import com.zyna.dev.ecommerce.users.dto.response.UserAuditLogResponse;
import com.zyna.dev.ecommerce.users.service.UserAuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users/audit-logs")
@RequiredArgsConstructor
public class UserAuditLogController {

    private final UserAuditLogService userAuditLogService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('USER_READ')")
    public ApiResponse<List<UserAuditLogResponse>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<UserAuditLogResponse> logs = userAuditLogService.getAllAuditLogs(page, size);
        return ApiResponse.successfulPageResponse(
                HttpStatus.OK.value(),
                "Lấy nhật ký hoạt động người dùng thành công",
                logs
        );
    }
}
