package com.raczkowski.app.article;

import com.raczkowski.app.enums.ArticleStatus;
import com.raczkowski.app.exceptions.ErrorMessages;
import com.raczkowski.app.exceptions.ResponseException;
import com.raczkowski.app.user.AppUser;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Service
@AllArgsConstructor
public class DeletedArticleService {
    private final DeletedArticleRepository deletedArticleRepository;
    private final ArticleRepository articleRepository;

    @Transactional
    public void deleteArticle(Long articleId, ArticleStatus status, AppUser user) {
        Article article = articleRepository.findArticleById(articleId);
        if (article != null) {
            articleRepository.deleteArticleById(articleId);
            DeletedArticle deletedArticle = new DeletedArticle(
                    article.getTitle(),
                    article.getContent(),
                    article.getContentHtml(),
                    article.getPostedDate(),
                    article.getAppUser(),
                    status,
                    article.getLikesCount(),
                    article.getUpdatedAt(),
                    article.isUpdated(),
                    article.getAcceptedAt(),
                    article.getAcceptedBy(),
                    ZonedDateTime.now(ZoneOffset.UTC),
                    user,
                    article.getCommentsCount()
            );
            deletedArticleRepository.save(deletedArticle);
        } else {
            throw new ResponseException(ErrorMessages.ARTICLE_ID_NOT_EXISTS);
        }
    }
}
