package com.couponrush.repository;

import com.couponrush.entity.CouponIssue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CouponIssueRepository extends JpaRepository<CouponIssue, Long> {

    boolean existsByCouponIdAndUserId(Long couponId, Long userId);

    Optional<CouponIssue> findByCouponIdAndUserId(Long couponId, Long userId);

    List<CouponIssue> findAllByUserId(Long userId);
}
