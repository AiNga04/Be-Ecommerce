package com.zyna.dev.ecommerce.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zyna.dev.ecommerce.common.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException, ServletException {

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json");

        ApiResponse<?> api = ApiResponse.failedResponse(
                HttpStatus.FORBIDDEN.value(),
                "Access Denied! You don't have permission to perform this action."
        );

        response.getWriter().write(mapper.writeValueAsString(api));
    }
}
