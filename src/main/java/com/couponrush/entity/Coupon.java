package com.couponrush.entity;

import com.couponrush.common.exception.BusinessException;
import com.couponrush.common.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "coupons")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "total_quantity", nullable = false)
    private int totalQuantity;

    @Column(name = "issued_quantity", nullable = false)
    private int issuedQuantity;

    @Column(name = "discount_amount", nullable = false)
    private int discountAmount;

    @Column(name = "issue_started_at", nullable = false)
    private LocalDateTime issueStartedAt;

    @Column(name = "issue_expired_at", nullable = false)
    private LocalDateTime issueExpiredAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public static Coupon create(String name, int totalQuantity, int discountAmount,
                                LocalDateTime issueStartedAt, LocalDateTime issueExpiredAt) {
        Coupon coupon = new Coupon();
        coupon.name = name;
        coupon.totalQuantity = totalQuantity;
        coupon.issuedQuantity = 0;
        coupon.discountAmount = discountAmount;
        coupon.issueStartedAt = issueStartedAt;
        coupon.issueExpiredAt = issueExpiredAt;
        return coupon;
    }

    public boolean isExhausted() {
        return this.issuedQuantity >= this.totalQuantity;
    }

    public int getRemainingQuantity() {
        return this.totalQuantity - this.issuedQuantity;
    }

    /**
     * DB 레벨 발급 수량 증가 (Redis 장애 시 최후 방어선)
     */
    public void increaseIssuedQuantity() {
        if (isExhausted()) {
            throw new BusinessException(ErrorCode.COUPON_EXHAUSTED);
        }
        this.issuedQuantity++;
    }
}
