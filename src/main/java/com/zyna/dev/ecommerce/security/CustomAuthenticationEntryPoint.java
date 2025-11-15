package com.zyna.dev.ecommerce.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zyna.dev.ecommerce.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");

        ApiResponse<?> api = ApiResponse.failedResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized! Please login again!"
        );

        response.getWriter().write(mapper.writeValueAsString(api));
    }
}
