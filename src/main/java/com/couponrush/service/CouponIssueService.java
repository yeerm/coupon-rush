package com.couponrush.service;

import com.couponrush.common.exception.BusinessException;
import com.couponrush.common.exception.ErrorCode;
import com.couponrush.dto.request.CouponIssueRequest;
import com.couponrush.dto.response.CouponIssueResponse;
import com.couponrush.entity.Coupon;
import com.couponrush.entity.CouponIssue;
import com.couponrush.entity.User;
import com.couponrush.repository.CouponIssueRepository;
import com.couponrush.repository.CouponRepository;
import com.couponrush.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
public class CouponIssueService {

    private final CouponIssueRepository couponIssueRepository;
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;
    private final CouponIssueRedisService couponIssueRedisService;

    /**
     * 쿠폰 발급 (Redisson 분산락 적용)
     * 1. 분산락 획득
     * 2. 중복 발급 체크
     * 3. 재고 소진 체크
     * 4. DB 저장
     */
    public CouponIssueResponse issueCoupon(Long couponId, CouponIssueRequest request) {
        AtomicReference<CouponIssueResponse> result = new AtomicReference<>();

        couponIssueRedisService.executeWithLock(couponId, () -> {
            result.set(doIssueCoupon(couponId, request));
        });

        return result.get();
    }

    @Transactional
    protected CouponIssueResponse doIssueCoupon(Long couponId, CouponIssueRequest request) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND));
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 중복 발급 체크
        if (couponIssueRepository.existsByCouponIdAndUserId(couponId, request.userId())) {
            throw new BusinessException(ErrorCode.ALREADY_ISSUED);
        }

        // 재고 소진 체크
        if (coupon.isExhausted()) {
            throw new BusinessException(ErrorCode.COUPON_EXHAUSTED);
        }

        coupon.increaseIssuedQuantity();
        CouponIssue couponIssue = couponIssueRepository.save(CouponIssue.create(coupon, user));

        return CouponIssueResponse.from(couponIssue);
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
