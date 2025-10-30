package com.zyna.dev.ecommerce.security;

import com.zyna.dev.ecommerce.user.User;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    // nhớ thay bằng secret thực sự dài 32+ ký tự
    private static final String SECRET = "replace-with-very-long-secret-key-of-32+-chars-123456";
    private static final long EXPIRATION_MS = 1000L * 60 * 60 * 24; // 24h

    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    // ✅ generate từ User
    public String generateToken(User user) {
        long now = System.currentTimeMillis();

        return Jwts.builder()
                .setSubject(user.getEmail())               // sẽ dùng email làm subject
                .claim("id", user.getId())                  // claim thêm, sau này dễ lấy
                .claim("role", user.getRole() != null ? user.getRole().name() : null)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + EXPIRATION_MS))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // nếu bạn muốn generate từ email cũng được, giữ lại cho tiện
    public String generateToken(String username) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + EXPIRATION_MS))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
