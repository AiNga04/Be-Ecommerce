package com.zyna.dev.ecommerce.cart.repository;

import com.zyna.dev.ecommerce.cart.models.CartItem;
import com.zyna.dev.ecommerce.users.User;
import com.zyna.dev.ecommerce.products.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByUser(User user);

    Optional<CartItem> findByUserAndProduct(User user, Product product);

    void deleteByUser(User user);
}
