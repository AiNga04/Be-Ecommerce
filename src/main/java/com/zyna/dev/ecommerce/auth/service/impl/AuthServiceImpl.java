package com.zyna.dev.ecommerce.auth.service.impl;

import com.zyna.dev.ecommerce.auth.AuthMapper;
import com.zyna.dev.ecommerce.auth.AuthRepository;
import com.zyna.dev.ecommerce.auth.dto.request.LoginRequest;
import com.zyna.dev.ecommerce.auth.dto.request.RegisterRequest;
import com.zyna.dev.ecommerce.auth.dto.response.LoginResponse;
import com.zyna.dev.ecommerce.auth.service.interfaces.AuthService;
import com.zyna.dev.ecommerce.common.enums.Status;
import com.zyna.dev.ecommerce.common.exceptions.ApplicationException;
import com.zyna.dev.ecommerce.security.JwtUtil;
import com.zyna.dev.ecommerce.user.User;
import com.zyna.dev.ecommerce.user.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthMapper authMapper;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        // 1. tìm user theo email (chỉ user chưa bị xóa mềm)
        User user = authRepository.findByEmailAndIsDeletedFalse(loginRequest.getEmail())
                .orElseThrow(() ->
                        new ApplicationException(HttpStatus.UNAUTHORIZED, "Invalid email or password")
                );

        // 2. kiểm tra
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new ApplicationException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        if (user.getStatus() != null && user.getStatus() != Status.ACTIVE) {
            throw new ApplicationException(HttpStatus.UNAUTHORIZED, "Account is not active");
        }

        // 3. sinh JWT
        String token = jwtUtil.generateToken(user);

        // 4. map sang response (ẩn password)
        UserResponse userResponse = authMapper.toUserResponse(user);

        return LoginResponse.builder()
                .token(token)
                .user(userResponse)
                .build();
    }

    @Override
    public UserResponse register(RegisterRequest registerRequest) {
        if (authRepository.existsByEmail(registerRequest.getEmail())) {
            throw new ApplicationException(HttpStatus.CONFLICT, "Email already in use!");
        }

        User user = authMapper.toUser(registerRequest);
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        user.setRole(com.zyna.dev.ecommerce.common.enums.Role.USER);

        if (user.getStatus() == null) {
            user.setStatus(Status.ACTIVE);
        }

        User saved = authRepository.save(user);
        return authMapper.toUserResponse(saved);
    }

}
