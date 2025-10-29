package com.zyna.dev.ecommerce.common.exceptions;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.zyna.dev.ecommerce.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Lỗi validate đầu vào (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = error instanceof FieldError fe ? fe.getField() : error.getObjectName();
            String message = error.getDefaultMessage() != null
                    ? error.getDefaultMessage()
                    : "Invalid value!";
            fieldErrors.put(fieldName, message);
        });

        ApiResponse<Map<String, String>> body = ApiResponse.failedResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed!",
                fieldErrors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ApiResponse<Object>> handleApplicationException(ApplicationException ex) {

        Object payload = null;

        // Ưu tiên errors nếu có
        if (ex.getErrors() != null && !ex.getErrors().isEmpty()) {
            payload = Map.of("errors", ex.getErrors());
        }
        // Nếu không có errors mà có data tuỳ biến -> dùng data
        else if (ex.getData() != null) {
            payload = ex.getData();
        }

        ApiResponse<Object> body = ApiResponse.failedResponse(
                ex.getHttpStatus().value(),
                ex.getMessage(),
                payload
        );

        return ResponseEntity.status(ex.getHttpStatus()).body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, WebRequest request) {

        Map<String, String> errors = new HashMap<>();

        if (ex.getCause() instanceof InvalidFormatException ife) {
            String fieldName = ife.getPath().stream()
                    .map(JsonMappingException.Reference::getFieldName)
                    .filter(name -> name != null)
                    .findFirst()
                    .orElse("unknown");
            String errorMessage = String.format(
                    "Invalid format for '%s'. Expected type: %s",
                    fieldName, ife.getTargetType().getSimpleName()
            );
            errors.put(fieldName, errorMessage);
        } else {
            errors.put("general", "Invalid JSON data. Please check format and field types!");
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse.failedResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        "Invalid JSON input!",
                        errors
                )
        );
    }

    // fallback - lỗi không đoán trước
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleUnexpected(Exception ex) {
        ex.printStackTrace();

        ApiResponse<String> body = ApiResponse.failedResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal server error!",
                ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
