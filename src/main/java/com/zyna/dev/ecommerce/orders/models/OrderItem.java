package com.zyna.dev.ecommerce.orders.models;

import com.zyna.dev.ecommerce.products.models.Product;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // order
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // sản phẩm
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // số lượng
    @Column(nullable = false)
    private Integer quantity;

    // giá tại thời điểm mua
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    // thành tiền
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column
    private String size;

    @Column
    private String color;
}
