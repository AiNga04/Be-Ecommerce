package com.zyna.dev.ecommerce.authentication.service;

import com.zyna.dev.ecommerce.authentication.models.PasswordResetToken;
import com.zyna.dev.ecommerce.authentication.repository.PasswordResetTokenRepository;
import com.zyna.dev.ecommerce.common.exceptions.ApplicationException;
import com.zyna.dev.ecommerce.common.mail.MailService;
import com.zyna.dev.ecommerce.users.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final MailService mailService;

    @Value("${app.password-reset.ttl-minutes:10}")
    private long otpTtlMinutes;

    public void createAndSendOtp(User user) {
        PasswordResetToken token = buildToken(user);
        passwordResetTokenRepository.save(token);
        mailService.sendPasswordResetOtp(user, token.getOtpCode());
    }

    public void validateOtp(User user, String otpCode) {
        PasswordResetToken token = passwordResetTokenRepository
                .findTopByUserAndUsedFalseOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new ApplicationException(
                        HttpStatus.BAD_REQUEST,
                        "No active OTP. Please request a new one."
                ));

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "OTP expired. Please request a new one.");
        }

        if (!token.getOtpCode().equals(otpCode)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "OTP is invalid.");
        }

        token.setUsed(true);
        token.setUsedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(token);
    }

    private PasswordResetToken buildToken(User user) {
        Duration ttl = Duration.ofMinutes(otpTtlMinutes > 0 ? otpTtlMinutes : 10);
        LocalDateTime expiresAt = LocalDateTime.now().plus(ttl);
        String otp = generateOtp();

        return PasswordResetToken.builder()
                .user(user)
                .otpCode(otp)
                .expiresAt(expiresAt)
                .build();
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000); // 6 digits
        return String.valueOf(code);
    }
}
