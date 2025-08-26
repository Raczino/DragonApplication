package com.raczkowski.app.dtoMappers;

import com.raczkowski.app.admin.moderation.article.ArticleToConfirm;
import com.raczkowski.app.admin.moderation.article.RejectedArticle;
import com.raczkowski.app.article.Article;
import com.raczkowski.app.article.DeletedArticle;
import com.raczkowski.app.dto.ArticleDto;
import com.raczkowski.app.dto.DeletedArticleDto;
import com.raczkowski.app.dto.NonConfirmedArticleDto;
import com.raczkowski.app.dto.RejectedArticleDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ArticleDtoMapper {

    private final AuthorDtoMapper authorDtoMapper;

    public ArticleDto toArticleDto(Article article) {
        return ArticleDto.builder()
                .id(article.getId())
                .title(article.getTitle())
                .content(article.getContent())
                .contentHtml(article.getContentHtml())
                .postedDate(article.getPostedDate())
                .likesCount(article.getLikesCount())
                .commentsCount(article.getCommentsCount())
                .author(authorDtoMapper.toAuthorDto(article.getAppUser()))
                .updatedAt(article.getUpdatedAt())
                .isUpdated(article.isUpdated())
                .status(article.getStatus())
                .acceptedAt(article.getAcceptedAt())
                .acceptedBy(authorDtoMapper.toAuthorDto(article.getAcceptedBy()))
                .isPinned(article.isPinned())
                .pinnedBy(authorDtoMapper.toAuthorDto(article.getPinnedBy()))
                .build();
    }

    public RejectedArticleDto toRejectedArticleDto(RejectedArticle article) {
        return RejectedArticleDto.builder()
                .id(article.getId())
                .title(article.getTitle())
                .content(article.getContent())
                .contentHtml(article.getContentHtml())
                .postedDate(article.getPostedDate())
                .author(authorDtoMapper.toAuthorDto(article.getAppUser()))
                .status(article.getStatus())
                .rejectedAt(article.getRejectedAt())
                .rejectedBy(authorDtoMapper.toAuthorDto(article.getRejectedBy()))
                .build();
    }

    public NonConfirmedArticleDto toNonConfirmedArticleDto(ArticleToConfirm article) {
        return NonConfirmedArticleDto.builder()
                .id(article.getId())
                .title(article.getTitle())
                .content(article.getContent())
                .contentHtml(article.getContentHtml())
                .postedDate(article.getPostedDate())
                .author(authorDtoMapper.toAuthorDto(article.getAppUser()))
                .status(article.getStatus())
                .build();
    }

    public DeletedArticleDto toDeletedArticleDto(DeletedArticle article) {
        return DeletedArticleDto.builder()
                .id(article.getId())
                .title(article.getTitle())
                .content(article.getContent())
                .contentHtml(article.getContentHtml())
                .postedDate(article.getPostedDate())
                .likesCount(article.getLikesNumber())
                .commentsCount(article.getCommentsCount())
                .author(authorDtoMapper.toAuthorDto(article.getAppUser()))
                .updatedAt(article.getUpdatedAt())
                .status(article.getStatus())
                .acceptedAt(article.getAcceptedAt())
                .acceptedBy(authorDtoMapper.toAuthorDto(article.getAcceptedBy()))
                .deletedAt(article.getDeletedAt())
                .deletedBy(authorDtoMapper.toAuthorDto(article.getDeletedBy()))
                .build();
    }
}