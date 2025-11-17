package com.zyna.dev.ecommerce.vouchers.models;

import com.zyna.dev.ecommerce.common.enums.VoucherScope;
import com.zyna.dev.ecommerce.common.enums.VoucherType;
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

    // mã voucher: FREESHIP30, SALE10, SALE100K...
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
    private VoucherScope scope = VoucherScope.GLOBAL;

    /**
     * PERCENTAGE: discountValue = % (vd: 10 = 10%)
     * FIXED_AMOUNT: discountValue = số tiền giảm
     * FREESHIP: discountValue = mức tối đa giảm phí ship (optional)
     */
    @Column(nullable = false)
    private BigDecimal discountValue;

    /**
     * Mức giảm tối đa (áp dụng cho PERCENTAGE)
     */
    private BigDecimal maxDiscountAmount;

    /**
     * Đơn tối thiểu để áp dụng
     */
    private BigDecimal minOrderValue;

    /**
     * Tối đa 1 user được dùng bao nhiêu lần (tương lai)
     */
    private Integer maxUsagePerUser;

    /**
     * Số lần tổng tối đa toàn hệ thống có thể dùng
     */
    private Integer maxUsage;

    /**
     * Đã dùng bao nhiêu lần
     */
    private Integer usedCount;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @Column(nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
