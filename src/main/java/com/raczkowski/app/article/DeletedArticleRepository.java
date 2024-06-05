package com.raczkowski.app.article;

import com.raczkowski.app.enums.ArticleStatus;
import com.raczkowski.app.user.AppUser;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public interface DeletedArticleRepository extends JpaRepository<DeletedArticle, Long> {

    Page<DeletedArticle> getDeletedArticleByDeletedByAndStatus(AppUser appUser, ArticleStatus status, Pageable pageable);

    Page<DeletedArticle> getDeletedArticleByStatus(ArticleStatus status, Pageable pageable);

    @NonNull Page<DeletedArticle> findAll(@NonNull Pageable pageable);
}
