package com.couponrush.event;

public record CouponIssuedEvent(
        Long couponIssueId,
        Long couponId,
        Long userId
) {
}
