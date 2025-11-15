package com.zyna.dev.ecommerce.authentication.service.impl;

import com.zyna.dev.ecommerce.authentication.mapper.AuthMapper;
import com.zyna.dev.ecommerce.authentication.repository.AppRoleRepository;
import com.zyna.dev.ecommerce.authentication.repository.AuthRepository;
import com.zyna.dev.ecommerce.authentication.LoginRateLimiter;
import com.zyna.dev.ecommerce.authentication.dto.request.IntrospectRequest;
import com.zyna.dev.ecommerce.authentication.dto.request.LoginRequest;
import com.zyna.dev.ecommerce.authentication.dto.request.RegisterRequest;
import com.zyna.dev.ecommerce.authentication.dto.response.IntrospectResponse;
import com.zyna.dev.ecommerce.authentication.dto.response.LoginResponse;
import com.zyna.dev.ecommerce.authentication.service.interfaces.AuthService;
import com.zyna.dev.ecommerce.common.enums.Status;
import com.zyna.dev.ecommerce.common.exceptions.ApplicationException;
import com.zyna.dev.ecommerce.security.JwtUtil;
import com.zyna.dev.ecommerce.security.TokenBlacklistService;
import com.zyna.dev.ecommerce.users.User;
import com.zyna.dev.ecommerce.users.dto.response.UserResponse;
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
    private final LoginRateLimiter rateLimiter;
    private final AppRoleRepository appRoleRepository;
    private final TokenBlacklistService tokenBlacklistService;


    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        String email = loginRequest.getEmail();

        // 0. Kiểm tra nếu user đang bị block
        if (rateLimiter.isBlocked(email)) {
            throw new ApplicationException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Too many failed attempts. Please try again later!"
            );
        }

        // 1. tìm user theo email (chỉ user chưa bị xóa mềm)
        User user = authRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> {
                    rateLimiter.recordFailedAttempt(email);
                    return new ApplicationException(HttpStatus.UNAUTHORIZED, "Invalid email or password!");
                });

        // 2. kiểm tra mật khẩu
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            rateLimiter.recordFailedAttempt(email);
            throw new ApplicationException(HttpStatus.UNAUTHORIZED, "Invalid email or password!");
        }

        // 3. kiểm tra trạng thái user
        if (user.getStatus() != null && user.getStatus() != Status.ACTIVE) {
            throw new ApplicationException(HttpStatus.UNAUTHORIZED, "Account is not active!");
        }

        // Reset đếm lỗi khi login thành công
        rateLimiter.resetAttempts(email);

        // 4. sinh JWT
        String token = jwtUtil.generateToken(user);

        // 5. map sang response (ẩn password)
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

        var userRole = appRoleRepository.findByCode("USER")
                .orElseThrow(() -> new ApplicationException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Default role USER is not configured!"
                ));
        user.getRoles().add(userRole);

        if (user.getStatus() == null) {
            user.setStatus(Status.ACTIVE);
        }

        User saved = authRepository.save(user);
        return authMapper.toUserResponse(saved);
    }

    @Override
    public IntrospectResponse introspect(IntrospectRequest introspectRequest) {
        String token = introspectRequest.getToken();

        boolean isValid = jwtUtil.validateToken(token);

        String email  = jwtUtil.extractUsername(token);

        if (!isValid) {
            throw new ApplicationException(HttpStatus.UNAUTHORIZED, "Invalid or expired token!");
        }

        return new IntrospectResponse(isValid, email);
    }

    @Override
    public void logout(String token) {
        if (!jwtUtil.validateToken(token)) {
            // token đã hết hạn hoặc invalid thì khỏi blacklist
            return;
        }

        // Lấy thời gian hết hạn của token
        var expiration = jwtUtil.extractExpiration(token); // Date
        long now = System.currentTimeMillis();
        long ttl = expiration.getTime() - now;

        tokenBlacklistService.blacklist(token, ttl);
    }

}
