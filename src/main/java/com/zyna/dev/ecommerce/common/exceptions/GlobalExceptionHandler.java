package com.zyna.dev.ecommerce.common.exceptions;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.zyna.dev.ecommerce.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;

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
                    : "Giá trị không hợp lệ";
            fieldErrors.put(fieldName, message);
        });

        ApiResponse<Map<String, String>> body = ApiResponse.failedResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Xác thực không thành công",
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
                    "Định dạng không hợp lệ cho '%s'. Kiểu dữ liệu mong đợi: %s",
                    fieldName, ife.getTargetType().getSimpleName()
            );
            errors.put(fieldName, errorMessage);
        } else {
            errors.put("general", "Dữ liệu JSON đầu vào không hợp lệ. Vui lòng kiểm tra định dạng và kiểu dữ liệu");
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(                                                      
                ApiResponse.failedResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        "Dữ liệu JSON đầu vào không hợp lệ",
                        errors
                )
        );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleNoResource(NoResourceFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiResponse.failedResponse(
                        HttpStatus.NOT_FOUND.value(),
                        "Không tìm thấy tài nguyên",
                        ex.getMessage()
                )
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<String>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(
                ApiResponse.failedResponse(
                        HttpStatus.METHOD_NOT_ALLOWED.value(),
                        "Phương thức không được hỗ trợ",
                        ex.getMessage()
                )
        );
    }

    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<String>> handleTypeMismatch(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException ex) {
        String name = ex.getName();
        String type = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        Object value = ex.getValue();
        String message = String.format("Tham số '%s' phải có kiểu dữ liệu là '%s'", name, type);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse.failedResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        "Kiểu tham số không hợp lệ",
                        message
                )
        );
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleUnexpected(Exception ex) {
        ex.printStackTrace();

        ApiResponse<String> body = ApiResponse.failedResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Lỗi hệ thống nội bộ",
                ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
