package com.zyna.dev.ecommerce.products.controller;

import com.zyna.dev.ecommerce.common.ApiResponse;
import com.zyna.dev.ecommerce.products.criteria.ProductCriteria;
import com.zyna.dev.ecommerce.products.dto.response.PriceHistoryResponse;
import com.zyna.dev.ecommerce.products.dto.response.ProductResponse;
import com.zyna.dev.ecommerce.products.service.interfaces.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService; // ✅ chỉ còn service

    // PUBLIC / AUTH – cho phép mọi người xem sản phẩm
    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> get(@PathVariable Long id) {
        return ApiResponse.successfulResponse(
                "Fetched product successfully!",
                productService.getProductById(id)
        );
    }

    @GetMapping
    public ApiResponse<Page<ProductResponse>> search(
            @Valid ProductCriteria criteria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.successfulResponse(
                "Fetched product list!",
                productService.searchProducts(criteria, page, size)
        );
    }

    // ---------- ADMIN: cần PRODUCT_WRITE ----------

    // CREATE
    @PostMapping(consumes = {"multipart/form-data"})
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    public ApiResponse<ProductResponse> createProduct(
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("price") Double price,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "stock", required = false) Integer stock,
            @RequestParam("image") MultipartFile image
    ) {
        ProductResponse response = productService.createProduct(
                name, description, price, category, stock, image
        );
        return ApiResponse.successfulResponse(
                HttpStatus.CREATED.value(),
                "Product created successfully!",
                response
        );
    }

    // UPDATE
    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    public ApiResponse<ProductResponse> updateProduct(
            @PathVariable Long id,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "price", required = false) Double price,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "stock", required = false) Integer stock,
            @RequestParam(value = "image", required = false) MultipartFile image
    ) {
        ProductResponse response = productService.updateProduct(
                id, name, description, price, category, stock, image
        );
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Product updated successfully!",
                response
        );
    }

    // SOFT DELETE 1 PRODUCT
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    public ApiResponse<Void> softDelete(@PathVariable Long id) {
        productService.softDeleteProduct(id);
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Product soft deleted successfully!"
        );
    }

    // RESTORE 1 PRODUCT
    @PutMapping("/{id}/restore")
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    public ApiResponse<Void> restore(@PathVariable Long id) {
        productService.restoreProduct(id);
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Product restored successfully!"
        );
    }

    // HARD DELETE 1 PRODUCT
    @DeleteMapping("/{id}/hard")
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    public ApiResponse<Void> hardDelete(@PathVariable Long id) {
        productService.hardDeleteProduct(id);
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Product hard deleted successfully!"
        );
    }

    // SOFT DELETE / RESTORE / HARD DELETE MANY PRODUCTS
    @PostMapping("/delete-many")
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    public ApiResponse<List<Long>> softDeleteMany(@RequestBody List<Long> ids) {
        return ApiResponse.successfulResponse(
                "Soft deleted products!",
                productService.softDeleteProducts(ids)
        );
    }

    @PostMapping("/restore-many")
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    public ApiResponse<List<Long>> restoreMany(@RequestBody List<Long> ids) {
        return ApiResponse.successfulResponse(
                "Restored products!",
                productService.restoreProducts(ids)
        );
    }

    @PostMapping("/hard-delete-many")
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    public ApiResponse<List<Long>> hardDeleteMany(@RequestBody List<Long> ids) {
        return ApiResponse.successfulResponse(
                "Hard deleted products!",
                productService.hardDeleteProducts(ids)
        );
    }

    // GET LIST SOFT DELETE PRODUCTS
    @GetMapping("/deleted")
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    public ApiResponse<Page<ProductResponse>> getDeleted(
            @Valid ProductCriteria criteria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.successfulResponse(
                "Fetched deleted products!",
                productService.getDeletedProducts(criteria, page, size)
        );
    }

    // UPLOAD GALLERY
    @PostMapping("/{productId}/gallery")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    public ApiResponse<List<String>> uploadGallery(
            @PathVariable Long productId,
            @RequestPart("images") List<MultipartFile> images
    ) {
        List<String> urls = productService.addGalleryImages(productId, images);
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Gallery uploaded successfully!",
                urls
        );
    }

    // DELETE /products/{productId}/gallery/{imageId}
    @DeleteMapping("/{productId}/gallery/{imageId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    public ApiResponse<Void> deleteGalleryImage(
            @PathVariable Long productId,
            @PathVariable Long imageId
    ) {
        productService.deleteGalleryImage(productId, imageId);
        return ApiResponse.successfulResponseNoData(
                HttpStatus.OK.value(),
                "Delete gallery image successfully!"
        );
    }

    // DELETE /products/{productId}/gallery  (xóa hết)
    @DeleteMapping("/{productId}/gallery")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    public ApiResponse<Integer> deleteAllGalleryImages(
            @PathVariable Long productId
    ) {
        int count = productService.deleteAllGalleryImages(productId);
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Delete all gallery images successfully!",
                count
        );
    }

    // GET HISTORY PRICE PRODUCT
    @GetMapping("/{id}/price-history")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    public ApiResponse<List<PriceHistoryResponse>> getPriceHistory(
            @PathVariable Long id
    ) {
        List<PriceHistoryResponse> response = productService.getPriceHistory(id);
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Fetched price history!",
                response
        );
    }
}
