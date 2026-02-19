package com.zyna.dev.ecommerce.products.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "categories",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_category_code", columnNames = "code")
        }
)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Mã category dạng code, dùng cho filter & URL, ví dụ:
     * "phone", "laptop", "accessories"
     */
    @Column(nullable = false, length = 100)
    private String code;

    /**
     * Tên hiển thị cho user, ví dụ:
     * "Điện thoại", "Laptop", "Phụ kiện"
     */
    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @org.hibernate.annotations.Formula("(SELECT count(*) FROM products p WHERE p.category_id = id)")
    private Long productCount;
}
