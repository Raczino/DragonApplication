package com.raczkowski.app.admin;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleToConfirmRepository extends JpaRepository<ArticleToConfirm, Long> {

    ArticleToConfirm getArticleToConfirmById(Long id);

    void deleteArticleToConfirmById(Long id);
}
