package com.couponrush.dto.response;

import com.couponrush.entity.Coupon;

import java.time.LocalDateTime;

public record CouponResponse(
        Long id,
        String name,
        int totalQuantity,
        int issuedQuantity,
        int remainingQuantity,
        LocalDateTime createdAt
) {
    public static CouponResponse from(Coupon coupon) {
        return new CouponResponse(
                coupon.getId(),
                coupon.getName(),
                coupon.getTotalQuantity(),
                coupon.getIssuedQuantity(),
                coupon.getRemainingQuantity(),
                coupon.getCreatedAt()
        );
    }
}
