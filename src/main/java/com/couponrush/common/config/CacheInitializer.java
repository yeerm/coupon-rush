package com.couponrush.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheInitializer {

    private final StringRedisTemplate redisTemplate;

    /**
     * 앱 시작 시 쿠폰 정보 캐시(coupon:{id})만 초기화한다.
     * coupon:stock, coupon:issued, coupon:lock 키는 유지한다.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void evictCouponCache() {
        log.info("캐시 초기화 시작");

        Set<String> keys = redisTemplate.keys("coupon:*");
        if (keys == null || keys.isEmpty()) {
            log.info("초기화할 쿠폰 캐시 없음");
            return;
        }

        // coupon:{id} 형태만 필터링 (두 번째 콜론 없는 키)
        Set<String> cacheKeys = keys.stream()
                .filter(key -> key.indexOf(':', "coupon:".length()) == -1)
                .collect(Collectors.toSet());

        if (cacheKeys.isEmpty()) {
            log.info("초기화할 쿠폰 캐시 없음");
            return;
        }

        redisTemplate.delete(cacheKeys);
        log.info("쿠폰 캐시 초기화 완료: {}개 키 삭제 {}", cacheKeys.size(), cacheKeys);
    }
}
