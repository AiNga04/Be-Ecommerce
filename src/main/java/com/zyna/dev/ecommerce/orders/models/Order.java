package com.zyna.dev.ecommerce.orders.models;

import com.zyna.dev.ecommerce.common.enums.OrderStatus;
import com.zyna.dev.ecommerce.common.enums.PaymentMethod;
import com.zyna.dev.ecommerce.common.enums.PaymentStatus;
import com.zyna.dev.ecommerce.users.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // (optional) mã đơn hàng dạng string nếu sau này bạn muốn đẹp hơn: ORD-20251118-0001
    // hiện tại có thể chưa dùng, giữ nullable = true
    @Column(name = "code", length = 50, unique = true)
    private String code;

    // user đặt hàng
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // tổng tiền phải trả (đã gồm ship / discount nếu có)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    // trạng thái đơn (PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELED, ...)
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private OrderStatus status;

    // phương thức thanh toán: COD, ONLINE, ...
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private PaymentMethod paymentMethod;

    // trạng thái thanh toán: UNPAID, PENDING, PAID, FAILED, REFUNDED
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private PaymentStatus paymentStatus;

    // ================== THÔNG TIN CỔNG THANH TOÁN (MULTI-GATEWAY) ==================

    /**
     * Tên cổng thanh toán / provider:
     *  - "VNPAY"
     *  - "MOMO"
     *  - "COD" (nếu bạn muốn dùng chung cho trả tiền mặt)
     */
    @Column(length = 30)
    private String paymentProvider;

    /**
     * Số tiền thực tế đã thanh toán qua cổng (thường = totalPrice, nhưng để room:
     *  - thanh toán một phần
     *  - chênh lệch do fee / rounding
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal paidAmount;

    /**
     * Đơn vị tiền tệ, ví dụ: "VND"
     */
    @Column(length = 3)
    private String paidCurrency;

    /**
     * Mã giao dịch do cổng thanh toán trả về:
     *  - VNPay: vnp_TransactionNo
     *  - MoMo: transId hoặc orderId (tùy API)
     */
    @Column(length = 100)
    private String providerTransactionId;

    /**
     * Mã đơn phía gateway (nếu khác với id/code trong hệ thống của bạn):
     *  - VNPay: thường dùng vnp_TxnRef
     *  - MoMo: orderId / requestId
     */
    @Column(length = 100)
    private String providerOrderId;

    /**
     * Mã trạng thái / response code của gateway:
     *  - VNPay: vnp_ResponseCode (00 = success)
     *  - MoMo: resultCode (0 = success)
     */
    @Column(length = 50)
    private String providerResponseCode;

    /**
     * Có thể dùng để lưu raw JSON response của gateway (tùy chọn)
     * cho việc debug / đối soát sau này.
     */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String providerRawData;

    // ================== THÔNG TIN GIAO HÀNG ==================

    @Column(nullable = false, length = 100)
    private String shippingName;

    @Column(nullable = false, length = 20)
    private String shippingPhone;

    @Column(nullable = false, length = 255)
    private String shippingAddress;

    @Column(length = 100)
    private String shippingTrackingCode;

    @Column(length = 100)
    private String shippingCarrier;

    // ================== TIMESTAMP ==================

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime confirmedAt;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime canceledAt;

    // ================== ITEMS ==================

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    // ================== HELPER METHODS (tùy chọn) ==================

    public void markPaidByVnPay(String transactionNo,
                                String responseCode,
                                BigDecimal amount,
                                String rawData) {
        this.paymentStatus = PaymentStatus.PAID;
        this.paymentProvider = "VNPAY";
        this.providerTransactionId = transactionNo;
        this.providerResponseCode = responseCode;
        this.paidAmount = amount;
        this.paidCurrency = "VND";
        this.providerRawData = rawData;
    }

    public void markPaidByMoMo(String momoTransId,
                               String momoOrderId,
                               String resultCode,
                               BigDecimal amount,
                               String rawData) {
        this.paymentStatus = PaymentStatus.PAID;
        this.paymentProvider = "MOMO";
        this.providerTransactionId = momoTransId;
        this.providerOrderId = momoOrderId;
        this.providerResponseCode = resultCode;
        this.paidAmount = amount;
        this.paidCurrency = "VND";
        this.providerRawData = rawData;
    }

    public void markPaymentFailed(String provider, String responseCode, String rawData) {
        this.paymentStatus = PaymentStatus.FAILED;
        this.paymentProvider = provider;
        this.providerResponseCode = responseCode;
        this.providerRawData = rawData;
    }
}
