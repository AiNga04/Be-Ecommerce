package com.zyna.dev.ecommerce.products.service.interfaces;

import com.zyna.dev.ecommerce.products.criteria.ProductCriteria;
import com.zyna.dev.ecommerce.products.dto.request.ProductCreateRequest;
import com.zyna.dev.ecommerce.products.dto.request.ProductUpdateRequest;
import com.zyna.dev.ecommerce.products.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {
    ProductResponse createProduct(String name, String description, Double price,
                                  String category, Integer stock, MultipartFile image);
    ProductResponse updateProduct(Long id, String name, String description, Double price,
                                  String category, Integer stock, MultipartFile image);
    ProductResponse getProductById(Long id);
    Page<ProductResponse> searchProducts(ProductCriteria criteria, int page, int size);
    void softDeleteProduct(Long id);
    void restoreProduct(Long id);
    void hardDeleteProduct(Long id);

    List<Long> softDeleteProducts(List<Long> ids);
    List<Long> restoreProducts(List<Long> ids);
    List<Long> hardDeleteProducts(List<Long> ids);
    Page<ProductResponse> getDeletedProducts(ProductCriteria criteria, int page, int size);

    // gallery
    List<String> addGalleryImages(Long productId, List<MultipartFile> images);
    void deleteGalleryImage(Long productId, Long imageId);
    int deleteAllGalleryImages(Long productId);
}
