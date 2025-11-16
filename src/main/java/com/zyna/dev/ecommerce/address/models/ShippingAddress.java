package com.zyna.dev.ecommerce.address.models;

import com.zyna.dev.ecommerce.users.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "shipping_addresses")
public class ShippingAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // chủ sở hữu địa chỉ
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Tên người nhận
    @Column(nullable = false, length = 100)
    private String receiverName;

    // SĐT
    @Column(nullable = false, length = 20)
    private String receiverPhone;

    // Có thể tách nhỏ như Shopee (tùy bạn muốn đi xa đến đâu)
    @Column(nullable = false, length = 255)
    private String fullAddress;  // "Dĩ An, Bình Dương"

    @Column(length = 100)
    private String province;

    @Column(length = 100)
    private String district;

    @Column(length = 100)
    private String ward;

    @Column(length = 255)
    private String detailAddress; // Số nhà, hẻm, toà, lầu...

    // Có phải địa chỉ mặc định?
    @Column(nullable = false)
    private boolean isDefault;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
