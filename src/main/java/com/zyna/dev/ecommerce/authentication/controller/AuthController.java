package com.zyna.dev.ecommerce.authentication.controller;

import com.zyna.dev.ecommerce.authentication.dto.request.IntrospectRequest;
import com.zyna.dev.ecommerce.authentication.dto.request.LoginRequest;
import com.zyna.dev.ecommerce.authentication.dto.request.RefreshTokenRequest;
import com.zyna.dev.ecommerce.authentication.dto.request.RegisterRequest;
import com.zyna.dev.ecommerce.authentication.dto.request.ResendActivationRequest;
import com.zyna.dev.ecommerce.authentication.dto.request.ChangeEmailRequest;

import com.zyna.dev.ecommerce.authentication.dto.request.ChangePasswordRequest;
import com.zyna.dev.ecommerce.authentication.dto.request.ForgotPasswordRequest;
import com.zyna.dev.ecommerce.authentication.dto.request.ResetPasswordRequest;
import com.zyna.dev.ecommerce.authentication.dto.response.IntrospectResponse;
import com.zyna.dev.ecommerce.authentication.dto.response.LoginResponse;
import com.zyna.dev.ecommerce.authentication.dto.response.RefreshTokenResponse;
import com.zyna.dev.ecommerce.authentication.service.interfaces.AuthService;
import com.zyna.dev.ecommerce.common.ApiResponse;
import com.zyna.dev.ecommerce.users.dto.response.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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
                "Register successfully! Please check your email to activate your account.",
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

    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<RefreshTokenResponse> refresh(
            HttpServletRequest httpRequest,
            @RequestBody(required = false) RefreshTokenRequest request
    ) {
        String refreshToken = null;

        if (request != null && request.getRefreshToken() != null && !request.getRefreshToken().isBlank()) {
            refreshToken = request.getRefreshToken();
        } else if (httpRequest.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : httpRequest.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshToken == null || refreshToken.isBlank()) {
            return ApiResponse.failedResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "Refresh token not provided"
            );
        }

        RefreshTokenResponse response = authService.refreshToken(new RefreshTokenRequest(refreshToken));
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Token refreshed successfully!",
                response
        );
    }

    @GetMapping("/activate")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<UserResponse> activate(@RequestParam String token) {
        UserResponse response = authService.activateAccount(token);
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Account activated successfully!",
                response
        );
    }

    @PostMapping("/activate/resend")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> resendActivation(@RequestBody @Valid ResendActivationRequest request) {
        authService.resendActivationEmail(request.getEmail());
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Activation email re-sent successfully!"
        );
    }

    @PostMapping("/change-email")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UserResponse> changeEmail(@RequestBody @Valid ChangeEmailRequest request) {
        UserResponse response = authService.changeEmail(request);
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Email updated. Please check the new inbox to activate your account.",
                response
        );
    }

    @PostMapping("/change-password")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        authService.changePassword(request);
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Password changed successfully!"
        );
    }

    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "OTP has been sent to your email."
        );
    }

    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Password has been reset successfully!"
        );
    }


    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> logout(
            HttpServletRequest request,
            @RequestBody(required = false) RefreshTokenRequest refreshReq
    ) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            authService.logout(token, refreshReq != null ? refreshReq.getRefreshToken() : null);
        }

        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Logout successfully!"
        );
    }

}
