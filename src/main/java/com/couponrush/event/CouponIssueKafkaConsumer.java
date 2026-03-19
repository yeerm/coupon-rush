package com.couponrush.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CouponIssueKafkaConsumer {

    /**
     * 쿠폰 발급 완료 이벤트를 수신하여 알림 처리
     */
    @KafkaListener(topics = "coupon-issue", groupId = "coupon-rush")
    public void consume(CouponIssuedEvent event) {
        log.info("쿠폰 발급 알림 발송: couponId={}, userId={}", event.couponId(), event.userId());
    }
}
