package com.zyna.dev.ecommerce.products.service.impl;

import com.zyna.dev.ecommerce.common.exceptions.ApplicationException;
import com.zyna.dev.ecommerce.products.dto.request.CategoryCreateRequest;
import com.zyna.dev.ecommerce.products.dto.request.CategoryUpdateRequest;
import com.zyna.dev.ecommerce.products.dto.response.CategoryResponse;
import com.zyna.dev.ecommerce.products.mappers.CategoryMapper;
import com.zyna.dev.ecommerce.products.models.Category;
import com.zyna.dev.ecommerce.products.repository.CategoryRepository;
import com.zyna.dev.ecommerce.products.repository.ProductRepository;
import com.zyna.dev.ecommerce.products.service.interfaces.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryResponse create(CategoryCreateRequest request) {
        if (categoryRepository.existsByCode(request.getCode())) {
            throw new ApplicationException(HttpStatus.CONFLICT, "Category code already exists!");
        }

        // mapper: DTO -> Entity
        Category category = categoryMapper.toEntity(request);
        Category saved = categoryRepository.save(category);

        // mapper: Entity -> Response
        return categoryMapper.toResponse(saved);
    }

    @Override
    public CategoryResponse update(Long id, CategoryUpdateRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Category not found!"));

        // mapper: apply update DTO -> Entity
        categoryMapper.applyUpdate(category, request);

        Category saved = categoryRepository.save(category);
        return categoryMapper.toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Category not found!"));
                
        // Check if category has products
        if (productRepository.existsByCategoryId(id)) {
            throw new ApplicationException(HttpStatus.CONFLICT, "Cannot delete category containing products. Please move or delete products first.");
        }

        // soft delete: inactive
        category.setIsActive(false);
        categoryRepository.save(category);
    }

    @Override
    public CategoryResponse getById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Category not found!"));

        return categoryMapper.toResponse(category);
    }

    @Override
    public Page<CategoryResponse> list(int page, int size, boolean onlyActive, String keyword) {
        Page<Category> pageData;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            pageData = categoryRepository.search(keyword.trim(), onlyActive, pageable);
        } else if (onlyActive) {
            pageData = categoryRepository.findAllByIsActiveTrue(pageable);
        } else {
            pageData = categoryRepository.findAll(pageable);
        }

        // map từng entity -> response bằng mapper
        return pageData.map(categoryMapper::toResponse);
    }
}
