package com.zyna.dev.ecommerce.inventory.models;

import com.zyna.dev.ecommerce.products.models.Product;
import com.zyna.dev.ecommerce.users.User;
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
@Table(name = "inventory_audit_logs")
public class InventoryAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_user_id")
    private User changedBy;

    @Column(nullable = false)
    private Integer oldStock;

    @Column(nullable = false)
    private Integer newStock;

    @Column(length = 255)
    private String reason;

    @CreationTimestamp
    private LocalDateTime changedAt;
}
