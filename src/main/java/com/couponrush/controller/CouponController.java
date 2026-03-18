package com.couponrush.controller;

import com.couponrush.common.response.ApiResponse;
import com.couponrush.dto.request.CouponCreateRequest;
import com.couponrush.dto.response.CouponResponse;
import com.couponrush.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Coupon", description = "쿠폰 API")
@RestController
@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @Operation(summary = "쿠폰 생성")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CouponResponse> createCoupon(@RequestBody @Valid CouponCreateRequest request) {
        return ApiResponse.ok(couponService.createCoupon(request));
    }

    @Operation(summary = "쿠폰 조회")
    @GetMapping("/{couponId}")
    public ApiResponse<CouponResponse> getCoupon(@PathVariable Long couponId) {
        return ApiResponse.ok(couponService.getCoupon(couponId));
    }
}
