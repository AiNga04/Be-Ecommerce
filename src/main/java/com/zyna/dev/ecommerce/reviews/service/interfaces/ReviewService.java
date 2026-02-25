package com.zyna.dev.ecommerce.reviews.service.interfaces;

import com.zyna.dev.ecommerce.reviews.dto.request.ReviewCreateRequest;
import com.zyna.dev.ecommerce.reviews.dto.request.ReviewUpdateRequest;
import com.zyna.dev.ecommerce.reviews.dto.response.ReviewResponse;
import com.zyna.dev.ecommerce.reviews.dto.response.ReviewListResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ReviewService {

    ReviewResponse create(Long userId, ReviewCreateRequest request, List<MultipartFile> images);

    ReviewListResponse listByProduct(Long productId, int page, int size);

    Page<ReviewResponse> listMyReviewsByProduct(Long userId, Long productId, int page, int size);
    Page<ReviewResponse> listAll(int page, int size);

    Page<ReviewResponse> listReported(int page, int size);

    Page<ReviewResponse> listHidden(int page, int size);

    void report(Long userId, Long reviewId);

    void hide(Long reviewId);

    void unhide(Long reviewId);

    ReviewResponse update(Long userId, ReviewUpdateRequest request, List<MultipartFile> images, Long reviewId, boolean canManage);

    void delete(Long userId, Long reviewId, boolean canManage);
}
