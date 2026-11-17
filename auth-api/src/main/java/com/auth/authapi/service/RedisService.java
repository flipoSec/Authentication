package com.auth.authapi.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;

@Service
public class RedisService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    public void revokeToken(String jti, Date expiration) {
        long ttl = expiration.getTime() - System.currentTimeMillis();
        if (ttl <= 0) {
            return;
        }

        String key = "revoked:" + jti;

        redisTemplate.opsForValue().set(
                key,
                "true",
                Duration.ofMillis(ttl)
        );
    }

    public boolean isTokenRevoked(String jti) {
        String key = "revoked:" + jti;

        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

}
