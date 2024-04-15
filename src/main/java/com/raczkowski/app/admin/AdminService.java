package com.raczkowski.app.admin;

import com.raczkowski.app.article.Article;
import com.raczkowski.app.article.ArticleRepository;
import com.raczkowski.app.dto.ArticleDto;
import com.raczkowski.app.dto.NonConfirmedArticleDto;
import com.raczkowski.app.dto.RejectedArticleDto;
import com.raczkowski.app.dtoMappers.ArticleDtoMapper;
import com.raczkowski.app.enums.UserRole;
import com.raczkowski.app.exceptions.ResponseException;
import com.raczkowski.app.user.AppUser;
import com.raczkowski.app.user.UserRepository;
import com.raczkowski.app.user.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@AllArgsConstructor
public class AdminService { //TODO: add more validation on id which not exists

    ArticleToConfirmRepository articleToConfirmRepository;
    ArticleRepository articleRepository;
    UserRepository userRepository;
    UserService userService;
    RejectedArticleRepository rejectedArticleRepository;

    public void addArticle(ArticleToConfirm articleToConfirm) {
        articleToConfirmRepository.save(articleToConfirm);
    }

    public List<NonConfirmedArticleDto> getArticleToConfirm() { //TODO: Add pagination and sort
        validateIfUserIsAdminOrOperator();
        return articleToConfirmRepository.findAll()
                .stream()
                .map(ArticleDtoMapper::nonConfirmedArticleMapper)
                .toList();
    }

    @Transactional
    public ArticleDto confirmArticle(Long articleId) {
        validateIfUserIsAdminOrOperator();

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
        validateIfUserIsAdminOrOperator();

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
        validateIfUserIsAdminOrOperator();
        return rejectedArticleRepository.findAll()
                .stream()
                .map(ArticleDtoMapper::rejectedArticleDtoMapper)
                .toList();
    }

    public void validateIfUserIsAdminOrOperator() {
        AppUser user = userService.getLoggedUser();
        if (user.getUserRole() != UserRole.ADMIN && user.getUserRole() != UserRole.MODERATOR) {
            throw new ResponseException("You don't have permissions to do this action");
        }
    }
}
