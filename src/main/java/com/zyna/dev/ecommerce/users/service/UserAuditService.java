package com.zyna.dev.ecommerce.users.service;

import com.zyna.dev.ecommerce.common.enums.UserAuditAction;
import com.zyna.dev.ecommerce.users.models.User;
import com.zyna.dev.ecommerce.users.models.UserAuditLog;
import com.zyna.dev.ecommerce.users.repository.UserAuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserAuditService {

    private final UserAuditLogRepository userAuditLogRepository;

    public void record(User user, String actorEmail, UserAuditAction action, String detail) {
        UserAuditLog log = UserAuditLog.builder()
                .user(user)
                .action(action)
                .actorEmail(StringUtils.hasText(actorEmail) ? actorEmail : "UNKNOWN")
                .detail(detail)
                .build();

        userAuditLogRepository.save(log);
    }
}
