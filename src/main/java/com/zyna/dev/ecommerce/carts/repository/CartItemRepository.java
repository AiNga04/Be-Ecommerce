package com.zyna.dev.ecommerce.carts.repository;

import com.zyna.dev.ecommerce.carts.models.CartItem;
import com.zyna.dev.ecommerce.users.models.User;
import com.zyna.dev.ecommerce.products.models.Product;
import com.zyna.dev.ecommerce.products.models.Size;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByUser(User user);

    Optional<CartItem> findByUserAndProduct(User user, Product product);

    Optional<CartItem> findByUserAndProductAndSize(User user, Product product, Size size);

    void deleteByUser(User user);
}
