package com.zyna.dev.ecommerce.users.dto.response;

import com.zyna.dev.ecommerce.common.enums.UserAuditAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAuditLogResponse {
    private Long id;
    private Long userId;
    private String userEmail;
    private UserAuditAction action;
    private String actorEmail;
    private String detail;
    private LocalDateTime createdAt;
}
