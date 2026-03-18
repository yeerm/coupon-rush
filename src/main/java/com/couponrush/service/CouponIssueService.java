package com.couponrush.service;

import com.couponrush.common.exception.BusinessException;
import com.couponrush.common.exception.ErrorCode;
import com.couponrush.dto.request.CouponIssueRequest;
import com.couponrush.dto.response.CouponIssueResponse;
import com.couponrush.entity.Coupon;
import com.couponrush.entity.CouponIssue;
import com.couponrush.entity.User;
import com.couponrush.event.CouponIssuedEvent;
import com.couponrush.repository.CouponIssueRepository;
import com.couponrush.repository.CouponRepository;
import com.couponrush.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponIssueService {

    private static final String LOCK_KEY = "lock:coupon:%d";
    private static final long LOCK_WAIT_TIME = 3L;
    private static final long LOCK_LEASE_TIME = 5L;

    private final CouponIssueRepository couponIssueRepository;
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;
    private final CouponIssueRedisService couponIssueRedisService;
    private final RedissonClient redissonClient;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 쿠폰 발급 요청 처리
     * 1. Redis 분산락 획득
     * 2. Redis 재고 선차감 (Cache Aside)
     * 3. DB 저장
     * 4. 발급 완료 이벤트 발행 (Kafka)
     */
    @Transactional
    public CouponIssueResponse issueCoupon(Long couponId, CouponIssueRequest request) {
        String lockKey = LOCK_KEY.formatted(couponId);
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean acquired = lock.tryLock(LOCK_WAIT_TIME, LOCK_LEASE_TIME, TimeUnit.SECONDS);
            if (!acquired) {
                throw new BusinessException(ErrorCode.LOCK_FAILED);
            }

            // Redis 재고 선차감 및 중복 발급 체크
            couponIssueRedisService.decreaseStock(couponId, request.userId());

            // DB 저장
            Coupon coupon = couponRepository.findById(couponId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND));
            User user = userRepository.findById(request.userId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

            coupon.increaseIssuedQuantity();
            CouponIssue couponIssue = CouponIssue.create(coupon, user);
            couponIssueRepository.save(couponIssue);

            // 발급 완료 이벤트 발행
            eventPublisher.publishEvent(new CouponIssuedEvent(
                    couponIssue.getId(), couponId, request.userId()
            ));

            return CouponIssueResponse.from(couponIssue);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.LOCK_FAILED);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 내 쿠폰 발급 이력 조회
     */
    @Transactional(readOnly = true)
    public CouponIssueResponse getMyIssue(Long couponId, Long userId) {
        CouponIssue issue = couponIssueRepository.findByCouponIdAndUserId(couponId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND));
        return CouponIssueResponse.from(issue);
    }
}
