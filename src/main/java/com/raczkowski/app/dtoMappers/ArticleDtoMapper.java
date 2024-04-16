package com.raczkowski.app.dtoMappers;

import com.raczkowski.app.admin.moderation.article.ArticleToConfirm;
import com.raczkowski.app.admin.moderation.article.RejectedArticle;
import com.raczkowski.app.article.Article;
import com.raczkowski.app.dto.ArticleDto;
import com.raczkowski.app.dto.NonConfirmedArticleDto;
import com.raczkowski.app.dto.RejectedArticleDto;
import com.raczkowski.app.dto.authorDto;
import org.springframework.stereotype.Component;

@Component
public class ArticleDtoMapper {
    public static ArticleDto articleDtoMapper(Article article) {
        return new ArticleDto(
                article.getId(),
                article.getTitle(),
                article.getContent(),
                article.getPostedDate(),
                article.getLikesNumber(),
                new authorDto(
                        article.getAppUser().getId(),
                        article.getAppUser().getFirstName(),
                        article.getAppUser().getLastName(),
                        article.getAppUser().getEmail()
                ),
                article.getUpdatedAt(),
                article.isUpdated(),
                article.getStatus()
        );
    }

    public static RejectedArticleDto rejectedArticleDtoMapper(RejectedArticle article) {
        return new RejectedArticleDto(
                article.getId(),
                article.getTitle(),
                article.getContent(),
                article.getPostedDate(),
                new authorDto(
                        article.getAppUser().getId(),
                        article.getAppUser().getFirstName(),
                        article.getAppUser().getLastName(),
                        article.getAppUser().getEmail()
                ),
                article.getStatus()
        );
    }

    public static NonConfirmedArticleDto nonConfirmedArticleMapper(ArticleToConfirm article) {
        return new NonConfirmedArticleDto(
                article.getId(),
                article.getTitle(),
                article.getContent(),
                article.getPostedDate(),
                article.getLikesNumber(),
                new authorDto(
                        article.getAppUser().getId(),
                        article.getAppUser().getFirstName(),
                        article.getAppUser().getLastName(),
                        article.getAppUser().getEmail()
                ),
                article.getUpdatedAt(),
                article.isUpdated(),
                article.getStatus()
        );
    }

    public static ArticleDto articleDtoMapperWithAdditionalFieldsMapper(Article article, boolean isLiked, int commentsNumber) {
        return new ArticleDto(
                article.getId(),
                article.getTitle(),
                article.getContent(),
                article.getPostedDate(),
                article.getLikesNumber(),
                new authorDto(
                        article.getAppUser().getId(),
                        article.getAppUser().getFirstName(),
                        article.getAppUser().getLastName(),
                        article.getAppUser().getEmail()
                ),
                article.getUpdatedAt(),
                article.isUpdated(),
                article.getStatus(),
                isLiked,
                commentsNumber
        );
    }
}
