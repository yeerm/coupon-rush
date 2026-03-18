package com.couponrush.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponIssueEventListener {

    private static final String TOPIC = "coupon-issued";

    private final KafkaTemplate<String, CouponIssuedEvent> kafkaTemplate;

    /**
     * 쿠폰 발급 완료 이벤트를 Kafka로 발행
     */
    @Async
    @EventListener
    public void onCouponIssued(CouponIssuedEvent event) {
        log.info("쿠폰 발급 이벤트 수신: couponIssueId={}, userId={}", event.couponIssueId(), event.userId());
        kafkaTemplate.send(TOPIC, String.valueOf(event.userId()), event);
    }
}
