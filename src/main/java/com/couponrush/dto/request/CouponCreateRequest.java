package com.couponrush.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CouponCreateRequest(

        @NotBlank(message = "쿠폰 이름은 필수입니다.")
        @Size(max = 100, message = "쿠폰 이름은 100자 이하로 입력해주세요.")
        String name,

        @Min(value = 1, message = "총 수량은 1 이상이어야 합니다.")
        int totalQuantity,

        @Min(value = 0, message = "할인 금액은 0 이상이어야 합니다.")
        int discountAmount,

        @NotNull(message = "발급 시작일은 필수입니다.")
        LocalDateTime issueStartedAt,

        @NotNull(message = "발급 종료일은 필수입니다.")
        LocalDateTime issueExpiredAt
) {
}
