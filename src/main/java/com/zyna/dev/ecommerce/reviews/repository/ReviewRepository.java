package com.zyna.dev.ecommerce.reviews.repository;

import com.zyna.dev.ecommerce.reviews.models.Review;
import com.zyna.dev.ecommerce.products.models.Product;
import com.zyna.dev.ecommerce.users.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByUserAndProduct(User user, Product product);

    Page<Review> findByProductAndHiddenFalse(Product product, Pageable pageable);

    Page<Review> findByHiddenFalse(Pageable pageable);

    Page<Review> findByHiddenTrue(Pageable pageable);

    @Query("select r from Review r where r.reporters is not empty")
    Page<Review> findReported(Pageable pageable);

    @Query("""
            select coalesce(avg(r.rating), 0) from Review r
            where r.product.id = :productId and r.hidden = false
            """)
    Double findAverageRating(@Param("productId") Long productId);

    @Query("""
            select count(r) from Review r
            where r.product.id = :productId and r.hidden = false
            """)
    Long countActiveReviews(@Param("productId") Long productId);
}
