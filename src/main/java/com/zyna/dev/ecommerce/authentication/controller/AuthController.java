package com.zyna.dev.ecommerce.authentication.controller;

import com.zyna.dev.ecommerce.authentication.dto.request.IntrospectRequest;
import com.zyna.dev.ecommerce.authentication.dto.request.LoginRequest;
import com.zyna.dev.ecommerce.authentication.dto.request.RegisterRequest;
import com.zyna.dev.ecommerce.authentication.dto.response.IntrospectResponse;
import com.zyna.dev.ecommerce.authentication.dto.response.LoginResponse;
import com.zyna.dev.ecommerce.authentication.service.interfaces.AuthService;
import com.zyna.dev.ecommerce.common.ApiResponse;
import com.zyna.dev.ecommerce.users.dto.response.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<LoginResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
        LoginResponse loginResponse = authService.login(loginRequest);
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Login successfully!",
                loginResponse
        );
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserResponse> register(@RequestBody @Valid RegisterRequest registerRequest) {
        UserResponse userResponse = authService.register(registerRequest);
        return ApiResponse.successfulResponse(
                HttpStatus.CREATED.value(),
                "Register successfully!",
                userResponse
        );
    }

    @PostMapping("/introspect")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<IntrospectResponse> introspect(
            @RequestBody @Valid IntrospectRequest introspectRequest
    ) {
        IntrospectResponse introspectResponse = authService.introspect(introspectRequest);
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Token introspection successful!",
                introspectResponse
        );
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            authService.logout(token);
        }

        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Logout successfully!"
        );
    }
}
