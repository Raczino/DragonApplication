UPDATE comment
SET is_liked = false
WHERE is_liked IS NULL;