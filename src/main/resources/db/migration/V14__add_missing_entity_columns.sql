ALTER TABLE article
    ADD COLUMN IF NOT EXISTS likes_count INT DEFAULT 0;

ALTER TABLE article
    ADD COLUMN IF NOT EXISTS comments_count INT DEFAULT 0;

ALTER TABLE comment
    ADD COLUMN IF NOT EXISTS likes_count INT DEFAULT 0;

ALTER TABLE deleted_article
    ADD COLUMN IF NOT EXISTS comments_count INT DEFAULT 0;

ALTER TABLE premium_feature
    ADD COLUMN IF NOT EXISTS value INT DEFAULT NULL;

ALTER TABLE moderators_statistics
    ADD COLUMN IF NOT EXISTS pinned_article_counter INT DEFAULT 0;
