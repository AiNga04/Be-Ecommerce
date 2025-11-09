package com.zyna.dev.ecommerce.auth;

import com.zyna.dev.ecommerce.users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthRepository extends JpaRepository<User, Long> {
    // dùng cho login
    Optional<User> findByEmailAndIsDeletedFalse(String email);

    // dùng cho register
    boolean existsByEmail(String email);
}
