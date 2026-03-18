# coupon-rush

선착순 쿠폰 발급 시스템 토이 프로젝트.
동시성 제어, 캐싱, 비동기 처리, 로그/모니터링을 핵심 주제로 다룬다.

---

## 기술 스택

| 영역 | 기술 |
|------|------|
| 언어/프레임워크 | Java 17, Spring Boot 3.3.x |
| DB | MySQL 8.0 |
| 캐시/분산락 | Redis 7.0 |
| 메시지 큐 | Kafka (bitnami/kafka) |
| 모니터링 | Prometheus + Grafana |
| 부하테스트 | k6 |
| 컨테이너 | Docker Compose |
| 빌드 | Gradle |

---

## 언어 설정

- 대화 응답은 한국어로 작성한다.
- 코드 주석은 한국어로 작성한다.
- 변수명, 메서드명, 클래스명, 파일명, 폴더명 등 코드 자체는 영어로 작성한다.
- 에러 메시지는 원문 영어 그대로 출력한다.

## 네이밍 컨벤션

- 패키지명: `com.couponrush`, 소문자
- 클래스명: PascalCase (예: `CouponIssueService`)
- 메서드명/변수명: camelCase (예: `issueCoupon`)
- 상수명: UPPER_SNAKE_CASE (예: `MAX_ISSUE_COUNT`)
- DB 컬럼명: snake_case (예: `issued_quantity`)
- 파일명: 클래스명과 동일하게 PascalCase (예: `CouponIssueService.java`)
- 폴더명: 소문자 (예: `controller`, `service`, `repository`)
- 설정 파일명: kebab-case (예: `application-local.yml`)
- 브랜치명: kebab-case (예: `feature/coupon-issue`)


## 프로젝트 구조

```
src/main/java/com/couponrush/
├── CouponRushApplication.java
│
├── controller/
│   ├── UserController.java
│   ├── CouponController.java
│   └── CouponIssueController.java
│
├── service/
│   ├── UserService.java
│   ├── CouponService.java
│   ├── CouponIssueService.java
│   └── CouponIssueRedisService.java
│
├── repository/
│   ├── UserRepository.java
│   ├── CouponRepository.java
│   └── CouponIssueRepository.java
│
├── entity/
│   ├── User.java
│   ├── Coupon.java
│   └── CouponIssue.java
│
├── dto/
│   ├── request/
│   │   ├── UserCreateRequest.java
│   │   ├── CouponCreateRequest.java
│   │   └── CouponIssueRequest.java
│   └── response/
│       ├── UserResponse.java
│       ├── CouponResponse.java
│       └── CouponIssueResponse.java
│
├── event/
│   ├── CouponIssuedEvent.java
│   └── CouponIssueEventListener.java
│
└── common/
    ├── exception/
    │   ├── GlobalExceptionHandler.java
    │   ├── BusinessException.java
    │   └── ErrorCode.java
    └── response/
        └── ApiResponse.java
```

---

## DB 설계

### 테이블 목록
- `users` : 유저
- `coupons` : 쿠폰 정책
- `coupon_issues` : 발급 이력

### 핵심 제약 조건
- `coupon_issues.uq_coupon_user` : (coupon_id, user_id) UNIQUE → 중복 발급 방지
- `coupons.issued_quantity` : Redis 장애 시 DB가 최후 방어선

---

## API 설계

Base URL: `/api/v1`

| 메서드 | 엔드포인트 | 설명 |
|------|------|------|
| POST | `/users` | 유저 생성 |
| POST | `/coupons` | 쿠폰 생성 |
| GET | `/coupons/{couponId}` | 쿠폰 조회 |
| POST | `/coupons/{couponId}/issues` | 쿠폰 발급 요청 |
| GET | `/coupons/{couponId}/issues/me` | 내 발급 이력 조회 |

### 에러 코드
| 코드 | HTTP Status | 설명 |
|------|------|------|
| `ALREADY_ISSUED` | 400 | 이미 발급받은 쿠폰 |
| `COUPON_EXHAUSTED` | 409 | 쿠폰 소진 |
| `LOCK_FAILED` | 423 | 분산락 획득 실패 |

---

## 핵심 구현 주제

### 1. 동시성 제어
- Redis 분산락 (Redisson) 사용
- 락 획득 실패 시 `423 Locked` 응답
- DB 레벨 UNIQUE 제약으로 이중 방어

### 2. 캐싱
- 쿠폰 재고 조회는 Redis 캐시 우선
- Cache Aside 전략 사용
- Redis 장애 시 DB fallback

### 3. 비동기 처리
- 발급 완료 후 알림 이벤트를 Kafka로 발행
- Spring ApplicationEvent → Kafka 순서로 구현
- Consumer에서 이메일 발송 처리

### 4. 로그/모니터링
- Spring Actuator + Prometheus + Grafana
- 주요 메트릭: 발급 요청 수, 실패율, 응답 시간
- 부하테스트: k6로 동시 요청 시나리오 검증



---

## 로컬 실행

```bash
# 인프라 실행
docker compose up -d

# 애플리케이션 실행
./gradlew bootRun
```

---

## 컨벤션

- 패키지명: `com.couponrush`
- DB 네이밍: snake_case
- 브랜치 전략: `main` / `feature/{기능명}`
- 커밋 메시지: `feat:`, `fix:`, `refactor:`, `docs:`, `test:`
