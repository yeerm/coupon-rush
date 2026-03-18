package com.couponrush.controller;

import com.couponrush.common.response.ApiResponse;
import com.couponrush.dto.request.CouponIssueRequest;
import com.couponrush.dto.response.CouponIssueResponse;
import com.couponrush.service.CouponIssueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/coupons/{couponId}/issues")
@RequiredArgsConstructor
public class CouponIssueController {

    private final CouponIssueService couponIssueService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CouponIssueResponse> issueCoupon(
            @PathVariable Long couponId,
            @RequestBody @Valid CouponIssueRequest request
    ) {
        return ApiResponse.ok(couponIssueService.issueCoupon(couponId, request));
    }

    @GetMapping("/me")
    public ApiResponse<CouponIssueResponse> getMyIssue(
            @PathVariable Long couponId,
            @RequestParam Long userId
    ) {
        return ApiResponse.ok(couponIssueService.getMyIssue(couponId, userId));
    }
}
