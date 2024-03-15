package com.raczkowski.app.dtoMappers;

import com.raczkowski.app.article.Article;
import com.raczkowski.app.dto.ArticleDto;
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
                article.isUpdated()
        );
    }

    public static ArticleDto articleDtoMapperWithAdditionalFields(Article article, boolean isLiked, int commentsNumber) {
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
                isLiked,
                commentsNumber
        );
    }
}
