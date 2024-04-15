ALTER TABLE article
    ADD CONSTRAINT uc_article_app_user UNIQUE (app_user_id);

ALTER TABLE article_like
    ADD CONSTRAINT uc_articlelike_app_user UNIQUE (app_user_id);

ALTER TABLE article_like
    ADD CONSTRAINT uc_articlelike_article UNIQUE (article_id);

ALTER TABLE comment
    ADD CONSTRAINT uc_comment_app_user UNIQUE (app_user_id);

ALTER TABLE comment_like
    ADD CONSTRAINT uc_commentlike_app_user UNIQUE (app_user_id);

ALTER TABLE comment_like
    ADD CONSTRAINT uc_commentlike_comment UNIQUE (comment_id);

DROP TABLE test CASCADE;