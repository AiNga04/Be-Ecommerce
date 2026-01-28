package com.zyna.dev.ecommerce.authentication.service;

import com.zyna.dev.ecommerce.users.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

@Component
public class ActivationTokenProvider {

    @Value("${app.activation.jwt-secret}")
    private String activationSecret;

    @Value("${app.activation.ttl-hours:24}")
    private long ttlHours;

    private static final String CLAIM_TYPE = "type";
    private static final String CLAIM_EMAIL = "email";

    public String generate(User user) {
        Date now = new Date();
        Date expires = new Date(now.getTime() + Duration.ofHours(ttlHours > 0 ? ttlHours : 24).toMillis());

        return Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .claim(CLAIM_EMAIL, user.getEmail())
                .claim(CLAIM_TYPE, "activation")
                .setIssuedAt(now)
                .setExpiration(expires)
                .signWith(Keys.hmacShaKeyFor(activationSecret.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(activationSecret.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isActivationType(Claims claims) {
        Object type = claims.get(CLAIM_TYPE);
        return type != null && "activation".equals(type.toString());
    }

    public String getEmail(Claims claims) {
        Object email = claims.get(CLAIM_EMAIL);
        return email != null ? email.toString() : null;
    }
}
