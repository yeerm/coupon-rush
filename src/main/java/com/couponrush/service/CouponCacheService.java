package com.couponrush.service;

import com.couponrush.dto.response.CouponResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponCacheService {

    private static final String CACHE_KEY = "coupon:%d";
    // TTL = 하루(86400)
    private static final Duration TTL = Duration.ofSeconds(86400);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Redis에서 쿠폰 캐시를 조회한다.
     */
    public Optional<CouponResponse> getCache(Long couponId) {
        String key = CACHE_KEY.formatted(couponId);
        String value = redisTemplate.opsForValue().get(key);

        if (value == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(objectMapper.readValue(value, CouponResponse.class));
        } catch (JsonProcessingException e) {
            log.warn("쿠폰 캐시 역직렬화 실패: couponId={}, error={}", couponId, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 쿠폰 정보를 Redis에 JSON으로 직렬화하여 저장한다. (TTL: 86400초)
     */
    public void setCache(Long couponId, CouponResponse response) {
        String key = CACHE_KEY.formatted(couponId);

        try {
            String value = objectMapper.writeValueAsString(response);
            redisTemplate.opsForValue().set(key, value, TTL);
            log.info("쿠폰 캐시 저장: couponId={}", couponId);
        } catch (JsonProcessingException e) {
            log.warn("쿠폰 캐시 직렬화 실패: couponId={}, error={}", couponId, e.getMessage());
        }
    }

    /**
     * 쿠폰 캐시를 삭제한다. (쿠폰 정보 수정 시 호출)
     */
    public void evictCache(Long couponId) {
        String key = CACHE_KEY.formatted(couponId);
        redisTemplate.delete(key);
        log.info("쿠폰 캐시 삭제: couponId={}", couponId);
    }
}
