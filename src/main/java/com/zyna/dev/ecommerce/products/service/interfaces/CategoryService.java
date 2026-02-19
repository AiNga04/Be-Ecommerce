package com.zyna.dev.ecommerce.products.service.interfaces;

import com.zyna.dev.ecommerce.products.dto.request.CategoryCreateRequest;
import com.zyna.dev.ecommerce.products.dto.request.CategoryUpdateRequest;
import com.zyna.dev.ecommerce.products.dto.response.CategoryResponse;
import org.springframework.data.domain.Page;

public interface CategoryService {

    CategoryResponse create(CategoryCreateRequest request);

    CategoryResponse update(Long id, CategoryUpdateRequest request);

    void delete(Long id);          // soft delete (isActive = false)

    CategoryResponse getById(Long id);

    Page<CategoryResponse> list(int page, int size, boolean onlyActive);

}
