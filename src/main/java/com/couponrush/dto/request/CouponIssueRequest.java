package com.couponrush.dto.request;

import jakarta.validation.constraints.NotNull;

public record CouponIssueRequest(

        @NotNull(message = "유저 ID는 필수입니다.")
        Long userId
) {
}
