package com.raczkowski.app.likes;

import com.raczkowski.app.user.AppUser;
import com.raczkowski.app.article.Article;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArticleLikeRepository extends JpaRepository<ArticleLike, Long> {

    boolean existsArticleLikesByAppUserAndArticle(AppUser user, Article article);

    ArticleLike findByArticleAndAppUser(Article article, AppUser user);

    List<ArticleLike> findAllByArticle(Article article);

    void deleteArticleLikesByArticle(Article article);
}