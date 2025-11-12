package com.zyna.dev.ecommerce.authentication;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS = 5;       // số lần đăng nhập sai tối đa
    private static final long WINDOW_MINUTES = 10;   // khoảng thời gian tính (phút)
    private static final long BLOCK_MINUTES = 15;    // thời gian khóa (phút)

    private final Map<String, AttemptInfo> attempts = new ConcurrentHashMap<>();

    public boolean isBlocked(String email) {
        AttemptInfo info = attempts.get(email);
        if (info == null) return false;

        // Nếu user đang bị block và chưa hết hạn block
        if (info.isBlocked && Instant.now().isBefore(info.blockedUntil)) {
            return true;
        }

        // Nếu hết thời gian block → reset
        if (info.isBlocked && Instant.now().isAfter(info.blockedUntil)) {
            attempts.remove(email);
            return false;
        }

        return false;
    }

    public void recordFailedAttempt(String email) {
        AttemptInfo info = attempts.getOrDefault(email, new AttemptInfo());
        long now = Instant.now().toEpochMilli();

        // nếu lần đầu tiên hoặc quá cửa sổ 10 phút → reset
        if (info.firstAttemptTime == 0 || (now - info.firstAttemptTime) > WINDOW_MINUTES * 60 * 1000) {
            info.firstAttemptTime = now;
            info.attempts = 1;
        } else {
            info.attempts++;
        }

        // nếu vượt quá số lần cho phép → block
        if (info.attempts >= MAX_ATTEMPTS) {
            info.isBlocked = true;
            info.blockedUntil = Instant.now().plusSeconds(BLOCK_MINUTES * 60);
        }

        attempts.put(email, info);
    }

    public void resetAttempts(String email) {
        attempts.remove(email);
    }

    private static class AttemptInfo {
        int attempts = 0;
        long firstAttemptTime = 0;
        boolean isBlocked = false;
        Instant blockedUntil;
    }
}
