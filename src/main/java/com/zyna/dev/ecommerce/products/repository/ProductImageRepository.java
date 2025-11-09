package com.zyna.dev.ecommerce.products.repository;

import com.zyna.dev.ecommerce.products.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findAllByProductId(Long productId);
    void deleteAllByProductId(Long productId);
}
