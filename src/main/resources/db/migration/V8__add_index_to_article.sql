CREATE INDEX IF NOT EXISTS idx_article__title
    ON article (title);

CREATE INDEX IF NOT EXISTS idx_article__app_user_id
    ON article (app_user_id);

CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX IF NOT EXISTS idx_article__title_trgm
    ON article USING GIN (title gin_trgm_ops);