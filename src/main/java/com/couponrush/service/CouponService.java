package com.couponrush.service;

import com.couponrush.common.exception.BusinessException;
import com.couponrush.common.exception.ErrorCode;
import com.couponrush.dto.request.CouponCreateRequest;
import com.couponrush.dto.response.CouponResponse;
import com.couponrush.entity.Coupon;
import com.couponrush.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

    // 쿠폰 생성
    @Transactional
    public CouponResponse createCoupon(CouponCreateRequest request) {
        Coupon coupon = Coupon.create(request.name(), request.totalQuantity(),
                request.discountAmount(), request.issueStartedAt(), request.issueExpiredAt());
        return CouponResponse.from(couponRepository.save(coupon));
    }

    // 쿠폰 조회
    @Transactional(readOnly = true)
    public CouponResponse getCoupon(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND));
        return CouponResponse.from(coupon);
    }
}
