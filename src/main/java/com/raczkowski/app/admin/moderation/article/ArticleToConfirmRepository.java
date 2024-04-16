package com.raczkowski.app.admin.moderation.article;

import com.raczkowski.app.admin.moderation.article.ArticleToConfirm;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleToConfirmRepository extends JpaRepository<ArticleToConfirm, Long> {

    ArticleToConfirm getArticleToConfirmById(Long id);

    void deleteArticleToConfirmById(Long id);
}
