package com.zyna.dev.ecommerce.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final StringRedisTemplate redisTemplate;

    private String buildKey(String token) {
        return "blacklist:token:" + token;
    }

    /**
     * Đưa token vào blacklist với TTL (thời gian sống còn lại của token)
     */
    public void blacklist(String token, long millisToExpire) {
        if (millisToExpire <= 0) {
            return; // token sắp hết hạn rồi, khỏi lưu
        }

        String key = buildKey(token);
        redisTemplate.opsForValue().set(key, "1", millisToExpire, TimeUnit.MILLISECONDS);
    }

    /**
     * Kiểm tra token có nằm trong blacklist không
     */
    public boolean isBlacklisted(String token) {
        String key = buildKey(token);
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }
}
