package com.zyna.dev.ecommerce.shipping.models;

import com.zyna.dev.ecommerce.common.enums.ShipmentStatus;
import com.zyna.dev.ecommerce.orders.models.Order;
import com.zyna.dev.ecommerce.users.models.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "shipments")
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ---- Mối quan hệ với Order ----
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    // ---- Shipper (User có role SHIPPER) ----
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipper_id")
    private User shipper;

    // ---- Trạng thái giao hàng ----
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShipmentStatus status;

    // ---- Carrier & Tracking ----
    @Column(length = 100)
    private String carrier;

    @Column(length = 100)
    private String trackingCode;

    @Column(columnDefinition = "TEXT")
    private String note;

    // ---- Số lần giao thất bại ----
    @Column(nullable = false)
    @Builder.Default
    private Integer attempts = 0;

    // ---- Cờ yêu cầu trả hàng từ user ----
    @Column(nullable = false)
    @Builder.Default
    private boolean returnRequested = false;

    // ---- Timestamp ----
    private LocalDateTime assignedAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime failedAt;
    private LocalDateTime returnedAt;
}
