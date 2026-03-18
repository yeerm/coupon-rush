package com.couponrush.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 쿠폰 발급 관련
    ALREADY_ISSUED(HttpStatus.BAD_REQUEST, "ALREADY_ISSUED", "이미 발급받은 쿠폰입니다."),
    COUPON_EXHAUSTED(HttpStatus.CONFLICT, "COUPON_EXHAUSTED", "쿠폰이 모두 소진되었습니다."),
    LOCK_FAILED(HttpStatus.LOCKED, "LOCK_FAILED", "잠시 후 다시 시도해주세요."),

    // 리소스 관련
    COUPON_NOT_FOUND(HttpStatus.NOT_FOUND, "COUPON_NOT_FOUND", "존재하지 않는 쿠폰입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "존재하지 않는 유저입니다."),

    // 공통
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "INVALID_INPUT", "잘못된 입력값입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
