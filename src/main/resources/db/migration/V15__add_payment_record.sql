CREATE TABLE payment_record (
    id                BIGSERIAL PRIMARY KEY,
    user_id           BIGINT NOT NULL,
    stripe_session_id VARCHAR(255) NOT NULL UNIQUE,
    plan_type         VARCHAR(20)  NOT NULL,
    currency          VARCHAR(3)   NOT NULL,
    amount            NUMERIC(10, 2) NOT NULL,
    status            VARCHAR(20)  NOT NULL,
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_payment_record_user_id ON payment_record (user_id);