package com.couponrush.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CouponCreateRequest(

        @NotBlank(message = "쿠폰 이름은 필수입니다.")
        @Size(max = 100, message = "쿠폰 이름은 100자 이하로 입력해주세요.")
        String name,

        @Min(value = 1, message = "총 수량은 1 이상이어야 합니다.")
        int totalQuantity
) {
}
