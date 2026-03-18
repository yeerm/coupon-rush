package com.couponrush.dto.response;

import com.couponrush.entity.CouponIssue;

import java.time.LocalDateTime;

public record CouponIssueResponse(
        Long id,
        Long couponId,
        String couponName,
        Long userId,
        LocalDateTime issuedAt
) {
    public static CouponIssueResponse from(CouponIssue issue) {
        return new CouponIssueResponse(
                issue.getId(),
                issue.getCoupon().getId(),
                issue.getCoupon().getName(),
                issue.getUser().getId(),
                issue.getIssuedAt()
        );
    }
}
