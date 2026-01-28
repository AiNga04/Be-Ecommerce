package com.zyna.dev.ecommerce.products.repository;

import com.zyna.dev.ecommerce.products.models.Color;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ColorRepository extends JpaRepository<Color, Long> {
    Optional<Color> findByName(String name);
    Optional<Color> findByCode(String code);
}
