package com.zyna.dev.ecommerce.products.service.impl;

import com.zyna.dev.ecommerce.common.exceptions.ApplicationException;
import com.zyna.dev.ecommerce.common.utils.FileUploadUtil;
import com.zyna.dev.ecommerce.products.criteria.ProductCriteria;
import com.zyna.dev.ecommerce.products.dto.response.PriceHistoryResponse;
import com.zyna.dev.ecommerce.products.dto.response.GalleryImageResponse;
import com.zyna.dev.ecommerce.products.dto.response.ProductResponse;
import com.zyna.dev.ecommerce.products.mappers.ProductMapper;
import com.zyna.dev.ecommerce.products.models.Category;
import com.zyna.dev.ecommerce.products.models.PriceHistory;
import com.zyna.dev.ecommerce.products.models.Product;
import com.zyna.dev.ecommerce.products.models.ProductImage;
import com.zyna.dev.ecommerce.products.repository.CategoryRepository;
import com.zyna.dev.ecommerce.products.repository.PriceHistoryRepository;
import com.zyna.dev.ecommerce.products.repository.ProductImageRepository;
import com.zyna.dev.ecommerce.products.repository.ProductRepository;
import com.zyna.dev.ecommerce.products.service.interfaces.ProductService;
import com.zyna.dev.ecommerce.security.JwtUtil;
import com.zyna.dev.ecommerce.users.models.User;
import com.zyna.dev.ecommerce.users.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
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
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ProductImageRepository productImageRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final JwtUtil jwtUtil;
    private final HttpServletRequest request;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final com.zyna.dev.ecommerce.products.repository.SizeRepository sizeRepository;
    private final com.zyna.dev.ecommerce.products.repository.SizeGuideRepository sizeGuideRepository;

    // CREATE PRODUCT (multipart/form-data)
    @Override
    @Transactional
    public ProductResponse createProduct(String name, String description, Double price,
                                         Long categoryId, MultipartFile image,
                                         Long sizeGuideId, List<Long> sizeIds) {

        if (productRepository.existsByName(name)) {
            throw new ApplicationException(HttpStatus.CONFLICT, "Tên sản phẩm đã tồn tại");
        }

        String imageUrl = FileUploadUtil.saveImage(image);

        // find category by ID
        Category categoryEntity = null;
        if (categoryId != null) {
            categoryEntity = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ApplicationException(
                            HttpStatus.BAD_REQUEST,
                            "Không tìm thấy mã danh mục"
                    ));
        }

        com.zyna.dev.ecommerce.products.models.SizeGuide sizeGuide = null;
        if (sizeGuideId != null) {
            sizeGuide = sizeGuideRepository.findById(sizeGuideId)
                    .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Không tìm thấy hướng dẫn chọn size"));
        }

        Product product = Product.builder()
                .name(name)
                .description(description)
                .price(price != null ? BigDecimal.valueOf(price) : BigDecimal.ZERO)
                .imageUrl(imageUrl)
                .category(categoryEntity)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .sizeGuide(sizeGuide)
                .build();

        // Process variants (Size IDs only, stock starts at 0)
        if (sizeIds != null && !sizeIds.isEmpty()) {
            List<com.zyna.dev.ecommerce.products.models.ProductSize> productSizes = new ArrayList<>();
            for (Long sizeId : sizeIds) {
                com.zyna.dev.ecommerce.products.models.Size size = sizeRepository.findById(sizeId)
                        .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Không tìm thấy mã kích thước " + sizeId));
                
                com.zyna.dev.ecommerce.products.models.ProductSize ps = com.zyna.dev.ecommerce.products.models.ProductSize.builder()
                        .product(product)
                        .size(size)
                        .quantity(0) // Start with 0 stock
                        .build();
                productSizes.add(ps);
            }
            product.setProductSizes(productSizes);
        }

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
                        new ApplicationException(HttpStatus.NOT_FOUND, "Sản phẩm không tồn tại hoặc đã bị ngừng kinh doanh")
                );

        return productMapper.toProductResponseDetail(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PriceHistoryResponse> getPriceHistory(Long productId) {
        List<PriceHistory> histories =
                priceHistoryRepository.findByProductIdOrderByChangedAtDesc(productId);

        return histories.stream()
                .map(h -> {
                    BigDecimal oldP = h.getOldPrice();
                    BigDecimal newP = h.getNewPrice();
                    double percent = 0.0;
                    String type = "NONE";

                    if (oldP != null && oldP.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal diff = newP.subtract(oldP);
                        // (new - old) / old * 100
                         percent = diff.divide(oldP, 2, java.math.RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100)).doubleValue();
                        
                        if (diff.compareTo(BigDecimal.ZERO) > 0) {
                            type = "INCREASE";
                        } else if (diff.compareTo(BigDecimal.ZERO) < 0) {
                            type = "DECREASE";
                        }
                    } else if (oldP != null && oldP.compareTo(BigDecimal.ZERO) == 0 && newP.compareTo(BigDecimal.ZERO) > 0) {
                        percent = 100.0; // 0 -> something is 100% increase effectively (or infinite, but 100 is simpler for UI)
                        type = "INCREASE";
                    }

                    String changedBy = "Unknown";
                    if (h.getChangedBy() != null) {
                         // Prefer name, fallback to email
                        String fName = h.getChangedBy().getFirstName();
                        String lName = h.getChangedBy().getLastName();
                        if (fName != null && lName != null) {
                            changedBy = fName + " " + lName;
                        } else {
                            changedBy = h.getChangedBy().getEmail();
                        }
                    }

                    return PriceHistoryResponse.builder()
                            .oldPrice(oldP)
                            .newPrice(newP)
                            .changedAt(h.getChangedAt())
                            .changedBy(changedBy)
                            .percentChange(percent)
                            .changeType(type)
                            .build();
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PriceHistoryResponse> getAllPriceHistory(int page, int size) {
        Page<PriceHistory> pageResult = priceHistoryRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "changedAt"))
        );

        return pageResult.map(h -> {
            BigDecimal oldP = h.getOldPrice();
            BigDecimal newP = h.getNewPrice();
            double percent = 0.0;
            String type = "NONE";

            if (oldP != null && oldP.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal diff = newP.subtract(oldP);
                 percent = diff.divide(oldP, 2, java.math.RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).doubleValue();
                
                if (diff.compareTo(BigDecimal.ZERO) > 0) {
                    type = "INCREASE";
                } else if (diff.compareTo(BigDecimal.ZERO) < 0) {
                    type = "DECREASE";
                }
            } else if (oldP != null && oldP.compareTo(BigDecimal.ZERO) == 0 && newP.compareTo(BigDecimal.ZERO) > 0) {
                percent = 100.0;
                type = "INCREASE";
            }

            String changedBy = "Unknown";
            if (h.getChangedBy() != null) {
                String fName = h.getChangedBy().getFirstName();
                String lName = h.getChangedBy().getLastName();
                if (fName != null && lName != null) {
                    changedBy = fName + " " + lName;
                } else {
                    changedBy = h.getChangedBy().getEmail();
                }
            }

            return PriceHistoryResponse.builder()
                    .oldPrice(oldP)
                    .newPrice(newP)
                    .changedAt(h.getChangedAt())
                    .changedBy(changedBy)
                    .percentChange(percent)
                    .changeType(type)
                    .productId(h.getProduct().getId())
                    .productName(h.getProduct().getName())
                    .productImage(h.getProduct().getImageUrl())
                    .build();
        });
    }

    // SEARCH PRODUCTS (filter, pagination, sorting)
    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(com.zyna.dev.ecommerce.products.criteria.ProductCriteria criteria, int page, int size) {
        Specification<Product> spec = createSpecification(criteria, true);
        Page<Product> productPage = productRepository.findAll(spec,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));

        return productPage.map(productMapper::toProductResponse);
    }

    private Specification<Product> createSpecification(com.zyna.dev.ecommerce.products.criteria.ProductCriteria criteria, boolean isActive) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // isActive filter
            predicates.add(cb.equal(root.get("isActive"), isActive));

            if (criteria.getName() != null && !criteria.getName().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")),
                        "%" + criteria.getName().toLowerCase() + "%"));
            }

            if (criteria.getCategory() != null && !criteria.getCategory().isEmpty()) {
                List<String> rawCategories = criteria.getCategory().stream()
                        .filter(c -> c != null && !c.isBlank())
                        .toList();

                if (!rawCategories.isEmpty()) {
                    List<Long> categoryIds = new ArrayList<>();
                    List<String> categoryCodes = new ArrayList<>();

                    for (String cat : rawCategories) {
                        try {
                            // Try to parse as ID
                            categoryIds.add(Long.parseLong(cat));
                        } catch (NumberFormatException e) {
                            // Not a number, treat as Code
                            categoryCodes.add(cat.toLowerCase());
                        }
                    }

                    List<Predicate> catPredicates = new ArrayList<>();
                    if (!categoryIds.isEmpty()) {
                        catPredicates.add(root.get("category").get("id").in(categoryIds));
                    }
                    if (!categoryCodes.isEmpty()) {
                        catPredicates.add(cb.lower(root.get("category").get("code")).in(categoryCodes));
                    }
                    
                    if (!catPredicates.isEmpty()) {
                        predicates.add(cb.or(catPredicates.toArray(new Predicate[0])));
                    }
                }
            }

            if (criteria.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), criteria.getMinPrice()));
            }

            if (criteria.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), criteria.getMaxPrice()));
            }

            // check total stock using subquery or simple stock field (which is gone)
            // Since stock field is gone, we can join product_sizes and sum quantity, or simpler:
            // Just skip stock filter for now or implement properly with subquery.
            // For now, removing stock filter logic to avoid crash.
            /*
            if (criteria.getMinStock() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("stock"), criteria.getMinStock()));
            }
            */

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    // UPDATE PRODUCT (multipart/form-data)
    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, String name, String description, Double price,
                                         Long categoryId, MultipartFile image,
                                         Long sizeGuideId, List<Long> sizeIds) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm"));

        if (!product.getIsActive()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Sản phẩm đang bị ẩn. Vui lòng khôi phục trước khi cập nhật");
        }

        // ... (User check skipped for brevity, keeping existing flow) ...
        String email = getCurrentUserEmail();
        User changedByUser = null;
        if (email != null) {
            changedByUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"));
        }

        if (image != null && !image.isEmpty()) {
            String newImageUrl = FileUploadUtil.replaceImage(product.getImageUrl(), image);
            product.setImageUrl(newImageUrl);
        }

        if (price != null && product.getPrice() != null &&
                product.getPrice().compareTo(BigDecimal.valueOf(price)) != 0) {
             PriceHistory history = PriceHistory.builder()
                    .product(product)
                    .oldPrice(product.getPrice())
                    .newPrice(BigDecimal.valueOf(price))
                    .changedBy(changedByUser)
                    .build();
            priceHistoryRepository.save(history);
            log.info("Price changed for product id={}, {} → {}", id, product.getPrice(), price);
        }

        if (name != null) product.setName(name);
        if (description != null) product.setDescription(description);
        if (price != null) product.setPrice(BigDecimal.valueOf(price));
        
        if (categoryId != null) {
            Category categoryEntity = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ApplicationException(
                            HttpStatus.BAD_REQUEST,
                            "Category ID not found!"
                    ));
            product.setCategory(categoryEntity);
        }

        // Update SizeGuide
        if (sizeGuideId != null) {
            com.zyna.dev.ecommerce.products.models.SizeGuide sizeGuide = sizeGuideRepository.findById(sizeGuideId)
                    .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "SizeGuide not found!"));
            product.setSizeGuide(sizeGuide);
        }

        // Update Variants (Sync Size IDs)
        if (sizeIds != null) {
            List<com.zyna.dev.ecommerce.products.models.ProductSize> currentSizes = product.getProductSizes();
            Set<Long> newSizeIdsSet = new HashSet<>(sizeIds);

            // 1. Remove sizes not in the new list
            currentSizes.removeIf(ps -> !newSizeIdsSet.contains(ps.getSize().getId()));

            // 2. Add new sizes that are not in the current list
            Set<Long> currentSizeIds = currentSizes.stream()
                    .map(ps -> ps.getSize().getId())
                    .collect(java.util.stream.Collectors.toSet());

            for (Long sid : sizeIds) {
                if (!currentSizeIds.contains(sid)) {
                    com.zyna.dev.ecommerce.products.models.Size sizeObj = sizeRepository.findById(sid)
                            .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Không tìm thấy mã kích thước " + sid));

                    com.zyna.dev.ecommerce.products.models.ProductSize ps = com.zyna.dev.ecommerce.products.models.ProductSize.builder()
                            .product(product)
                            .size(sizeObj)
                            .quantity(0) // New variants start with 0
                            .build();
                    currentSizes.add(ps);
                }
            }
            // Existing sizes kept their Quantity.
        }

        product.setUpdatedAt(LocalDateTime.now());
        Product saved = productRepository.save(product);

        log.info("Product updated id={}, image replaced={}", id, (image != null && !image.isEmpty()));
        return productMapper.toProductResponse(saved);
    }

    private String getCurrentUserEmail() {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.validateToken(token)) {
            return null;
        }

        // subject trong token = email
        return jwtUtil.extractUsername(token);
    }

    // SOFT DELETE PRODUCT
    @Override
    public void softDeleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm"));

        if (!product.getIsActive()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Sản phẩm đã ở trạng thái ẩn");
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
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm"));

        if (product.getIsActive()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Sản phẩm đã ở trạng thái kích hoạt");
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
            throw new ApplicationException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm");
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
        Specification<Product> spec = createSpecification(criteria, false);
        Page<Product> productPage = productRepository.findAll(spec,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "deletedAt")));

        return productPage.map(productMapper::toProductResponse);
    }

    // CREATE GALLERY
    @Override
    public List<GalleryImageResponse> addGalleryImages(Long productId, List<MultipartFile> images) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm"));

        if (!product.getIsActive()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Không thể thêm ảnh vào sản phẩm đang bị ẩn");
        }

        List<String> savedUrls = FileUploadUtil.saveImages(images);

        List<ProductImage> gallery = savedUrls.stream()
                .map(url -> ProductImage.builder()
                        .product(product)
                        .imageUrl(url)
                        .uploadedAt(LocalDateTime.now())
                        .build())
                .toList();

        List<ProductImage> saved = productImageRepository.saveAll(gallery);
        log.info("Added {} gallery images for product id={}", saved.size(), productId);

        return saved.stream()
                .map(img -> GalleryImageResponse.builder()
                        .id(img.getId())
                        .url(img.getImageUrl())
                        .build())
                .toList();
    }

    @Override
    public GalleryImageResponse updateGalleryImage(Long productId, Long imageId, MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Cần có tệp hình ảnh");
        }

        ProductImage existing = productImageRepository.findById(imageId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Không tìm thấy ảnh"));

        if (existing.getProduct() == null || !existing.getProduct().getId().equals(productId)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Ảnh không thuộc về sản phẩm này");
        }

        String newUrl = FileUploadUtil.replaceImage(existing.getImageUrl(), image);
        existing.setImageUrl(newUrl);
        existing.setUploadedAt(LocalDateTime.now());

        productImageRepository.save(existing);

        return GalleryImageResponse.builder()
                .id(existing.getId())
                .url(existing.getImageUrl())
                .build();
    }

    // DELETE 1 IMAGE IN GALLERY
    @Override
    public void deleteGalleryImage(Long productId, Long imageId) {
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Không tìm thấy ảnh"));

        // bảo vệ: image phải thuộc đúng product
        if (image.getProduct() == null || !image.getProduct().getId().equals(productId)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Ảnh không thuộc về sản phẩm này");
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
