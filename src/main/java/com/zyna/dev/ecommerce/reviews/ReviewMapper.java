package com.zyna.dev.ecommerce.reviews;

import com.zyna.dev.ecommerce.reviews.dto.response.ReviewResponse;
import com.zyna.dev.ecommerce.reviews.models.Review;
import com.zyna.dev.ecommerce.reviews.models.ReviewImage;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReviewMapper {

    public ReviewResponse toResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .productId(review.getProduct().getId())
                .orderId(review.getOrder() != null ? review.getOrder().getId() : null)
                .userId(review.getUser().getId())
                .userName(review.getUser().getFirstName() + " " + review.getUser().getLastName())
                .rating(review.getRating())
                .content(review.getContent())
                .images(
                        review.getImages() == null
                                ? List.of()
                                : review.getImages().stream().map(ReviewImage::getImageUrl).toList()
                )
                .hidden(review.getHidden())
                .reportCount(review.getReporters() == null ? 0 : review.getReporters().size())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
