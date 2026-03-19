package com.couponrush.service;

import com.couponrush.common.exception.BusinessException;
import com.couponrush.common.exception.ErrorCode;
import com.couponrush.dto.request.CouponIssueRequest;
import com.couponrush.dto.response.CouponIssueResponse;
import com.couponrush.entity.CouponIssue;
import com.couponrush.repository.CouponIssueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponIssueService {

    private final CouponIssueRepository couponIssueRepository;
    private final CouponIssueRedisService couponIssueRedisService;
    private final CouponIssueWriter couponIssueWriter;

    /**
     * 쿠폰 발급
     * 1. 분산락 획득
     * 2. Redis 재고 선차감 + 중복 발급 체크
     * 3. DB 저장 (실패 시 Redis 롤백)
     */
    public CouponIssueResponse issueCoupon(Long couponId, CouponIssueRequest request) {
        return couponIssueRedisService.executeWithLock(couponId, () -> {
            couponIssueRedisService.decreaseStock(couponId, request.userId());

            try {
                return couponIssueWriter.issue(couponId, request.userId());
            } catch (Exception e) {
                couponIssueRedisService.rollbackStock(couponId, request.userId());
                throw e;
            }
        });
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
