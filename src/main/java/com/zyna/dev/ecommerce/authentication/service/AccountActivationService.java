package com.zyna.dev.ecommerce.authentication.service;

import com.zyna.dev.ecommerce.authentication.models.AccountActivationToken;
import com.zyna.dev.ecommerce.authentication.repository.AccountActivationTokenRepository;
import com.zyna.dev.ecommerce.common.enums.Status;
import com.zyna.dev.ecommerce.common.enums.UserAuditAction;
import com.zyna.dev.ecommerce.common.exceptions.ApplicationException;
import com.zyna.dev.ecommerce.common.mail.MailService;
import com.zyna.dev.ecommerce.users.User;
import com.zyna.dev.ecommerce.users.UserRepository;
import com.zyna.dev.ecommerce.users.service.UserAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountActivationService {

    private final AccountActivationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final MailService mailService;
    private final UserAuditService userAuditService;

    @Value("${app.activation.base-url:http://localhost:8080/activate}")
    private String activationBaseUrl;

    @Value("${app.activation.ttl-hours:24}")
    private long ttlHours;

    public void sendActivationToken(User user, String actorEmail) {
        AccountActivationToken token = createToken(user);
        String activationLink = buildActivationLink(token.getToken());
        mailService.sendActivationEmail(user, activationLink);

        userAuditService.record(
                user,
                actorEmail,
                UserAuditAction.CREATE_USER,
                "Account created; activation email sent"
        );
    }

    @Transactional
    public User activate(String rawToken) {
        if (!StringUtils.hasText(rawToken)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Activation token is required");
        }

        AccountActivationToken token = tokenRepository.findByToken(rawToken)
                .orElseThrow(() -> new ApplicationException(
                        HttpStatus.BAD_REQUEST,
                        "Activation token is invalid"
                ));

        if (token.isUsed()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Activation token has already been used");
        }

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Activation token is expired");
        }

        User user = token.getUser();

        if (user.isDeleted()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "User was deleted and cannot be activated");
        }

        user.setStatus(Status.ACTIVE);
        token.setUsed(true);
        token.setUsedAt(LocalDateTime.now());

        tokenRepository.save(token);
        userRepository.save(user);

        userAuditService.record(
                user,
                user.getEmail(),
                UserAuditAction.ACTIVATE_USER,
                "Account activated via email link"
        );

        return user;
    }

    private AccountActivationToken createToken(User user) {
        tokenRepository.deleteAllByUser(user);

        Duration ttl = Duration.ofHours(ttlHours > 0 ? ttlHours : 24);
        LocalDateTime expiresAt = LocalDateTime.now().plus(ttl);

        AccountActivationToken token = AccountActivationToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiresAt(expiresAt)
                .build();

        return tokenRepository.save(token);
    }

    private String buildActivationLink(String token) {
        String base = activationBaseUrl.endsWith("/")
                ? activationBaseUrl.substring(0, activationBaseUrl.length() - 1)
                : activationBaseUrl;

        if (base.contains("?")) {
            return base + "&token=" + token;
        }

        return base + "?token=" + token;
    }
}
