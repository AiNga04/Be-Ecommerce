package com.zyna.dev.ecommerce.products.mappers;

import com.zyna.dev.ecommerce.products.dto.request.CategoryCreateRequest;
import com.zyna.dev.ecommerce.products.dto.request.CategoryUpdateRequest;
import com.zyna.dev.ecommerce.products.dto.response.CategoryResponse;
import com.zyna.dev.ecommerce.products.models.Category;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CategoryMapper {

    // Convert CreateRequest → Entity
    public Category toEntity(CategoryCreateRequest dto) {
        return Category.builder()
                .code(dto.getCode().trim())
                .name(dto.getName().trim())
                .description(dto.getDescription())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // Áp dụng UpdateRequest → Entity đã có
    public void applyUpdate(Category category, CategoryUpdateRequest dto) {
        if (dto.getName() != null) {
            category.setName(dto.getName().trim());
        }
        if (dto.getDescription() != null) {
            category.setDescription(dto.getDescription());
        }
        if (dto.getIsActive() != null) {
            category.setIsActive(dto.getIsActive());
        }
        category.setUpdatedAt(LocalDateTime.now());
    }

    // Entity → Response DTO
    public CategoryResponse toResponse(Category entity) {
        return CategoryResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
