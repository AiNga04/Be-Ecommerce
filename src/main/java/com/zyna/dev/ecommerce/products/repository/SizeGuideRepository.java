package com.zyna.dev.ecommerce.products.repository;

import com.zyna.dev.ecommerce.products.models.SizeGuide;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SizeGuideRepository extends JpaRepository<SizeGuide, Long> {
}
