package com.couponrush.service;

import com.couponrush.common.exception.BusinessException;
import com.couponrush.common.exception.ErrorCode;
import com.couponrush.dto.request.CouponCreateRequest;
import com.couponrush.dto.response.CouponResponse;
import com.couponrush.entity.Coupon;
import com.couponrush.event.CouponCreatedEvent;
import com.couponrush.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponCacheService couponCacheService;
    private final ApplicationEventPublisher eventPublisher;

    // 쿠폰 생성
    @Transactional
    public CouponResponse createCoupon(CouponCreateRequest request) {
        Coupon coupon = Coupon.create(request.name(), request.totalQuantity(),
                request.discountAmount(), request.issueStartedAt(), request.issueExpiredAt());
        CouponResponse response = CouponResponse.from(couponRepository.save(coupon));

        // 트랜잭션 커밋 후 Redis 재고 초기화 이벤트 발행
        eventPublisher.publishEvent(new CouponCreatedEvent(response.id(), request.totalQuantity()));

        return response;
    }

    // 쿠폰 조회 (Cache Aside)
    @Transactional(readOnly = true)
    public CouponResponse getCoupon(Long couponId) {
        // 1. Redis 캐시 조회
        return couponCacheService.getCache(couponId)
                .orElseGet(() -> {
                    // 2. 캐시 미스 → DB 조회
                    Coupon coupon = couponRepository.findById(couponId)
                            .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND));
                    CouponResponse response = CouponResponse.from(coupon);

                    // 3. Redis에 저장
                    couponCacheService.setCache(couponId, response);

                    return response;
                });
    }

    // 쿠폰 캐시 무효화 (쿠폰 정보 수정 시 호출)
    public void evictCouponCache(Long couponId) {
        couponCacheService.evictCache(couponId);
    }
}
