package com.zyna.dev.ecommerce.products;

import com.zyna.dev.ecommerce.products.dto.request.ProductCreateRequest;
import com.zyna.dev.ecommerce.products.dto.request.ProductUpdateRequest;
import com.zyna.dev.ecommerce.products.dto.response.ProductResponse;
import com.zyna.dev.ecommerce.products.models.Product;
import com.zyna.dev.ecommerce.products.models.ProductImage;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;

@Component
public class ProductMapper {

    // Tạo Product từ DTO tạo mới
    public Product createToProduct(ProductCreateRequest dto) {
        return Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .imageUrl(dto.getImageUrl())
                .category(dto.getCategory())
                .stock(dto.getStock())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // Cập nhật các trường không null từ DTO
    public void applyUpdate(Product target, ProductUpdateRequest dto) {
        if (dto.getName() != null) {
            target.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            target.setDescription(dto.getDescription());
        }
        if (dto.getPrice() != null) {
            target.setPrice(dto.getPrice());
        }
        if (dto.getImageUrl() != null) {
            target.setImageUrl(dto.getImageUrl());
        }
        if (dto.getCategory() != null) {
            target.setCategory(dto.getCategory());
        }
        if (dto.getStock() != null) {
            target.setStock(dto.getStock());
        }
        if (dto.getIsActive() != null) {
            target.setIsActive(dto.getIsActive());
            if (!dto.getIsActive()) {
                target.setDeletedAt(LocalDateTime.now());
            } else {
                target.setDeletedAt(null);
            }
        }
    }

    // Chuyển entity sang response DTO
    public ProductResponse toProductResponse(Product entity) {
        return ProductResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .imageUrl(entity.getImageUrl())
                .category(entity.getCategory())
                .stock(entity.getStock())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .gallery(
                        entity.getGallery() == null
                                ? Collections.emptyList()
                                : entity.getGallery().stream()
                                .map(ProductImage::getImageUrl)
                                .toList()
                )
                .build();
    }
}
