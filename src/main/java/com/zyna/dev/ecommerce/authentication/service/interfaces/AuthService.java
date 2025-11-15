package com.zyna.dev.ecommerce.authentication.service.interfaces;

import com.zyna.dev.ecommerce.authentication.dto.request.IntrospectRequest;
import com.zyna.dev.ecommerce.authentication.dto.request.LoginRequest;
import com.zyna.dev.ecommerce.authentication.dto.request.RegisterRequest;
import com.zyna.dev.ecommerce.authentication.dto.response.IntrospectResponse;
import com.zyna.dev.ecommerce.authentication.dto.response.LoginResponse;
import com.zyna.dev.ecommerce.users.dto.response.UserResponse;

public interface AuthService {
    LoginResponse login(LoginRequest loginRequest);

    UserResponse register(RegisterRequest registerRequest);

    IntrospectResponse introspect(IntrospectRequest introspectRequest);

    void logout(String token);
}
