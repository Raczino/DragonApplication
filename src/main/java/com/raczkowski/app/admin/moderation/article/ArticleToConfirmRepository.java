package com.raczkowski.app.admin.moderation.article;

import com.raczkowski.app.user.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ArticleToConfirmRepository extends JpaRepository<ArticleToConfirm, Long> {

    ArticleToConfirm getArticleToConfirmById(Long id);

    void deleteArticleToConfirmById(Long id);
}
