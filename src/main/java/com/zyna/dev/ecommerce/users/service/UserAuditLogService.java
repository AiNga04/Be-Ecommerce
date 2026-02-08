package com.zyna.dev.ecommerce.users.service;

import com.zyna.dev.ecommerce.users.dto.response.UserAuditLogResponse;
import org.springframework.data.domain.Page;

public interface UserAuditLogService {
    Page<UserAuditLogResponse> getAllAuditLogs(int page, int size);
}
