package com.couponrush.event;

import com.couponrush.service.CouponIssueRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponIssueEventListener {

    private static final String TOPIC = "coupon-issue";

    private final KafkaTemplate<String, CouponIssuedEvent> kafkaTemplate;
    private final CouponIssueRedisService couponIssueRedisService;

    /**
     * 쿠폰 생성 트랜잭션 커밋 후 Redis 재고 초기화
     * - DB 커밋 완료 후 실행되므로 불일치 없음
     * - Redis 실패해도 쿠폰 생성에는 영향 없음
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCouponCreated(CouponCreatedEvent event) {
        try {
            couponIssueRedisService.initStock(event.couponId(), event.totalQuantity());
        } catch (Exception e) {
            log.warn("Redis 재고 초기화 실패: couponId={}, totalQuantity={}, error={}",
                    event.couponId(), event.totalQuantity(), e.getMessage());
        }
    }

    /**
     * 쿠폰 발급 완료 이벤트를 Kafka로 발행
     * - AFTER_COMMIT: DB 커밋 후에만 발행 → 롤백 시 Kafka 메시지 발행 안됨
     * - kafkaTemplate.send()는 내부적으로 비동기 처리되므로 @Async 불필요
     * - whenComplete 콜백으로 발행 성공/실패 확인
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCouponIssued(CouponIssuedEvent event) {
        log.info("쿠폰 발급 이벤트 발행 시도: couponIssueId={}, userId={}", event.couponIssueId(), event.userId());
        kafkaTemplate.send(TOPIC, String.valueOf(event.userId()), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.warn("Kafka 발행 실패: couponIssueId={}, userId={}, error={}",
                                event.couponIssueId(), event.userId(), ex.getMessage());
                    } else {
                        log.info("Kafka 발행 성공: couponIssueId={}, topic={}, partition={}, offset={}",
                                event.couponIssueId(),
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
