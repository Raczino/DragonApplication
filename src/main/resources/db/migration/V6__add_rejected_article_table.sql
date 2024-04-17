CREATE TABLE rejected_article
(
    id           BIGINT PRIMARY KEY,
    title        VARCHAR(255) NOT NULL,
    content      TEXT         NOT NULL,
    posted_date  TIMESTAMP    NOT NULL,
    app_user_id  BIGINT       NOT NULL,
    FOREIGN KEY (app_user_id) REFERENCES app_user (id),
    status VARCHAR(20) DEFAULT 'REJECTED'
);
