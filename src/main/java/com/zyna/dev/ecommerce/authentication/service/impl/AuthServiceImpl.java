package com.zyna.dev.ecommerce.authentication.service.impl;

import com.zyna.dev.ecommerce.authentication.dto.request.RefreshTokenRequest;
import com.zyna.dev.ecommerce.authentication.dto.response.RefreshTokenResponse;
import com.zyna.dev.ecommerce.authentication.mapper.AuthMapper;
import com.zyna.dev.ecommerce.authentication.models.RefreshToken;
import com.zyna.dev.ecommerce.authentication.repository.AppRoleRepository;
import com.zyna.dev.ecommerce.authentication.repository.AuthRepository;
import com.zyna.dev.ecommerce.authentication.LoginRateLimiter;
import com.zyna.dev.ecommerce.authentication.dto.request.IntrospectRequest;
import com.zyna.dev.ecommerce.authentication.dto.request.LoginRequest;
import com.zyna.dev.ecommerce.authentication.dto.request.RegisterRequest;
import com.zyna.dev.ecommerce.authentication.dto.response.IntrospectResponse;
import com.zyna.dev.ecommerce.authentication.dto.response.LoginResponse;
import com.zyna.dev.ecommerce.authentication.repository.RefreshTokenRepository;
import com.zyna.dev.ecommerce.authentication.service.interfaces.AuthService;
import com.zyna.dev.ecommerce.authentication.service.AccountActivationService;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

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
    private final RefreshTokenRepository  refreshTokenRepository;
    private final AccountActivationService accountActivationService;


    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        String email = loginRequest.getEmail();

        if (rateLimiter.isBlocked(email)) {
            throw new ApplicationException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Too many failed attempts. Please try again later!"
            );
        }

        User user = authRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> {
                    rateLimiter.recordFailedAttempt(email);
                    return new ApplicationException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
                });

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            rateLimiter.recordFailedAttempt(email);
            throw new ApplicationException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        if (user.getStatus() != null && user.getStatus() != Status.ACTIVE) {
            throw new ApplicationException(HttpStatus.UNAUTHORIZED, "Account is not active. Please check your email for the activation link.");
        }

        rateLimiter.resetAttempts(email);

        // 4. sinh Access Token (như cũ)
        String accessToken = jwtUtil.generateToken(user);

        // 5. sinh Refresh Token (random string)
        String refreshToken = UUID.randomUUID().toString(); // có thể dùng secureRandom, nhưng tạm ổn

        // 6. lưu refresh token vào DB
        LocalDateTime refreshExpiresAt = LocalDateTime.now().plusDays(7); // ví dụ 7 ngày
        RefreshToken rt = RefreshToken.builder()
                .user(user)
                .token(refreshToken)
                .expiresAt(refreshExpiresAt)
//                .createdByIp(loginRequest.getIp())      // nếu có field này
//                .userAgent(loginRequest.getUserAgent()) // nếu có
                .build();
        refreshTokenRepository.save(rt);

        UserResponse userResponse = authMapper.toUserResponse(user);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userResponse)
                .build();
    }

    @Override
    @Transactional
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
            user.setStatus(Status.PENDING);
        }

        User saved = authRepository.save(user);
        accountActivationService.sendActivationToken(saved, saved.getEmail());
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
    @Transactional
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        String refreshTokenStr = request.getRefreshToken();

        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenStr)
                .orElseThrow(() -> new ApplicationException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid refresh token"
                ));

        if (refreshToken.isRevoked() || refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ApplicationException(HttpStatus.UNAUTHORIZED, "Refresh token is expired or revoked");
        }

        User user = refreshToken.getUser();  // 👈 lúc này session còn mở

        // 👉 Ở đây gọi generateToken sẽ an toàn, vì user.getRoles() lazy load được
        String newAccessToken = jwtUtil.generateToken(user);

        // rotation (nếu bạn làm):
        String newRefreshToken = UUID.randomUUID().toString();
        LocalDateTime newExpiresAt = LocalDateTime.now().plusDays(7);

        refreshToken.setRevoked(true);
        refreshToken.setRevokedAt(LocalDateTime.now());

        RefreshToken newRt = RefreshToken.builder()
                .user(user)
                .token(newRefreshToken)
                .expiresAt(newExpiresAt)
                .build();

        refreshTokenRepository.save(refreshToken);
        refreshTokenRepository.save(newRt);

        return RefreshTokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }


    @Override
    public void logout(String accessToken, String refreshTokenStr) {
        // 1. blacklist access token như bạn đã làm
        if (jwtUtil.validateToken(accessToken)) {
            Date expiration = jwtUtil.extractExpiration(accessToken);
            long now = System.currentTimeMillis();
            long ttl = expiration.getTime() - now;
            tokenBlacklistService.blacklist(accessToken, ttl);
        }

        // 2. revoke refresh token trong DB (nếu có)
        if (refreshTokenStr != null) {
            refreshTokenRepository.findByToken(refreshTokenStr).ifPresent(rt -> {
                rt.setRevoked(true);
                rt.setRevokedAt(LocalDateTime.now());
                refreshTokenRepository.save(rt);
            });
        }
    }

    @Override
    public UserResponse activateAccount(String token) {
        User activated = accountActivationService.activate(token);
        return authMapper.toUserResponse(activated);
    }


}
