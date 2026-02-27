package com.zyna.dev.ecommerce.products.controller;

import com.zyna.dev.ecommerce.common.ApiResponse;
import com.zyna.dev.ecommerce.products.dto.request.CategoryCreateRequest;
import com.zyna.dev.ecommerce.products.dto.request.CategoryUpdateRequest;
import com.zyna.dev.ecommerce.products.dto.response.CategoryResponse;
import com.zyna.dev.ecommerce.products.service.interfaces.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // PUBLIC: lấy danh sách category active để filter product (hoặc ADMIN lấy tất cả)
    @GetMapping
    public ApiResponse<java.util.List<CategoryResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "true") boolean activeOnly,
            @RequestParam(required = false) String keyword
    ) {
        Page<CategoryResponse> data = categoryService.list(page, size, activeOnly, keyword);
        return ApiResponse.successfulPageResponse(
                HttpStatus.OK.value(),
                "Lấy danh sách danh mục thành công",
                data
        );
    }

    // ADMIN: tạo category
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    public ApiResponse<CategoryResponse> create(
            @Valid @RequestBody CategoryCreateRequest request
    ) {
        CategoryResponse data = categoryService.create(request);
        return ApiResponse.successfulResponse(
                HttpStatus.CREATED.value(),
                "Tạo danh mục thành công",
                data
        );
    }

    // ADMIN: update category
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    public ApiResponse<CategoryResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CategoryUpdateRequest request
    ) {
        CategoryResponse data = categoryService.update(id, request);
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Cập nhật danh mục thành công",
                data
        );
    }

    // ADMIN: soft delete category
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ApiResponse.successfulResponseNoData(
                HttpStatus.OK.value(),
                "Xóa danh mục thành công"
        );
    }

    // GET chi tiết 1 category
    @GetMapping("/{id}")
    public ApiResponse<CategoryResponse> getById(@PathVariable Long id) {
        CategoryResponse data = categoryService.getById(id);
        return ApiResponse.successfulResponse(
                "Lấy thông tin danh mục thành công",
                data
        );
    }
}
