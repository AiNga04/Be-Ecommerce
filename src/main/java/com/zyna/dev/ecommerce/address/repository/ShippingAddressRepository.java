package com.zyna.dev.ecommerce.address.repository;

import com.zyna.dev.ecommerce.address.models.ShippingAddress;
import com.zyna.dev.ecommerce.users.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShippingAddressRepository extends JpaRepository<ShippingAddress, Long> {

    List<ShippingAddress> findByUserOrderByIsDefaultDescCreatedAtDesc(User user);

    Optional<ShippingAddress> findByIdAndUser(Long id, User user);

    Optional<ShippingAddress> findByUserAndIsDefaultTrue(User user);
}
