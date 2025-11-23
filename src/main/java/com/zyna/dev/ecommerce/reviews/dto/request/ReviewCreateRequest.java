package com.zyna.dev.ecommerce.reviews.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewCreateRequest {

    @NotNull
    private Long productId;

    // optional: gắn với order cụ thể, dùng để verify purchase
    private Long orderId;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;

    private String content;
}
