UPDATE article SET liked = false WHERE liked IS NULL;
ALTER TABLE article ALTER COLUMN liked SET DEFAULT false;
ALTER TABLE article ALTER COLUMN liked SET NOT NULL;