package com.zyna.dev.ecommerce.carts.models;

import com.zyna.dev.ecommerce.products.models.Product;
import com.zyna.dev.ecommerce.products.models.Size;
import com.zyna.dev.ecommerce.users.models.User;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "cart_items",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "product_id", "size_id"})
)
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // chủ sở hữu giỏ hàng
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // sản phẩm
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "size_id")
    private Size size;

    // số lượng trong giỏ
    @Column(nullable = false)
    private Integer quantity;
}
