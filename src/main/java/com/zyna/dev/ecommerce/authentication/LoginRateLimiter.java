package com.zyna.dev.ecommerce.authentication;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS = 5;       // số lần đăng nhập sai tối đa
    private static final long WINDOW_MINUTES = 10;   // khoảng thời gian tính (phút)
    private static final long BLOCK_MINUTES = 15;    // thời gian khóa (phút)

    private final StringRedisTemplate redisTemplate;

    private String attemptsKey(String email) {
        return "login:attempts:" + email;
    }

    private String blockKey(String email) {
        return "login:block:" + email;
    }

    /**
     * Kiểm tra user có đang bị block không
     */
    public boolean isBlocked(String email) {
        Boolean exists = redisTemplate.hasKey(blockKey(email));
        return Boolean.TRUE.equals(exists);
    }

    /**
     * Ghi nhận một lần login sai
     */
    public void recordFailedAttempt(String email) {
        String attemptsKey = attemptsKey(email);

        // Tăng số lần login sai
        Long attempts = redisTemplate.opsForValue().increment(attemptsKey);

        // Nếu vừa tạo key mới (attempts == 1) → set TTL cho cửa sổ 10 phút
        if (attempts != null && attempts == 1L) {
            redisTemplate.expire(attemptsKey, WINDOW_MINUTES, TimeUnit.MINUTES);
        }

        // Nếu vượt quá số lần cho phép → block user
        if (attempts != null && attempts >= MAX_ATTEMPTS) {
            String blockKey = blockKey(email);
            redisTemplate.opsForValue().set(
                    blockKey,
                    "1",
                    BLOCK_MINUTES,
                    TimeUnit.MINUTES
            );

            // Có thể xóa luôn attempts để tránh rác (optional)
            redisTemplate.delete(attemptsKey);
        }
    }

    /**
     * Reset lại count & block khi login thành công
     */
    public void resetAttempts(String email) {
        redisTemplate.delete(attemptsKey(email));
        redisTemplate.delete(blockKey(email));
    }
}
