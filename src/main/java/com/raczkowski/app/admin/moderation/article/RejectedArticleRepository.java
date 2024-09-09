package com.raczkowski.app.admin.moderation.article;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RejectedArticleRepository extends JpaRepository<RejectedArticle, Long> {
}
