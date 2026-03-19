package com.couponrush.event;

import java.time.LocalDateTime;

public record CouponIssuedEvent(
        Long couponIssueId,
        Long couponId,
        Long userId,
        LocalDateTime issuedAt
) {
}
