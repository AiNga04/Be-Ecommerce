package com.zyna.dev.ecommerce.common.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.List;

@Getter
public class ApplicationException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final List<String> errors;
    private final Object data;

    // Trường hợp phổ biến nhất: chỉ status + message
    public ApplicationException(HttpStatus status, String message) {
        super(message);
        this.httpStatus = status;
        this.errors = null;
        this.data = null;
    }

    // Nếu muốn đính kèm data bổ sung
    public ApplicationException(HttpStatus status, String message, Object data) {
        super(message);
        this.httpStatus = status;
        this.errors = null;
        this.data = data;
    }

    // Nếu muốn gửi nhiều errors cụ thể
    public ApplicationException(HttpStatus status, String message, List<String> errors, Object data) {
        super(message);
        this.httpStatus = status;
        this.errors = errors;
        this.data = data;
    }
}
