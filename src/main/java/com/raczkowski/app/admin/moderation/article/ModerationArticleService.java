package com.raczkowski.app.admin.moderation.article;

import com.raczkowski.app.admin.common.PermissionValidator;
import com.raczkowski.app.article.*;
import com.raczkowski.app.comment.CommentService;
import com.raczkowski.app.common.GenericService;
import com.raczkowski.app.common.MetaData;
import com.raczkowski.app.common.PageResponse;
import com.raczkowski.app.dto.ArticleDto;
import com.raczkowski.app.dto.DeletedArticleDto;
import com.raczkowski.app.dto.NonConfirmedArticleDto;
import com.raczkowski.app.dto.RejectedArticleDto;
import com.raczkowski.app.dtoMappers.ArticleDtoMapper;
import com.raczkowski.app.enums.ArticleStatus;
import com.raczkowski.app.exceptions.ResponseException;
import com.raczkowski.app.user.AppUser;
import com.raczkowski.app.user.UserRepository;
import com.raczkowski.app.user.UserService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;

@Service
@AllArgsConstructor
@Transactional
public class ModerationArticleService {

    ArticleToConfirmRepository articleToConfirmRepository;
    ArticleRepository articleRepository;
    UserRepository userRepository;
    UserService userService;
    RejectedArticleRepository rejectedArticleRepository;
    PermissionValidator permissionValidator;
    DeletedArticleService deletedArticleService;
    DeletedArticleRepository deletedArticleRepository;
    CommentService commentService;

    public void addArticle(ArticleToConfirm articleToConfirm) {
        articleToConfirmRepository.save(articleToConfirm);
    }

    public PageResponse<NonConfirmedArticleDto> getArticleToConfirm(int pageNumber, int pageSize, String sortBy, String sortDirection) {
        permissionValidator.validateIfUserIsAdminOrOperator();
        Page<ArticleToConfirm> article = GenericService
                .pagination(
                        articleToConfirmRepository,
                        pageNumber,
                        pageSize,
                        sortBy,
                        sortDirection
                );

        return new PageResponse<>(
                article.stream()
                        .map(ArticleDtoMapper::nonConfirmedArticleMapper)
                        .toList(),
                new MetaData(
                        article.getTotalElements(),
                        article.getTotalPages(),
                        article.getNumber() + 1,
                        article.getSize())
        );
    }

    public ArticleDto confirmArticle(Long articleId) {
        permissionValidator.validateIfUserIsAdminOrOperator();
        AppUser appUser = userService.getLoggedUser();

        ArticleToConfirm articleToConfirm = articleToConfirmRepository.getArticleToConfirmById(articleId);
        if (articleToConfirm == null) {
            throw new ResponseException("Article with provided id doesn't not exists");
        }
        Article article = new Article(
                articleToConfirm.getTitle(),
                articleToConfirm.getContent(),
                articleToConfirm.getPostedDate(),
                articleToConfirm.getAppUser(),
                ZonedDateTime.now(ZoneOffset.UTC),
                appUser
        );
        articleToConfirmRepository.deleteArticleToConfirmById(articleId);
        articleRepository.save(article);
        userRepository.updateArticlesCount(article.getAppUser().getId());

        return ArticleDtoMapper.articleDtoMapper(article);
    }

    public RejectedArticleDto rejectArticle(Long articleId) {
        permissionValidator.validateIfUserIsAdminOrOperator();
        AppUser user = userService.getLoggedUser();

        ArticleToConfirm articleToConfirm = articleToConfirmRepository.getArticleToConfirmById(articleId);
        if (articleToConfirm == null) {
            throw new ResponseException("Article with provided id doesn't not exists");
        }
        articleToConfirmRepository.deleteArticleToConfirmById(articleId);
        RejectedArticle rejectedArticle = new RejectedArticle(
                articleToConfirm.getTitle(),
                articleToConfirm.getContent(),
                articleToConfirm.getPostedDate(),
                articleToConfirm.getAppUser(),
                ZonedDateTime.now(ZoneOffset.UTC),
                user
        );
        rejectedArticleRepository.save(rejectedArticle);

        return ArticleDtoMapper.rejectedArticleDtoMapper(rejectedArticle);
    }

    public PageResponse<RejectedArticleDto> getRejectedArticles(int pageNumber, int pageSize, String sortBy, String sortDirection) {
        permissionValidator.validateIfUserIsAdminOrOperator();

        Page<RejectedArticle> article = GenericService
                .pagination(
                        rejectedArticleRepository,
                        pageNumber,
                        pageSize,
                        sortBy,
                        sortDirection
                );

        return new PageResponse<>(
                article
                        .stream()
                        .map(ArticleDtoMapper::rejectedArticleDtoMapper)
                        .toList(),
                new MetaData(
                        article.getTotalElements(),
                        article.getTotalPages(),
                        article.getNumber() + 1,
                        article.getSize())
        );
    }

    public PageResponse<ArticleDto> getAcceptedArticlesByUser(Long id, int pageNumber, int pageSize, String sortBy, String sortDirection) {
        permissionValidator.validateIfUserIsAdminOrOperator();
        Optional<AppUser> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new ResponseException("User doesn't exists");
        }

        Page<Article> article = GenericService
                .paginationOfElementsAcceptedByUser(
                        user,
                        articleRepository,
                        pageNumber,
                        pageSize,
                        sortBy,
                        sortDirection
                );

        return new PageResponse<>(article
                .stream()
                .map(ArticleDtoMapper::articleDtoMapper)
                .toList(),
                new MetaData(
                        article.getTotalElements(),
                        article.getTotalPages(),
                        article.getNumber() + 1,
                        article.getSize())
        );
    }

    public void deleteArticle(Long articleId) {
        permissionValidator.validateIfUserIsAdminOrOperator();
        AppUser user = userService.getLoggedUser();
        deletedArticleService.deleteArticle(articleId, ArticleStatus.DELETED_BY_ADMIN, user);
    }

    public PageResponse<DeletedArticleDto> getAllDeletedArticlesByAdmins(int page, int size, String sortBy, String sortDirection) {
        permissionValidator.validateIfUserIsAdminOrOperator();
        Page<DeletedArticle> articles = GenericService
                .paginationOfDeletedArticles(
                        deletedArticleRepository,
                        page,
                        size,
                        sortBy,
                        sortDirection
                );
        return new PageResponse<>(
                articles
                        .stream()
                        .map(ArticleDtoMapper::deletedArticle)
                        .toList(),
                new MetaData(articles.getTotalElements(),
                        articles.getTotalPages(),
                        articles.getNumber() + 1,
                        articles.getSize())
        );
    }

    public void pinArticle(Long id) {
        permissionValidator.validateIfUserIsAdminOrOperator();

        if (articleRepository.findArticleById(id) == null) {
            throw new ResponseException("There is no article with provided id");
        }

        articleRepository.pinArticle(id);
    }
}
