package com.zyna.dev.ecommerce.products.repository;

import com.zyna.dev.ecommerce.products.models.Product;
import com.zyna.dev.ecommerce.products.models.ProductSize;
import com.zyna.dev.ecommerce.products.models.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProductSizeRepository extends JpaRepository<ProductSize, Long> {
    Optional<ProductSize> findByProductAndSize(Product product, Size size);
    Optional<ProductSize> findByProductIdAndSizeId(Long productId, Long sizeId);
}
