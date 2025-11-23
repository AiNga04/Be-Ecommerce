package com.zyna.dev.ecommerce.users.repository;

import com.zyna.dev.ecommerce.users.models.UserAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAuditLogRepository extends JpaRepository<UserAuditLog, Long> {
}
