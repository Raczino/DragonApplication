package com.raczkowski.app.admin.moderation.article;

import com.raczkowski.app.admin.common.AdminValidator;
import com.raczkowski.app.article.Article;
import com.raczkowski.app.article.ArticleRepository;
import com.raczkowski.app.common.GenericService;
import com.raczkowski.app.common.MetaData;
import com.raczkowski.app.common.PageResponse;
import com.raczkowski.app.dto.ArticleDto;
import com.raczkowski.app.dto.NonConfirmedArticleDto;
import com.raczkowski.app.dto.RejectedArticleDto;
import com.raczkowski.app.dtoMappers.ArticleDtoMapper;
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
public class ModerationService {

    ArticleToConfirmRepository articleToConfirmRepository;
    ArticleRepository articleRepository;
    UserRepository userRepository;
    UserService userService;
    RejectedArticleRepository rejectedArticleRepository;
    AdminValidator adminValidator;

    public void addArticle(ArticleToConfirm articleToConfirm) {
        articleToConfirmRepository.save(articleToConfirm);
    }

    public PageResponse<NonConfirmedArticleDto> getArticleToConfirm(int pageNumber, int pageSize, String sortBy, String sortDirection) {
        adminValidator.validateIfUserIsAdminOrOperator();
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
        adminValidator.validateIfUserIsAdminOrOperator();
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
        adminValidator.validateIfUserIsAdminOrOperator();
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
        adminValidator.validateIfUserIsAdminOrOperator();

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
        adminValidator.validateIfUserIsAdminOrOperator();
        Optional<AppUser> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new ResponseException("User doesn't exists");
        }

        Page<Article> article = GenericService
                .pagination(
                        articleRepository,
                        pageNumber,
                        pageSize,
                        sortBy,
                        sortDirection
                );

        return new PageResponse<>(article
                .stream()
                .filter(art -> art.getAcceptedBy().getId().equals(user.get().getId()))
                .map(ArticleDtoMapper::articleDtoMapper)
                .toList(),
                new MetaData(
                        article.getTotalElements(),
                        article.getTotalPages(),
                        article.getNumber() + 1,
                        article.getSize())
        );
    }
}
