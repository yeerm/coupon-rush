package com.couponrush.service;

import com.couponrush.common.exception.BusinessException;
import com.couponrush.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponIssueRedisService {

    private static final String COUPON_STOCK_KEY = "coupon:stock:%d";
    private static final String COUPON_ISSUED_KEY = "coupon:issued:%d";

    private final StringRedisTemplate redisTemplate;

    /**
     * Redis에서 쿠폰 재고를 감소시키고 발급 가능 여부를 반환한다.
     * Cache Aside 전략: Redis 선처리 후 DB에 반영
     */
    public void decreaseStock(Long couponId, Long userId) {
        String stockKey = COUPON_STOCK_KEY.formatted(couponId);
        String issuedKey = COUPON_ISSUED_KEY.formatted(couponId);

        // 중복 발급 체크
        Boolean alreadyIssued = redisTemplate.opsForSet().isMember(issuedKey, String.valueOf(userId));
        if (Boolean.TRUE.equals(alreadyIssued)) {
            throw new BusinessException(ErrorCode.ALREADY_ISSUED);
        }

        // 재고 감소
        Long remaining = redisTemplate.opsForValue().decrement(stockKey);
        if (remaining == null || remaining < 0) {
            // 재고 초과 시 롤백
            redisTemplate.opsForValue().increment(stockKey);
            throw new BusinessException(ErrorCode.COUPON_EXHAUSTED);
        }

        // 발급 이력 추가
        redisTemplate.opsForSet().add(issuedKey, String.valueOf(userId));
    }

    /**
     * 쿠폰 재고를 Redis에 초기화한다.
     */
    public void initStock(Long couponId, int totalQuantity) {
        String stockKey = COUPON_STOCK_KEY.formatted(couponId);
        redisTemplate.opsForValue().set(stockKey, String.valueOf(totalQuantity));
        log.info("쿠폰 재고 Redis 초기화: couponId={}, totalQuantity={}", couponId, totalQuantity);
    }

    /**
     * Redis에서 쿠폰 잔여 재고를 조회한다.
     */
    public int getStock(Long couponId) {
        String stockKey = COUPON_STOCK_KEY.formatted(couponId);
        String value = redisTemplate.opsForValue().get(stockKey);
        return value != null ? Integer.parseInt(value) : -1;
    }
}
