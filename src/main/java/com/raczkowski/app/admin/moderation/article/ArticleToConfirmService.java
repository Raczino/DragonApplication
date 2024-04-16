package com.raczkowski.app.admin.moderation.article;

import com.raczkowski.app.admin.common.AdminValidator;
import com.raczkowski.app.article.Article;
import com.raczkowski.app.article.ArticleRepository;
import com.raczkowski.app.dto.ArticleDto;
import com.raczkowski.app.dto.NonConfirmedArticleDto;
import com.raczkowski.app.dto.RejectedArticleDto;
import com.raczkowski.app.dtoMappers.ArticleDtoMapper;
import com.raczkowski.app.user.UserRepository;
import com.raczkowski.app.user.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@AllArgsConstructor
public class ArticleToConfirmService { //TODO: add more validation on id which not exists

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

    @Transactional
    public ArticleDto confirmArticle(Long articleId) {
        adminValidator.validateIfUserIsAdminOrOperator();

        ArticleToConfirm articleToConfirm = articleToConfirmRepository.getArticleToConfirmById(articleId);
        Article article = new Article(
                articleToConfirm.getTitle(),
                articleToConfirm.getContent(),
                articleToConfirm.getPostedDate(),
                articleToConfirm.getAppUser()
        );
        articleToConfirmRepository.deleteArticleToConfirmById(articleId);
        articleRepository.save(article);
        userRepository.updateArticlesCount(article.getAppUser().getId());

        return ArticleDtoMapper.articleDtoMapper(article);
    }

    @Transactional
    public RejectedArticleDto rejectArticle(Long articleId) {
        adminValidator.validateIfUserIsAdminOrOperator();

        ArticleToConfirm articleToConfirm = articleToConfirmRepository.getArticleToConfirmById(articleId);
        articleToConfirmRepository.deleteArticleToConfirmById(articleId);
        RejectedArticle rejectedArticle = new RejectedArticle(
                articleToConfirm.getTitle(),
                articleToConfirm.getContent(),
                articleToConfirm.getPostedDate(),
                articleToConfirm.getAppUser()
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
