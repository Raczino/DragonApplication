package com.raczkowski.app.admin.moderation.article;

import com.raczkowski.app.user.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleToConfirmRepository extends JpaRepository<ArticleToConfirm, Long> {
    ArticleToConfirm getArticleToConfirmById(Long id);

    void deleteArticleToConfirmById(Long id);

    Page<ArticleToConfirm> findByAppUser(AppUser appUser, Pageable pageable);
}
