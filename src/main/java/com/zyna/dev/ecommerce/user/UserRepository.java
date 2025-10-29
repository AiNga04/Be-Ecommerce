package com.zyna.dev.ecommerce.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    Optional<User> findById(Long id);

    Optional<User> findByIdAndIsDeletedFalse(Long id);

    Page<User> findAllByIsDeletedFalse(Pageable pageable);
}

