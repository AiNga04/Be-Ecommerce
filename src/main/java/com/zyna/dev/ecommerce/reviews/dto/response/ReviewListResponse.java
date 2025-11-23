package com.zyna.dev.ecommerce.reviews.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class ReviewListResponse {
    private Page<ReviewResponse> reviews;
    private BigDecimal averageRating;
    private Long totalReviews;
}
