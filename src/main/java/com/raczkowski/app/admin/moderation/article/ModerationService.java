package com.raczkowski.app.admin.moderation.article;

import com.raczkowski.app.admin.common.AdminValidator;
import com.raczkowski.app.article.Article;
import com.raczkowski.app.article.ArticleRepository;
import com.raczkowski.app.dto.ArticleDto;
import com.raczkowski.app.dto.NonConfirmedArticleDto;
import com.raczkowski.app.dto.RejectedArticleDto;
import com.raczkowski.app.dtoMappers.ArticleDtoMapper;
import com.raczkowski.app.exceptions.ResponseException;
import com.raczkowski.app.user.AppUser;
import com.raczkowski.app.user.UserRepository;
import com.raczkowski.app.user.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@AllArgsConstructor
@Transactional
public class ModerationService { //TODO: add more validation on id which not exists

    ArticleToConfirmRepository articleToConfirmRepository;
    ArticleRepository articleRepository;
    UserRepository userRepository;
    UserService userService;
    RejectedArticleRepository rejectedArticleRepository;
    AdminValidator adminValidator;

    public void addArticle(ArticleToConfirm articleToConfirm) {
        articleToConfirmRepository.save(articleToConfirm);
    }

    public List<NonConfirmedArticleDto> getArticleToConfirm() { //TODO: Add pagination and sort
        adminValidator.validateIfUserIsAdminOrOperator();
        return articleToConfirmRepository.findAll()
                .stream()
                .map(ArticleDtoMapper::nonConfirmedArticleMapper)
                .toList();
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

    public List<RejectedArticleDto> getRejectedArticles() { //TODO: Add pagination and sort
        adminValidator.validateIfUserIsAdminOrOperator();
        return rejectedArticleRepository.findAll()
                .stream()
                .map(ArticleDtoMapper::rejectedArticleDtoMapper)
                .toList();
    }

}
