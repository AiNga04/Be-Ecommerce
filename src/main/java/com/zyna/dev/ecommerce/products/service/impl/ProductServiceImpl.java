package com.zyna.dev.ecommerce.products.service.impl;

import com.zyna.dev.ecommerce.common.exceptions.ApplicationException;
import com.zyna.dev.ecommerce.common.utils.FileUploadUtil;
import com.zyna.dev.ecommerce.products.*;
import com.zyna.dev.ecommerce.products.criteria.ProductCriteria;
import com.zyna.dev.ecommerce.products.dto.response.ProductResponse;
import com.zyna.dev.ecommerce.products.models.PriceHistory;
import com.zyna.dev.ecommerce.products.models.Product;
import com.zyna.dev.ecommerce.products.models.ProductImage;
import com.zyna.dev.ecommerce.products.repository.PriceHistoryRepository;
import com.zyna.dev.ecommerce.products.repository.ProductImageRepository;
import com.zyna.dev.ecommerce.products.repository.ProductRepository;
import com.zyna.dev.ecommerce.products.service.interfaces.ProductService;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Predicate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ProductImageRepository productImageRepository;
    private final PriceHistoryRepository priceHistoryRepository;

    // CREATE PRODUCT (multipart/form-data)
    @Override
    public ProductResponse createProduct(String name, String description, Double price,
                                         String category, Integer stock, MultipartFile image) {

        if (productRepository.existsByName(name)) {
            throw new ApplicationException(HttpStatus.CONFLICT, "Product name already exists!");
        }

        String imageUrl = FileUploadUtil.saveImage(image);

        Product product = Product.builder()
                .name(name)
                .description(description)
                .price(price != null ? BigDecimal.valueOf(price) : BigDecimal.ZERO)
                .imageUrl(imageUrl)
                .category(category)
                .stock(stock != null ? stock : 0)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        Product saved = productRepository.save(product);
        log.info("✅ Product created: id={}, name={}, image={}", saved.getId(), saved.getName(), imageUrl);

        return productMapper.toProductResponse(saved);
    }

    // GET ACTIVE PRODUCT BY ID
    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findActiveByIdWithGallery(id)
                .orElseThrow(() ->
                        new ApplicationException(HttpStatus.NOT_FOUND, "Product not found or inactive!")
                );

        return productMapper.toProductResponse(product);
    }


    // SEARCH PRODUCTS (filter, pagination, sorting)
    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(ProductCriteria criteria, int page, int size) {
        Page<Product> basePage = productRepository.findAllByIsActiveTrue(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        Predicate<Product> matches = p -> {
            if (criteria.getName() != null &&
                    !p.getName().toLowerCase().contains(criteria.getName().toLowerCase())) return false;
            if (criteria.getCategory() != null &&
                    (p.getCategory() == null || !p.getCategory().equalsIgnoreCase(criteria.getCategory()))) return false;
            if (criteria.getMinPrice() != null &&
                    (p.getPrice() == null || p.getPrice().compareTo(criteria.getMinPrice()) < 0)) return false;
            if (criteria.getMaxPrice() != null &&
                    (p.getPrice() == null || p.getPrice().compareTo(criteria.getMaxPrice()) > 0)) return false;
            if (criteria.getMinStock() != null &&
                    (p.getStock() == null || p.getStock() < criteria.getMinStock())) return false;
            return true;
        };

        List<ProductResponse> filtered = basePage.getContent().stream()
                .filter(matches)
                .map(productMapper::toProductResponse)
                .toList();

        return new PageImpl<>(filtered, basePage.getPageable(), filtered.size());
    }

    // UPDATE PRODUCT (multipart/form-data)
    @Override
    public ProductResponse updateProduct(Long id, String name, String description, Double price,
                                         String category, Integer stock, MultipartFile image) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Product not found!"));

        if (!product.getIsActive()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Product is inactive. Restore before updating!");
        }

        // Nếu có ảnh mới → thay ảnh
        if (image != null && !image.isEmpty()) {
            String newImageUrl = FileUploadUtil.replaceImage(product.getImageUrl(), image);
            product.setImageUrl(newImageUrl);
        }

        // Kiểm tra nếu giá thay đổi → lưu vào lịch sử giá
        if (price != null && product.getPrice() != null &&
                product.getPrice().compareTo(BigDecimal.valueOf(price)) != 0) {

            PriceHistory history = PriceHistory.builder()
                    .product(product)
                    .oldPrice(product.getPrice())
                    .newPrice(BigDecimal.valueOf(price))
                    // có thể lấy user hiện tại từ SecurityContextHolder (để audit)
                    .changedBy(null)
                    .build();

            priceHistoryRepository.save(history);
            log.info("Price changed for product id={}, {} → {}", id, product.getPrice(), price);
        }

        if (name != null) product.setName(name);
        if (description != null) product.setDescription(description);
        if (price != null) product.setPrice(BigDecimal.valueOf(price));
        if (category != null) product.setCategory(category);
        if (stock != null) product.setStock(stock);

        product.setUpdatedAt(LocalDateTime.now());
        Product saved = productRepository.save(product);

        log.info("Product updated id={}, image replaced={}", id, (image != null && !image.isEmpty()));
        return productMapper.toProductResponse(saved);
    }

    // SOFT DELETE PRODUCT
    @Override
    public void softDeleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Product not found!"));

        if (!product.getIsActive()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Product already inactive!");
        }

        product.setIsActive(false);
        product.setDeletedAt(LocalDateTime.now());
        productRepository.save(product);

        log.info("Soft deleted product id={}", id);
    }

    // RESTORE PRODUCT
    @Override
    public void restoreProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Product not found!"));

        if (product.getIsActive()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Product already active!");
        }

        product.setIsActive(true);
        product.setDeletedAt(null);
        productRepository.save(product);

        log.info("Restored product id={}", id);
    }

    // HARD DELETE PRODUCT
    @Override
    public void hardDeleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ApplicationException(HttpStatus.NOT_FOUND, "Product not found!");
        }
        productRepository.deleteById(id);
        log.info("Hard deleted product id={}", id);
    }

    // BULK SOFT DELETE
    @Override
    public List<Long> softDeleteProducts(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyList();

        List<Product> products = productRepository.findAllById(ids)
                .stream().filter(Product::getIsActive).toList();

        LocalDateTime now = LocalDateTime.now();
        products.forEach(p -> {
            p.setIsActive(false);
            p.setDeletedAt(now);
        });

        productRepository.saveAll(products);
        log.info("Soft deleted {} products", products.size());

        return products.stream().map(Product::getId).toList();
    }

    // BULK RESTORE
    @Override
    public List<Long> restoreProducts(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyList();

        List<Product> products = productRepository.findAllById(ids)
                .stream().filter(p -> !p.getIsActive()).toList();

        products.forEach(p -> {
            p.setIsActive(true);
            p.setDeletedAt(null);
        });

        productRepository.saveAll(products);
        log.info("Restored {} products", products.size());

        return products.stream().map(Product::getId).toList();
    }

    // BULK HARD DELETE
    @Override
    public List<Long> hardDeleteProducts(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyList();

        List<Product> products = productRepository.findAllById(ids);
        productRepository.deleteAll(products);
        log.info("Hard deleted {} products", products.size());

        return products.stream().map(Product::getId).toList();
    }

    // GET ALL DELETED PRODUCTS
    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getDeletedProducts(ProductCriteria criteria, int page, int size) {
        Page<Product> basePage = productRepository.findAllByIsActiveFalse(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "deletedAt"))
        );

        Predicate<Product> matches = p -> {
            if (criteria.getName() != null &&
                    !p.getName().toLowerCase().contains(criteria.getName().toLowerCase())) return false;
            if (criteria.getCategory() != null &&
                    (p.getCategory() == null || !p.getCategory().equalsIgnoreCase(criteria.getCategory()))) return false;
            return true;
        };

        List<ProductResponse> filtered = basePage.getContent().stream()
                .filter(matches)
                .map(productMapper::toProductResponse)
                .toList();

        return new PageImpl<>(filtered, basePage.getPageable(), filtered.size());
    }

    // CREATE GALLERY
    @Override
    public List<String> addGalleryImages(Long productId, List<MultipartFile> images) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Product not found!"));

        if (!product.getIsActive()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Cannot add images to inactive product!");
        }

        List<String> savedUrls = FileUploadUtil.saveImages(images);

        List<ProductImage> gallery = savedUrls.stream()
                .map(url -> ProductImage.builder()
                        .product(product)
                        .imageUrl(url)
                        .uploadedAt(LocalDateTime.now())
                        .build())
                .toList();

        productImageRepository.saveAll(gallery);
        log.info("Added {} gallery images for product id={}", gallery.size(), productId);

        return savedUrls;
    }

    // DELETE 1 IMAGE IN GALLERY
    @Override
    public void deleteGalleryImage(Long productId, Long imageId) {
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Image not found!"));

        // bảo vệ: image phải thuộc đúng product
        if (image.getProduct() == null || !image.getProduct().getId().equals(productId)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Image does not belong to this product!");
        }

        // xóa file trên disk
        FileUploadUtil.deleteImage(image.getImageUrl());

        // xóa record trong DB
        productImageRepository.delete(image);

        log.info("Deleted gallery image id={} of product id={}", imageId, productId);
    }

    // DELETE GALLERY
    @Override
    public int deleteAllGalleryImages(Long productId) {
        List<ProductImage> images = productImageRepository.findAllByProductId(productId);

        if (images.isEmpty()) {
            return 0;
        }

        // xóa file vật lý
        images.forEach(img -> FileUploadUtil.deleteImage(img.getImageUrl()));

        // xóa DB
        productImageRepository.deleteAll(images);

        log.info("Deleted {} gallery images of product id={}", images.size(), productId);
        return images.size();
    }
}
