package com.zyna.dev.ecommerce.products.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.zyna.dev.ecommerce.products.models.Size;
import com.zyna.dev.ecommerce.products.models.SizeGuide;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "product",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProductImage> gallery = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "size_guide_id")
    private SizeGuide sizeGuide;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProductSize> productSizes = new ArrayList<>();

    // Color removed

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal ratingAverage = BigDecimal.ZERO;

    @Column
    @Builder.Default
    private Integer reviewCount = 0;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    public Integer getTotalStock() {
        if (productSizes == null || productSizes.isEmpty()) {
            return 0;
        }
        return productSizes.stream().mapToInt(ProductSize::getQuantity).sum();
    }
}
