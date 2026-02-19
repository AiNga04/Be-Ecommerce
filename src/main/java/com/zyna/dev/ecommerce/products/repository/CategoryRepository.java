package com.zyna.dev.ecommerce.products.repository;

import com.zyna.dev.ecommerce.products.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByCode(String code);

    Optional<Category> findByCodeAndIsActiveTrue(String code);

    org.springframework.data.domain.Page<Category> findAllByIsActiveTrue(org.springframework.data.domain.Pageable pageable);
}
