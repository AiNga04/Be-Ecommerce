package com.zyna.dev.ecommerce.orders.repository;

import com.zyna.dev.ecommerce.common.enums.OrderStatus;
import com.zyna.dev.ecommerce.orders.models.OrderItem;
import com.zyna.dev.ecommerce.products.models.Product;
import com.zyna.dev.ecommerce.users.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("""
            select (count(oi) > 0) from OrderItem oi
            where oi.product = :product
              and oi.order.user = :user
              and oi.order.status <> :excludedStatus
            """)
    boolean existsPurchased(@Param("product") Product product,
                            @Param("user") User user,
                            @Param("excludedStatus") OrderStatus excludedStatus);

    @Query("SELECT new com.zyna.dev.ecommerce.dashboard.dto.TopProductResponse(" +
            "oi.product.id, oi.product.name, oi.product.imageUrl, " +
            "CAST(SUM(oi.quantity) AS long), SUM(oi.subtotal)) " +
            "FROM OrderItem oi " +
            "WHERE oi.order.status IN :statuses " +
            "GROUP BY oi.product.id, oi.product.name, oi.product.imageUrl " +
            "ORDER BY SUM(oi.quantity) DESC")
    java.util.List<com.zyna.dev.ecommerce.dashboard.dto.TopProductResponse> findTopSellingProducts(
            @Param("statuses") java.util.List<OrderStatus> statuses,
            org.springframework.data.domain.Pageable pageable
    );
}
