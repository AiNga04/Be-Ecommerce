package com.zyna.dev.ecommerce.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import java.util.List;


@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private int statusCode;
    private String message;
    boolean success = false;
    /**
     * Can be a hashmap or list Spring will render a nice Json response :-)
     */
    private T data;

    public ApiResponse(int statCode, String statusDesc) {
        statusCode = statCode;
        message = statusDesc;

        if (statusCode == HttpStatus.OK.value()) {
            success = true;
        }

    }

    public ApiResponse() {
    }

    public static <T> ApiResponse<T> failedResponse(String message) {
        return failedResponse(HttpStatus.BAD_REQUEST.value(), message, null);
    }

    public static <T> ApiResponse<T> failedResponse(T data) {
        return failedResponse(HttpStatus.BAD_REQUEST.value(), "Bad request", data);
    }

    public static <T> ApiResponse<T> failedResponse(int statusCode, String message) {
        return failedResponse(statusCode, message, null);
    }

    public static <T> ApiResponse<T> failedResponse(int statusCode, String message, T data) {
        ApiResponse<T> response = new ApiResponse<>(statusCode, message);
        response.setSuccess(false);
        response.setData(data);
        return response;
    }

    public static <T> ApiResponse<T> successfulResponse(int statusCode, String message) {
        return successfulResponseNoData(statusCode, message);
    }


    public static <T> ApiResponse<T> successfulResponse(String message, T data) {
        return successfulResponse(HttpStatus.OK.value(), message, data);
    }

    public static <T> ApiResponse<T> successfulResponse(String message) {
        return successfulResponse(HttpStatus.OK.value(), message, null);
    }

    public static <T> ApiResponse<T> successfulResponse(int statusCode, String message, T data) {
        ApiResponse<T> response = new ApiResponse<>(statusCode, message);
        response.setSuccess(true);
        response.setData(data);
        return response;
    }

    public static <T> ApiResponse<T> successfulResponseNoData(int statusCode, String message) {
        ApiResponse<T> response = new ApiResponse<>(statusCode, message);
        response.setSuccess(true);
        return response;
    }

    private Pagination pagination;

    public static <T> ApiResponse<List<T>> successfulPageResponse(String message, Page<T> page) {
        return successfulPageResponse(HttpStatus.OK.value(), message, page);
    }

    public static <T> ApiResponse<List<T>> successfulPageResponse(int statusCode, String message, Page<T> page) {
        ApiResponse<List<T>> response = new ApiResponse<>(statusCode, message);
        response.setSuccess(true);
        response.setData(page.getContent());
        response.setPagination(Pagination.builder()
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .build());
        return response;
    }

    @Getter
    @Setter
    @lombok.Builder
    public static class Pagination {
        private int pageNumber;
        private int pageSize;
        private int totalPages;
        private long totalElements;
    }

}

