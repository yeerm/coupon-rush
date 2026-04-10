package com.couponrush.service;

import com.couponrush.common.exception.BusinessException;
import com.couponrush.common.exception.ErrorCode;
import com.couponrush.dto.request.CouponCreateRequest;
import com.couponrush.dto.response.CouponResponse;
import com.couponrush.entity.Coupon;
import com.couponrush.event.CouponCreatedEvent;
import com.couponrush.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponCacheService couponCacheService;
    private final ApplicationEventPublisher eventPublisher;

    // 쿠폰 생성
    @Transactional
    public CouponResponse createCoupon(CouponCreateRequest request) {
        Coupon coupon = Coupon.create(request.name(), request.totalQuantity(),
                request.discountAmount(), request.issueStartedAt(), request.issueExpiredAt());
        CouponResponse response = CouponResponse.from(couponRepository.save(coupon));

        // 트랜잭션 커밋 후 Redis 재고 초기화 이벤트 발행
        eventPublisher.publishEvent(new CouponCreatedEvent(response.id(), request.totalQuantity()));

        return response;
    }

    // 쿠폰 조회 (Cache Aside)
    @Transactional(readOnly = true)
    public CouponResponse getCoupon(Long couponId) {
        // 1. Redis 캐시 조회
        return couponCacheService.getCache(couponId)
                .orElseGet(() -> {
                    // 2. 캐시 미스 → DB 조회
                    Coupon coupon = couponRepository.findById(couponId)
                            .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND));
                    CouponResponse response = CouponResponse.from(coupon);

                    // 3. Redis에 저장
                    couponCacheService.setCache(couponId, response);

                    return response;
                });
    }

    // 쿠폰 캐시 무효화 (쿠폰 정보 수정 시 호출)
    public void evictCouponCache(Long couponId) {
        couponCacheService.evictCache(couponId);
    }

    // ── IntelliJ Profiler 병목 탐지 테스트용 ──────────────────────────────────
    // Profiler 실행 후 /api/v1/coupons/test 를 호출하면
    // 플레임 그래프에서 어느 메서드가 CPU 시간을 얼마나 점유하는지 확인 가능
    public String diagnoseBottleneck() {
        validateInput();       // 빠름 (~0ms)
        fetchFromDatabase();   // 느림 — I/O 시뮬레이션 (wall-clock 모드에서 두드러짐)
        processData();         // 느림 — CPU 연산 집중 (CPU 모드에서 두드러짐)
        buildResponse();       // 빠름 (~0ms)
        return "bottleneck test complete";
    }

    // 빠른 입력 검증 시뮬레이션
    private void validateInput() {
        long sum = 0;
        for (int i = 0; i < 1_000; i++) {
            sum += i;
        }
    }

    // 느린 DB 쿼리 시뮬레이션 — Thread.sleep은 Wall-clock 프로파일링에서 포착됨
    // (CPU 프로파일링에서는 스레드가 대기 중이므로 보이지 않음)
    private void fetchFromDatabase() {
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // CPU 집약적 연산 시뮬레이션 — CPU 프로파일링에서 두드러지게 나타남
    private void processData() {
        double result = 0;
        for (int i = 0; i < 10_000_000; i++) {
            result += Math.sqrt(i);
        }
    }

    // 빠른 응답 생성 시뮬레이션
    private void buildResponse() {
        String.valueOf(System.currentTimeMillis());
    }
    // ──────────────────────────────────────────────────────────────────────────
}
