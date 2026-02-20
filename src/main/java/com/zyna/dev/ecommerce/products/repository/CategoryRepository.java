package com.zyna.dev.ecommerce.products.repository;

import com.zyna.dev.ecommerce.products.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByCode(String code);

    Optional<Category> findByCodeAndIsActiveTrue(String code);

    org.springframework.data.domain.Page<Category> findAllByIsActiveTrue(org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT c FROM Category c WHERE " +
            "(:keyword IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.code) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:activeOnly = false OR c.isActive = true)")
    org.springframework.data.domain.Page<Category> search(@Param("keyword") String keyword, @   Param("activeOnly") boolean activeOnly, org.springframework.data.domain.Pageable pageable);
}
