package com.zyna.dev.ecommerce.products.repository;

import com.zyna.dev.ecommerce.products.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {
    List<PriceHistory> findByProductIdOrderByChangedAtDesc(Long productId);
}
