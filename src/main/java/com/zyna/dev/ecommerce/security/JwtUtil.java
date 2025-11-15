package com.zyna.dev.ecommerce.security;

import com.zyna.dev.ecommerce.authentication.models.AppRole;
import com.zyna.dev.ecommerce.authentication.models.Permission;
import com.zyna.dev.ecommerce.users.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expirationMs;
    private final String issuer;

    public JwtUtil(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms:86400000}") long expirationMs,
            @Value("${app.jwt.issuer:zyna-app}") String issuer
    ) {
        this.expirationMs = expirationMs;
        this.issuer = issuer;
        this.key = parseSecret(secret);
    }

    private SecretKey parseSecret(String secret) {
        try {
            return Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
        } catch (IllegalArgumentException e) {
            return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        }
    }

    // Build permissions từ roles + permissions trong DB
    private Set<String> buildPermissions(User user) {
        Set<String> perms = new HashSet<>();

        if (user.getRoles() != null) {
            for (AppRole role : user.getRoles()) {
                if (role == null) continue;

                // authority theo role: ROLE_ADMIN, ROLE_USER, ...
                if (role.getCode() != null) {
                    perms.add("ROLE_" + role.getCode());
                }

                // authority theo permission: PRODUCT_READ, PRODUCT_WRITE, ...
                if (role.getPermissions() != null) {
                    for (Permission p : role.getPermissions()) {
                        if (p != null && p.getName() != null) {
                            perms.add(p.getName());
                        }
                    }
                }
            }
        }

        // fallback: nếu không có gì hết, vẫn cho đọc product
        if (perms.isEmpty()) {
            perms.add("PRODUCT_READ");
        }

        return perms;
    }

    // Generate JWT chứa list permissions
    public String generateToken(User user) {
        long now = System.currentTimeMillis();

        Set<String> permissions = buildPermissions(user);

        return Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuer(issuer)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationMs))
                .claim("id", user.getId())
                .claim("permissions", permissions) // nhét list permission / role-code vào token
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .requireIssuer(issuer)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    // Lấy authorities (permissions) từ claim "permissions"
    public Collection<? extends GrantedAuthority> extractAuthorities(String token) {
        Claims claims = extractAllClaims(token);
        List<String> permissions = claims.get("permissions", List.class);
        if (permissions == null) return List.of();

        return permissions.stream()
                .map(SimpleGrantedAuthority::new) // hasAuthority("PRODUCT_WRITE") / hasAuthority("ROLE_ADMIN")
                .collect(Collectors.toList());
    }
}
