package com.zyna.dev.ecommerce.products.mappers;

import com.zyna.dev.ecommerce.products.dto.request.ProductCreateRequest;
import com.zyna.dev.ecommerce.products.dto.request.ProductUpdateRequest;
import com.zyna.dev.ecommerce.products.dto.response.GalleryImageResponse;
import com.zyna.dev.ecommerce.products.dto.response.ProductResponse;
import com.zyna.dev.ecommerce.products.models.Category;
import com.zyna.dev.ecommerce.products.models.Product;
import com.zyna.dev.ecommerce.products.models.ProductImage;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;

@Component
public class ProductMapper {

    // Tạo Product từ DTO tạo mới + Category đã resolve sẵn
    public Product createToProduct(ProductCreateRequest dto, Category category) {
        return Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .imageUrl(dto.getImageUrl())
                .category(category)         // ✅
                .stock(dto.getStock())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // Cập nhật các trường không null từ DTO, cho phép đổi category
    public void applyUpdate(Product target, ProductUpdateRequest dto, Category category) {
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
        if (category != null) {
            target.setCategory(category);
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
        String categoryName = null;
        if (entity.getCategory() != null) {
            categoryName = entity.getCategory().getName(); // hoặc getCode()
        }

        return ProductResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .imageUrl(entity.getImageUrl())
                .category(categoryName)   // ✅ giờ là name/code từ Category
                .stock(entity.getStock())
                .isActive(entity.getIsActive())
                .ratingAverage(entity.getRatingAverage())
                .reviewCount(entity.getReviewCount())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    // Chuyển entity sang response DTO
    public ProductResponse toProductResponseDetail(Product entity) {
        String categoryName = null;
        if (entity.getCategory() != null) {
            categoryName = entity.getCategory().getName(); // hoặc getCode()
        }

        return ProductResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .imageUrl(entity.getImageUrl())
                .category(categoryName)   // ✅ giờ là name/code từ Category
                .stock(entity.getStock())
                .isActive(entity.getIsActive())
                .ratingAverage(entity.getRatingAverage())
                .reviewCount(entity.getReviewCount())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .gallery(
                        entity.getGallery() == null
                                ? Collections.emptyList()
                                : entity.getGallery().stream()
                                .map(img -> GalleryImageResponse.builder()
                                        .id(img.getId())
                                        .url(img.getImageUrl())
                                        .build())
                                .toList()
                )
                .build();
    }
}
