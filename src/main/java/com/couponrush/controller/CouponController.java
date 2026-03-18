package com.couponrush.controller;

import com.couponrush.common.response.ApiResponse;
import com.couponrush.dto.request.CouponCreateRequest;
import com.couponrush.dto.response.CouponResponse;
import com.couponrush.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CouponResponse> createCoupon(@RequestBody @Valid CouponCreateRequest request) {
        return ApiResponse.ok(couponService.createCoupon(request));
    }

    @GetMapping("/{couponId}")
    public ApiResponse<CouponResponse> getCoupon(@PathVariable Long couponId) {
        return ApiResponse.ok(couponService.getCoupon(couponId));
    }
}
