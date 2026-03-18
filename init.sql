CREATE TABLE IF NOT EXISTS users
(
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    email      VARCHAR(100) NOT NULL,
    name       VARCHAR(50)  NOT NULL,
    created_at DATETIME     NOT NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_users_email (email)
);

CREATE TABLE IF NOT EXISTS coupons
(
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    name             VARCHAR(100) NOT NULL,
    total_quantity   INT          NOT NULL,
    issued_quantity  INT          NOT NULL DEFAULT 0,
    discount_amount  INT          NOT NULL,
    issue_started_at DATETIME     NOT NULL,
    issue_expired_at DATETIME     NOT NULL,
    created_at       DATETIME     NOT NULL,
    updated_at       DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS coupon_issues
(
    id        BIGINT   NOT NULL AUTO_INCREMENT,
    coupon_id BIGINT   NOT NULL,
    user_id   BIGINT   NOT NULL,
    issued_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_coupon_user (coupon_id, user_id),
    CONSTRAINT fk_coupon_issues_coupon FOREIGN KEY (coupon_id) REFERENCES coupons (id),
    CONSTRAINT fk_coupon_issues_user FOREIGN KEY (user_id) REFERENCES users (id)
);
