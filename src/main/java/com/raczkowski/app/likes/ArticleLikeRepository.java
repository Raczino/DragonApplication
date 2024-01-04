package com.raczkowski.app.likes;

import com.raczkowski.app.User.AppUser;
import com.raczkowski.app.article.Article;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArticleLikeRepository extends JpaRepository<ArticleLike, Long> {

    boolean existsArticleByAppUser(AppUser user, Article article);
}
//TODO: naprawic sprawdzanie polubionych komentarzy u artykułów w bazie przez usera