-- V__fix_hashtag_join_tables_inplace.sql
BEGIN;

------------------------------------------------------------
-- ARTICLE_HASHTAG – deduplikacja i poprawne klucze
------------------------------------------------------------
-- usuń duplikaty (zostaw 1 rekord per para)
DELETE FROM article_hashtag a
    USING article_hashtag b
WHERE a.ctid < b.ctid
  AND a.article_id = b.article_id
  AND a.hashtag_id = b.hashtag_id;

-- zdejmij potencjalne stare constrainty/indeksy, żeby uniknąć kolizji nazw
ALTER TABLE article_hashtag DROP CONSTRAINT IF EXISTS pk_article_hashtag;
DROP INDEX IF EXISTS pk_article_hashtag;                -- czasem pk było indexem o takiej nazwie
ALTER TABLE article_hashtag DROP CONSTRAINT IF EXISTS article_hashtag_pkey;
-- znane stare FK z Twoich dumpów (bezpiecznie "IF EXISTS")
ALTER TABLE article_hashtag DROP CONSTRAINT IF EXISTS fk4adimje6fx9kut67u6jd9c174;
ALTER TABLE article_hashtag DROP CONSTRAINT IF EXISTS fke3ccvxb3rjbjyugsttrkpabma;
ALTER TABLE article_hashtag DROP CONSTRAINT IF EXISTS fk_article_hashtag__article;
ALTER TABLE article_hashtag DROP CONSTRAINT IF EXISTS fk_article_hashtag__hashtag;
-- jeżeli kiedyś powstał zły UNIQUE tylko na hashtag_id (ten od Twojego błędu)
ALTER TABLE article_hashtag DROP CONSTRAINT IF EXISTS uk_ixu2e99mwe1youw66to4xtcyd;

-- dodaj PRIMARY KEY na PARZE (bez nazwy – DB sama nada, brak 42P07)
ALTER TABLE article_hashtag
    ADD PRIMARY KEY (article_id, hashtag_id);

-- dodaj FKs z ON DELETE CASCADE (też bez nazw)
ALTER TABLE article_hashtag
    ADD FOREIGN KEY (article_id) REFERENCES article(id) ON DELETE CASCADE,
    ADD FOREIGN KEY (hashtag_id) REFERENCES hashtag(id) ON DELETE CASCADE;

-- indeks pod wyszukiwanie po tagu
CREATE INDEX IF NOT EXISTS idx_article_hashtag__hashtag_id
    ON article_hashtag(hashtag_id);


------------------------------------------------------------
-- ARTICLE_TO_CONFIRM_HASHTAG – deduplikacja i poprawne klucze
------------------------------------------------------------
DELETE FROM article_to_confirm_hashtag a
    USING article_to_confirm_hashtag b
WHERE a.ctid < b.ctid
  AND a.article_to_confirm_id = b.article_to_confirm_id
  AND a.hashtag_id = b.hashtag_id;

ALTER TABLE article_to_confirm_hashtag DROP CONSTRAINT IF EXISTS pk_atc_hashtag;
DROP INDEX IF EXISTS pk_article_to_confirm_hashtag;
ALTER TABLE article_to_confirm_hashtag DROP CONSTRAINT IF EXISTS article_to_confirm_hashtag_pkey;

ALTER TABLE article_to_confirm_hashtag DROP CONSTRAINT IF EXISTS fk_atc_hashtag__atc;
ALTER TABLE article_to_confirm_hashtag DROP CONSTRAINT IF EXISTS fk_atc_hashtag__hashtag;
-- jeśli kiedyś powstał zły UNIQUE, też go zrzuć (podmień nazwę jeśli masz inną)
ALTER TABLE article_to_confirm_hashtag DROP CONSTRAINT IF EXISTS uk_atc_hashtag_bad;

ALTER TABLE article_to_confirm_hashtag
    ADD PRIMARY KEY (article_to_confirm_id, hashtag_id);

ALTER TABLE article_to_confirm_hashtag
    ADD FOREIGN KEY (article_to_confirm_id) REFERENCES article_to_confirm(id) ON DELETE CASCADE,
    ADD FOREIGN KEY (hashtag_id) REFERENCES hashtag(id) ON DELETE CASCADE;

CREATE INDEX IF NOT EXISTS idx_atc_hashtag__hashtag_id
    ON article_to_confirm_hashtag(hashtag_id);

COMMIT;
