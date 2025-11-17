package com.zyna.dev.ecommerce.vouchers.models;

import com.zyna.dev.ecommerce.common.enums.VoucherScope;
import com.zyna.dev.ecommerce.common.enums.VoucherType;
import com.zyna.dev.ecommerce.common.enums.VoucherStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "vouchers",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_voucher_code", columnNames = "code")
        }
)
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // mã voucher: FREESHIP30, SALE10...
    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private VoucherType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private VoucherScope scope;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private VoucherStatus status;   // ⭐ trạng thái DRAFT / ACTIVE / INACTIVE / EXPIRED

    @Column(nullable = false)
    private BigDecimal discountValue;

    private BigDecimal maxDiscountAmount;

    private BigDecimal minOrderValue;

    private Integer maxUsagePerUser;

    private Integer maxUsage;

    private Integer usedCount;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
