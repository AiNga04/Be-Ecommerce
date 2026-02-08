package com.zyna.dev.ecommerce.users.service.impl;

import com.zyna.dev.ecommerce.users.dto.response.UserAuditLogResponse;
import com.zyna.dev.ecommerce.users.models.UserAuditLog;
import com.zyna.dev.ecommerce.users.repository.UserAuditLogRepository;
import com.zyna.dev.ecommerce.users.service.UserAuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserAuditLogServiceImpl implements UserAuditLogService {

    private final UserAuditLogRepository userAuditLogRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<UserAuditLogResponse> getAllAuditLogs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<UserAuditLog> logs = userAuditLogRepository.findAll(pageable);
        return logs.map(this::mapToResponse);
    }

    private UserAuditLogResponse mapToResponse(UserAuditLog log) {
        return UserAuditLogResponse.builder()
                .id(log.getId())
                .userId(log.getUser().getId())
                .userEmail(log.getUser().getEmail())
                .action(log.getAction())
                .actorEmail(log.getActorEmail())
                .detail(log.getDetail())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
