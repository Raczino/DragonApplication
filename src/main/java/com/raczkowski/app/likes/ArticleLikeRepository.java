package com.raczkowski.app.likes;

import com.raczkowski.app.article.Article;
import com.raczkowski.app.user.AppUser;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

public interface ArticleLikeRepository extends JpaRepository<ArticleLike, Long> {

    boolean existsArticleLikesByAppUserAndArticle(AppUser user, Article article);

    ArticleLike findByArticleAndAppUser(Article article, AppUser user);

    List<ArticleLike> findAllByArticle(Article article);

    @Query("SELECT al.article.id FROM ArticleLike al WHERE al.appUser = :user AND al.article.id IN :articleIds")
    Set<Long> findLikedArticleIdsByUserAndArticleIds(@Param("user") AppUser user, @Param("articleIds") List<Long> articleIds);
}