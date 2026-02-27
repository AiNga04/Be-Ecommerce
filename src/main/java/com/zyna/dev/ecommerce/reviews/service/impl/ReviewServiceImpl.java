package com.zyna.dev.ecommerce.reviews.service.impl;

import com.zyna.dev.ecommerce.common.enums.OrderStatus;
import com.zyna.dev.ecommerce.common.exceptions.ApplicationException;
import com.zyna.dev.ecommerce.common.utils.FileUploadUtil;
import com.zyna.dev.ecommerce.orders.models.Order;
import com.zyna.dev.ecommerce.orders.repository.OrderItemRepository;
import com.zyna.dev.ecommerce.orders.repository.OrderRepository;
import com.zyna.dev.ecommerce.products.models.Product;
import com.zyna.dev.ecommerce.products.repository.ProductRepository;
import com.zyna.dev.ecommerce.reviews.ReviewMapper;
import com.zyna.dev.ecommerce.reviews.dto.request.ReviewCreateRequest;
import com.zyna.dev.ecommerce.reviews.dto.response.ReviewResponse;
import com.zyna.dev.ecommerce.reviews.dto.response.ReviewListResponse;
import com.zyna.dev.ecommerce.reviews.dto.request.ReviewUpdateRequest;
import com.zyna.dev.ecommerce.reviews.models.Review;
import com.zyna.dev.ecommerce.reviews.models.ReviewImage;
import com.zyna.dev.ecommerce.reviews.repository.ReviewRepository;
import com.zyna.dev.ecommerce.reviews.service.interfaces.ReviewService;
import com.zyna.dev.ecommerce.users.models.User;
import com.zyna.dev.ecommerce.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ReviewMapper reviewMapper;

    @Override
    @Transactional
    public ReviewResponse create(Long userId, ReviewCreateRequest request, List<MultipartFile> images) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm"));

        // verify purchased (any order of user contains this product and is COMPLETED)
        java.util.List<com.zyna.dev.ecommerce.orders.models.OrderItem> purchasedItems = 
                orderItemRepository.findByProductAndOrder_UserAndOrder_StatusOrderByOrder_CreatedAtDesc(product, user, OrderStatus.COMPLETED);
                
        if (purchasedItems.isEmpty()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Bạn chỉ có thể đánh giá các sản phẩm từ đơn hàng đã HOÀN THÀNH");
        }

        Order order = null;
        if (request.getOrderId() != null) {
            order = orderRepository.findById(request.getOrderId())
                    .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng"));
            if (!order.getUser().getId().equals(userId)) {
                throw new ApplicationException(HttpStatus.FORBIDDEN, "Đơn hàng này không thuộc về bạn");
            }
            if (order.getStatus() != OrderStatus.COMPLETED) {
                throw new ApplicationException(HttpStatus.BAD_REQUEST, "Đơn hàng phải ở trạng thái HOÀN THÀNH mới có thể đánh giá");
            }
            // Optional: verify that the specified order actually contains the product
            boolean orderContainsProduct = purchasedItems.stream()
                    .anyMatch(oi -> oi.getOrder().getId().equals(request.getOrderId()));
            if (!orderContainsProduct) {
                throw new ApplicationException(HttpStatus.BAD_REQUEST, "Không tìm thấy sản phẩm trong đơn hàng đã chỉ định");
            }
        } else {
            // Auto link the most recent completed order
            order = purchasedItems.get(0).getOrder();
        }

        // Check duplicate: same user + product + order
        if (reviewRepository.existsByUserAndProductAndOrder(user, product, order)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Bạn đã đánh giá sản phẩm này cho đơn hàng này rồi");
        }

        Review review = Review.builder()
                .user(user)
                .product(product)
                .order(order)
                .rating(request.getRating())
                .content(request.getContent())
                .hidden(false)
                .build();

        Review saved = reviewRepository.save(review);

        if (images != null && !images.isEmpty()) {
            for (MultipartFile file : images) {
                String url = FileUploadUtil.saveImage(file);
                if (url == null) continue;
                ReviewImage img = ReviewImage.builder()
                        .review(saved)
                        .imageUrl(url)
                        .build();
                saved.getImages().add(img);
            }
        }

        saved = reviewRepository.save(saved);
        updateProductRating(product.getId());

        return reviewMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewListResponse listByProduct(Long productId, int page, int size) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm"));

        Page<Review> reviews = reviewRepository.findByProductAndHiddenFalse(
                product,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        Double avg = reviewRepository.findAverageRating(productId);
        Long count = reviewRepository.countActiveReviews(productId);

        return ReviewListResponse.builder()
                .reviews(reviews.map(reviewMapper::toResponse))
                .averageRating(BigDecimal.valueOf(avg == null ? 0.0 : avg).setScale(2, RoundingMode.HALF_UP))
                .totalReviews(count != null ? count : 0L)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> listMyReviewsByProduct(Long userId, Long productId, int page, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm"));

        Page<Review> reviews = reviewRepository.findByUserAndProductAndHiddenFalse(
                user,
                product,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        return reviews.map(reviewMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> listAll(int page, int size) {
        Page<Review> reviews = reviewRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        return reviews.map(reviewMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> listReported(int page, int size) {
        Page<Review> reviews = reviewRepository.findReported(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        return reviews.map(reviewMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> listHidden(int page, int size) {
        Page<Review> reviews = reviewRepository.findByHiddenTrue(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        return reviews.map(reviewMapper::toResponse);
    }

    @Override
    @Transactional
    public void report(Long userId, Long reviewId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "User not found"));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Không tìm thấy đánh giá"));

        if (review.getUser().getId().equals(userId)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Bạn không thể báo cáo đánh giá của chính mình");
        }

        if (review.getReporters().contains(user)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Bạn đã báo cáo đánh giá này rồi");
        }

        review.getReporters().add(user);
        reviewRepository.save(review);
    }

    @Override
    @Transactional
    public void hide(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Không tìm thấy đánh giá"));
        if (!review.getHidden()) {
            review.setHidden(true);
            reviewRepository.save(review);
            updateProductRating(review.getProduct().getId());
        }
    }

    @Override
    @Transactional
    public void unhide(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Không tìm thấy đánh giá"));
        if (review.getHidden()) {
            review.setHidden(false);
            reviewRepository.save(review);
            updateProductRating(review.getProduct().getId());
        }
    }

    @Override
    @Transactional
    public ReviewResponse update(Long userId, ReviewUpdateRequest request, List<MultipartFile> images, Long reviewId, boolean canManage) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Không tìm thấy đánh giá"));

        if (!Objects.equals(review.getUser().getId(), userId) && !canManage) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "Bạn không thể cập nhật đánh giá này");
        }

        if (request.getRating() != null) {
            if (request.getRating() < 1 || request.getRating() > 5) {
                throw new ApplicationException(HttpStatus.BAD_REQUEST, "Điểm đánh giá phải từ 1 đến 5");
            }
            review.setRating(request.getRating());
        }
        if (request.getContent() != null) {
            review.setContent(request.getContent());
        }

        if (images != null) {
            // replace images: delete old files, clear list, add new
            if (review.getImages() != null) {
                review.getImages().forEach(img -> FileUploadUtil.deleteImage(img.getImageUrl()));
                review.getImages().clear();
            }
            for (MultipartFile file : images) {
                String url = FileUploadUtil.saveImage(file);
                if (url == null) continue;
                ReviewImage img = ReviewImage.builder()
                        .review(review)
                        .imageUrl(url)
                        .build();
                review.getImages().add(img);
            }
        }

        review = reviewRepository.save(review);
        updateProductRating(review.getProduct().getId());
        return reviewMapper.toResponse(review);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long reviewId, boolean canManage) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Review not found"));

        if (!Objects.equals(review.getUser().getId(), userId) && !canManage) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "Bạn không thể xóa đánh giá này");
        }

        if (review.getImages() != null) {
            review.getImages().forEach(img -> FileUploadUtil.deleteImage(img.getImageUrl()));
        }
        reviewRepository.delete(review);
        updateProductRating(review.getProduct().getId());
    }

    private void updateProductRating(Long productId) {
        Double avg = reviewRepository.findAverageRating(productId);
        Long count = reviewRepository.countActiveReviews(productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Product not found"));

        BigDecimal average = BigDecimal.valueOf(avg == null ? 0.0 : avg)
                .setScale(2, RoundingMode.HALF_UP);
        product.setRatingAverage(average);
        product.setReviewCount(count != null ? count.intValue() : 0);

        productRepository.save(product);
    }
}
