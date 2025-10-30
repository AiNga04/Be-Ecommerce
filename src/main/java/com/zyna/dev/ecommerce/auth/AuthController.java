package com.zyna.dev.ecommerce.auth;

import com.zyna.dev.ecommerce.auth.dto.request.LoginRequest;
import com.zyna.dev.ecommerce.auth.dto.request.RegisterRequest;
import com.zyna.dev.ecommerce.auth.dto.response.LoginResponse;
import com.zyna.dev.ecommerce.auth.service.interfaces.AuthService;
import com.zyna.dev.ecommerce.common.ApiResponse;
import com.zyna.dev.ecommerce.user.dto.UserResponse;
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
}
