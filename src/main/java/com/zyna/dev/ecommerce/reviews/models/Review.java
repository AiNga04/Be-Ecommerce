package com.zyna.dev.ecommerce.reviews.models;

import com.zyna.dev.ecommerce.orders.models.Order;
import com.zyna.dev.ecommerce.products.models.Product;
import com.zyna.dev.ecommerce.users.models.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "reviews", uniqueConstraints = {
        @UniqueConstraint(name = "uk_review_user_product", columnNames = {"user_id", "product_id"})
})
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order; // verified purchase source

    @Column(nullable = false)
    private Integer rating; // 1 - 5

    @Column(columnDefinition = "TEXT")
    private String content;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReviewImage> images = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "review_reports",
            joinColumns = @JoinColumn(name = "review_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"),
            uniqueConstraints = @UniqueConstraint(name = "uk_review_report_user", columnNames = {"review_id", "user_id"})
    )
    @Builder.Default
    private Set<User> reporters = new HashSet<>();

    @Column(nullable = false)
    @Builder.Default
    private Boolean hidden = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
