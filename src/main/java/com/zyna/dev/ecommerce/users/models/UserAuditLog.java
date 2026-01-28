package com.zyna.dev.ecommerce.users.models;

import com.zyna.dev.ecommerce.common.enums.UserAuditAction;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_audit_logs")
public class UserAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private UserAuditAction action;

    @Column(length = 150)
    private String actorEmail;

    @Column(length = 255)
    private String detail;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
