package com.zyna.dev.ecommerce.authentication.service.interfaces;

import com.zyna.dev.ecommerce.authentication.dto.request.IntrospectRequest;
import com.zyna.dev.ecommerce.authentication.dto.request.LoginRequest;
import com.zyna.dev.ecommerce.authentication.dto.request.ChangeEmailRequest;
import com.zyna.dev.ecommerce.authentication.dto.request.ChangePasswordRequest;
import com.zyna.dev.ecommerce.authentication.dto.request.ForgotPasswordRequest;
import com.zyna.dev.ecommerce.authentication.dto.request.RefreshTokenRequest;
import com.zyna.dev.ecommerce.authentication.dto.request.RegisterRequest;
import com.zyna.dev.ecommerce.authentication.dto.request.ResetPasswordRequest;
import com.zyna.dev.ecommerce.authentication.dto.response.IntrospectResponse;
import com.zyna.dev.ecommerce.authentication.dto.response.LoginResponse;
import com.zyna.dev.ecommerce.authentication.dto.response.RefreshTokenResponse;
import com.zyna.dev.ecommerce.users.dto.response.UserResponse;

public interface AuthService {
    LoginResponse login(LoginRequest loginRequest);

    UserResponse register(RegisterRequest registerRequest);

    IntrospectResponse introspect(IntrospectRequest introspectRequest);

    RefreshTokenResponse refreshToken(RefreshTokenRequest refreshTokenRequest);

    void logout(String accessToken, String refreshTokenStr);

    UserResponse activateAccount(String token);

    void resendActivationEmail(String email);

    UserResponse changeEmail(ChangeEmailRequest request);

    void changePassword(ChangePasswordRequest request);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}
