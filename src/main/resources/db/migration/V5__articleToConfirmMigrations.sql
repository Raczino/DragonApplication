CREATE TABLE articles_to_confirm
(
    id           BIGINT PRIMARY KEY,
    title        VARCHAR(255) NOT NULL,
    content      TEXT         NOT NULL,
    posted_date  TIMESTAMP    NOT NULL,
    app_user_id  BIGINT       NOT NULL,
    likes_number INT     DEFAULT 0,
    updated_at   TIMESTAMP,
    is_updated   BOOLEAN DEFAULT false,
    FOREIGN KEY (app_user_id) REFERENCES app_user (id),
    status VARCHAR(20) DEFAULT 'PENDING'
);
