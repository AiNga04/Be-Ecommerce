package com.zyna.dev.ecommerce.orders.repository;

import com.zyna.dev.ecommerce.orders.models.Order;
import com.zyna.dev.ecommerce.users.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    Optional<Order> findByCode(String code);
}
