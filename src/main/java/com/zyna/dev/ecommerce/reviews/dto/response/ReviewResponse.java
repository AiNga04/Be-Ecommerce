package com.zyna.dev.ecommerce.reviews.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class ReviewResponse {

    private Long id;
    private Long productId;
    private Long orderId;
    private Long userId;
    private String userName;
    private Integer rating;
    private String content;
    private List<String> images;
    private Boolean hidden;
    private Integer reportCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
