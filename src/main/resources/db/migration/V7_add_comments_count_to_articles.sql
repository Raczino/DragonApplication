ALTER TABLE article ADD COLUMN comments_count INT DEFAULT 0;

ALTER TABLE article ADD COLUMN likes_count INT DEFAULT 0;

ALTER TABLE comment ADD COLUMN likes_count INT DEFAULT 0;

ALTER TABLE deleted_article ADD COLUMN comments_count INT DEFAULT 0;

ALTER TABLE moderators_statistics ADD COLUMN pinned_article_counter INT DEFAULT 0;