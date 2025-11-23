package com.zyna.dev.ecommerce.orders.repository;

import com.zyna.dev.ecommerce.common.enums.OrderStatus;
import com.zyna.dev.ecommerce.orders.models.OrderItem;
import com.zyna.dev.ecommerce.products.models.Product;
import com.zyna.dev.ecommerce.users.User;
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
}
