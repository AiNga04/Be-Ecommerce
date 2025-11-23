package com.zyna.dev.ecommerce.reviews.controller;

import com.zyna.dev.ecommerce.common.ApiResponse;
import com.zyna.dev.ecommerce.reviews.dto.request.ReviewCreateRequest;
import com.zyna.dev.ecommerce.reviews.dto.request.ReviewUpdateRequest;
import com.zyna.dev.ecommerce.reviews.dto.response.ReviewResponse;
import com.zyna.dev.ecommerce.reviews.dto.response.ReviewListResponse;
import com.zyna.dev.ecommerce.reviews.service.interfaces.ReviewService;
import com.zyna.dev.ecommerce.users.service.interfaces.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    private Long getCurrentUserId(Authentication authentication) {
        String email = (String) authentication.getPrincipal();
        return userService.getUserIdByEmail(email);
    }

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ORDER_WRITE')")
    public ApiResponse<ReviewResponse> create(
            Authentication authentication,
            @RequestPart(value = "data", required = false) String data,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        Long userId = getCurrentUserId(authentication);
        ReviewCreateRequest request = parseRequest(data);
        ReviewResponse response = reviewService.create(userId, request, images);
        return ApiResponse.successfulResponse(
                HttpStatus.CREATED.value(),
                "Review created successfully!",
                response
        );
    }

    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ORDER_WRITE')")
    public ApiResponse<ReviewResponse> createJson(
            Authentication authentication,
            @Valid @RequestBody ReviewCreateRequest request
    ) {
        Long userId = getCurrentUserId(authentication);
        ReviewResponse response = reviewService.create(userId, request, List.of());
        return ApiResponse.successfulResponse(
                HttpStatus.CREATED.value(),
                "Review created successfully!",
                response
        );
    }

    @PostMapping(consumes = {MediaType.APPLICATION_OCTET_STREAM_VALUE})
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ORDER_WRITE')")
    public ApiResponse<ReviewResponse> createOctet(
            Authentication authentication,
            @Valid @RequestBody ReviewCreateRequest request
    ) {
        Long userId = getCurrentUserId(authentication);
        ReviewResponse response = reviewService.create(userId, request, List.of());
        return ApiResponse.successfulResponse(
                HttpStatus.CREATED.value(),
                "Review created successfully!",
                response
        );
    }

    @PutMapping(value = "/{id}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ORDER_WRITE')")
    public ApiResponse<ReviewResponse> update(
            Authentication authentication,
            @PathVariable Long id,
            @RequestPart(value = "data", required = false) String data,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        Long userId = getCurrentUserId(authentication);
        boolean canManage = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("REVIEW_MANAGE"));
        ReviewUpdateRequest request = parseUpdateRequest(data);
        ReviewResponse response = reviewService.update(userId, request, images, id, canManage);
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Review updated successfully!",
                response
        );
    }

    @PutMapping(value = "/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ORDER_WRITE')")
    public ApiResponse<ReviewResponse> updateJson(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody ReviewUpdateRequest request
    ) {
        Long userId = getCurrentUserId(authentication);
        boolean canManage = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("REVIEW_MANAGE"));
        ReviewResponse response = reviewService.update(userId, request, null, id, canManage);
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Review updated successfully!",
                response
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ORDER_WRITE')")
    public ApiResponse<Void> delete(
            Authentication authentication,
            @PathVariable Long id
    ) {
        Long userId = getCurrentUserId(authentication);
        boolean canManage = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("REVIEW_MANAGE"));
        reviewService.delete(userId, id, canManage);
        return ApiResponse.successfulResponseNoData(
                HttpStatus.OK.value(),
                "Review deleted successfully!"
        );
    }

    private ReviewCreateRequest parseRequest(String data) {
        if (data == null) {
            throw new IllegalArgumentException("Review data is required");
        }
        try {
            return objectMapper.readValue(data, ReviewCreateRequest.class);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid review data format");
        }
    }

    private ReviewUpdateRequest parseUpdateRequest(String data) {
        if (data == null) {
            throw new IllegalArgumentException("Review data is required");
        }
        try {
            return objectMapper.readValue(data, ReviewUpdateRequest.class);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid review data format");
        }
    }

    @GetMapping("/product/{productId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<ReviewListResponse> listByProduct(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        ReviewListResponse data = reviewService.listByProduct(productId, page, size);
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Get reviews successfully!",
                data
        );
    }

    // ADMIN/STAFF: full list
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('REVIEW_MANAGE')")
    public ApiResponse<Page<ReviewResponse>> listAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<ReviewResponse> data = reviewService.listAll(page, size);
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Get all reviews successfully!",
                data
        );
    }

    // ADMIN/STAFF: reported
    @GetMapping("/reported")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('REVIEW_MANAGE')")
    public ApiResponse<Page<ReviewResponse>> listReported(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<ReviewResponse> data = reviewService.listReported(page, size);
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Get reported reviews successfully!",
                data
        );
    }

    // ADMIN/STAFF: hidden=false? requirement is view hidden -> reuse listAll; to list hidden-only we can add:
    @GetMapping("/hidden")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('REVIEW_MANAGE')")
    public ApiResponse<Page<ReviewResponse>> listHidden(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<ReviewResponse> data = reviewService.listHidden(page, size);
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Get hidden reviews successfully!",
                data
        );
    }

    @PostMapping("/{id}/report")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ORDER_READ')")
    public ApiResponse<Void> report(
            Authentication authentication,
            @PathVariable Long id
    ) {
        Long userId = getCurrentUserId(authentication);
        reviewService.report(userId, id);
        return ApiResponse.successfulResponseNoData(
                HttpStatus.OK.value(),
                "Report submitted"
        );
    }

    @PatchMapping("/{id}/hide")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('REVIEW_MANAGE')")
    public ApiResponse<Void> hide(@PathVariable Long id) {
        reviewService.hide(id);
        return ApiResponse.successfulResponseNoData(
                HttpStatus.OK.value(),
                "Review hidden"
        );
    }

    @PatchMapping("/{id}/unhide")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('REVIEW_MANAGE')")
    public ApiResponse<Void> unhide(@PathVariable Long id) {
        reviewService.unhide(id);
        return ApiResponse.successfulResponseNoData(
                HttpStatus.OK.value(),
                "Review unhidden"
        );
    }
}
