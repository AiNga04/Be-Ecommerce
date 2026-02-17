package com.zyna.dev.ecommerce.authentication.service;

import com.zyna.dev.ecommerce.common.enums.Status;
import com.zyna.dev.ecommerce.common.enums.UserAuditAction;
import com.zyna.dev.ecommerce.common.exceptions.ApplicationException;
import com.zyna.dev.ecommerce.common.mail.MailService;
import com.zyna.dev.ecommerce.users.models.User;
import com.zyna.dev.ecommerce.users.UserRepository;
import com.zyna.dev.ecommerce.users.service.UserAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AccountActivationService {

    private final UserRepository userRepository;
    private final MailService mailService;
    private final UserAuditService userAuditService;
    private final ActivationTokenProvider activationTokenProvider;

    @Value("${app.activation.base-url:http://localhost:3000/activate}")
    private String activationBaseUrl;

    public void sendActivationToken(User user, String actorEmail) {
        sendActivationToken(user, actorEmail, null, com.zyna.dev.ecommerce.common.enums.ActivationType.REGISTRATION);
    }

    public void sendActivationToken(User user, String actorEmail, com.zyna.dev.ecommerce.common.enums.ActivationType type) {
        sendActivationToken(user, actorEmail, null, type);
    }

    public void sendActivationToken(User user, String actorEmail, String rawPassword) {
        sendActivationToken(user, actorEmail, rawPassword, com.zyna.dev.ecommerce.common.enums.ActivationType.REGISTRATION);
    }

    public void sendActivationToken(User user, String actorEmail, String rawPassword, com.zyna.dev.ecommerce.common.enums.ActivationType type) {
        String token = activationTokenProvider.generate(user);
        String activationLink = buildActivationLink(token);
        if (StringUtils.hasText(rawPassword)) {
            mailService.sendActivationEmail(user, activationLink, rawPassword, type);
        } else {
            mailService.sendActivationEmail(user, activationLink, type);
        }

        userAuditService.record(
                user,
                actorEmail,
                UserAuditAction.CREATE_USER,
                "Account created; activation email sent (" + type + ")"
        );
    }

    @Transactional
    public User activate(String rawToken) {
        if (!StringUtils.hasText(rawToken)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Activation token is required");
        }

        var claims = activationTokenProvider.parse(rawToken);
        if (!activationTokenProvider.isActivationType(claims)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Invalid activation token type");
        }

        Long userId;
        try {
            userId = Long.parseLong(claims.getSubject());
        } catch (NumberFormatException ex) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Invalid activation token payload");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.BAD_REQUEST, "User not found"));

        if (user.isDeleted()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "User was deleted and cannot be activated");
        }

        String emailInToken = activationTokenProvider.getEmail(claims);
        if (StringUtils.hasText(emailInToken) && !user.getEmail().equalsIgnoreCase(emailInToken)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Activation token does not match current email");
        }

        user.setStatus(Status.ACTIVE);
        userRepository.save(user);

        userAuditService.record(
                user,
                user.getEmail(),
                UserAuditAction.ACTIVATE_USER,
                "Account activated via email link"
        );

        return user;
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
