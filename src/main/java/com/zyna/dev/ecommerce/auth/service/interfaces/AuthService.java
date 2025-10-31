package com.zyna.dev.ecommerce.auth.service.interfaces;

import com.zyna.dev.ecommerce.auth.dto.request.IntrospectRequest;
import com.zyna.dev.ecommerce.auth.dto.request.LoginRequest;
import com.zyna.dev.ecommerce.auth.dto.request.RegisterRequest;
import com.zyna.dev.ecommerce.auth.dto.response.IntrospectResponse;
import com.zyna.dev.ecommerce.auth.dto.response.LoginResponse;
import com.zyna.dev.ecommerce.user.dto.response.UserResponse;

public interface AuthService {
    LoginResponse login(LoginRequest loginRequest);

    UserResponse register(RegisterRequest registerRequest);

    IntrospectResponse introspect(IntrospectRequest introspectRequest);
}
