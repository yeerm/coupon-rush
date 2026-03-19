package com.couponrush.service;

import com.couponrush.common.exception.BusinessException;
import com.couponrush.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponIssueRedisService {

    private static final String COUPON_STOCK_KEY = "coupon:stock:%d";
    private static final String COUPON_ISSUED_KEY = "coupon:issued:%d";
    private static final String COUPON_LOCK_KEY = "coupon:lock:%d";
    private static final long LOCK_WAIT_TIME = 3L;
    private static final long LOCK_LEASE_TIME = 5L;

    private final StringRedisTemplate redisTemplate;
    private final RedissonClient redissonClient;


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
        // TODO: [배치 동기화 미구현] Redis 장애로 add()가 실패하면 재고는 차감됐지만 발급 이력 Set에 userId가 없는
        //  데이터 불일치 상태가 된다. 이 경우 동일 유저 재시도 시 isMember() 체크를 통과하지만,
        //  DB의 UNIQUE 제약(uq_coupon_user)이 중복 발급을 최종 차단하고 rollbackStock()으로 재고를 복구한다.
        //  단, Redis 복구 후에도 Set이 비어있는 상태가 지속될 수 있으므로
        //  DB coupon_issues 테이블 기준으로 Redis Set(coupon:issued:{couponId})을 재동기화하는
        //  배치 작업이 필요하다. (Spring Scheduler 또는 Redis 복구 트리거 연동)
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

    /**
     * Redis 재고 선차감을 롤백한다. (DB 저장 실패 시 호출)
     */
    public void rollbackStock(Long couponId, Long userId) {
        // 재고 1 증가 (재고 되돌리기)
        redisTemplate.opsForValue().increment(COUPON_STOCK_KEY.formatted(couponId));
        // 발급된 유저 목록에서 제거
        redisTemplate.opsForSet().remove(COUPON_ISSUED_KEY.formatted(couponId), String.valueOf(userId));
        log.warn("Redis 재고 롤백: couponId={}, userId={}", couponId, userId);
    }

    /**
     * Redisson 분산락을 획득하고 action을 실행한 뒤 결과를 반환한다.
     * 락 획득 실패 시 LOCK_FAILED 예외를 던진다.
     */
    public <T> T executeWithLock(Long couponId, Supplier<T> action) {
        String lockKey = COUPON_LOCK_KEY.formatted(couponId);
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean acquired = lock.tryLock(LOCK_WAIT_TIME, LOCK_LEASE_TIME, TimeUnit.SECONDS);
            if (!acquired) {
                throw new BusinessException(ErrorCode.LOCK_FAILED);
            }
            return action.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.LOCK_FAILED);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
