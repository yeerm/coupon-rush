package com.couponrush.service;

import com.couponrush.common.exception.BusinessException;
import com.couponrush.common.exception.ErrorCode;
import com.couponrush.dto.response.CouponIssueResponse;
import com.couponrush.entity.Coupon;
import com.couponrush.entity.CouponIssue;
import com.couponrush.entity.User;
import com.couponrush.event.CouponIssuedEvent;
import com.couponrush.repository.CouponIssueRepository;
import com.couponrush.repository.CouponRepository;
import com.couponrush.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CouponIssueWriter {

    private final CouponIssueRepository couponIssueRepository;
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 쿠폰 발급 DB 저장 (Redis 선차감 이후 호출)
     * - 재고/중복 체크는 Redis에서 처리 완료
     * - issued_quantity 증가는 Redis 장애 시 DB 최후 방어선
     */
    @Transactional
    public CouponIssueResponse issue(Long couponId, Long userId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        coupon.increaseIssuedQuantity();
        CouponIssue couponIssue = couponIssueRepository.save(CouponIssue.create(coupon, user));

        // 발급 완료 이벤트 발행 (트랜잭션 커밋 후 Kafka로 전송)
        eventPublisher.publishEvent(new CouponIssuedEvent(
                couponIssue.getId(),
                couponIssue.getCoupon().getId(),
                couponIssue.getUser().getId(),
                couponIssue.getIssuedAt()
        ));

        return CouponIssueResponse.from(couponIssue);
    }
}
