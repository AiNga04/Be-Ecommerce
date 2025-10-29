package com.zyna.dev.ecommerce.common.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.List;

@Getter
public class ApplicationException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final List<String> errors;
    private final Object data;

    public ApplicationException(String message) {
        this(HttpStatus.BAD_REQUEST, message, Collections.singletonList(message), null);
    }

    public ApplicationException(HttpStatus status, String message) {
        this(status, message, Collections.singletonList(message), null);
    }

    public ApplicationException(HttpStatus status, String message, Object data) {
        this(status, message, Collections.singletonList(message), data);
    }

    public ApplicationException(HttpStatus status, String message, List<String> errors, Object data) {
        super(message);
        this.httpStatus = status;
        this.errors = errors;
        this.data = data;
    }
}
