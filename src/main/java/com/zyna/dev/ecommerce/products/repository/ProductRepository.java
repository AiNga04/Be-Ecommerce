package com.zyna.dev.ecommerce.products.repository;

import com.zyna.dev.ecommerce.products.models.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    Page<Product> findAllByIsActiveTrue(Pageable pageable);
    Page<Product> findAllByIsActiveFalse(Pageable pageable);
    boolean existsByName(String name);
    @Query("""
           select p from Product p 
           left join fetch p.gallery g
           where p.id = :id and p.isActive = true
           """)
    Optional<Product> findActiveByIdWithGallery(@Param("id") Long id);

    boolean existsByCategoryId(Long categoryId);
}
