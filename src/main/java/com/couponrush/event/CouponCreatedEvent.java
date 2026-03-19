package com.couponrush.event;

public record CouponCreatedEvent(
        Long couponId,
        int totalQuantity
) {
}
