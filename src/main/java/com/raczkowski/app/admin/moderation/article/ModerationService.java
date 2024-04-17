package com.raczkowski.app.admin.moderation.article;

import com.raczkowski.app.admin.common.AdminValidator;
import com.raczkowski.app.article.Article;
import com.raczkowski.app.article.ArticleRepository;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

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

        Pageable pageable = PageRequest
                .of(pageNumber - 1, pageSize, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
        Page<ArticleToConfirm> article = articleToConfirmRepository.findAll(pageable);

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
        Pageable pageable = PageRequest
                .of(pageNumber - 1, pageSize, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
        Page<RejectedArticle> article = rejectedArticleRepository.findAll(pageable);

        return new PageResponse<>(
                rejectedArticleRepository.findAll()
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
        AppUser user = userRepository.getAppUserById(id);
        if (user == null) {
            throw new ResponseException("User doesn't exists");
        }

        Pageable pageable = PageRequest
                .of(pageNumber - 1, pageSize, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
        Page<Article> article = articleRepository.getArticleByAcceptedBy(user, pageable);

        return new PageResponse<>(articleRepository.getArticleByAcceptedBy(user, pageable)
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
}
