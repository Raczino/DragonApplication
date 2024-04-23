package com.raczkowski.app.article;

import com.raczkowski.app.admin.moderation.article.ArticleToConfirm;
import com.raczkowski.app.admin.moderation.article.ModerationArticleService;
import com.raczkowski.app.comment.CommentService;
import com.raczkowski.app.common.GenericService;
import com.raczkowski.app.common.MetaData;
import com.raczkowski.app.common.PageResponse;
import com.raczkowski.app.dto.ArticleDto;
import com.raczkowski.app.dtoMappers.ArticleDtoMapper;
import com.raczkowski.app.enums.ArticleStatus;
import com.raczkowski.app.exceptions.ResponseException;
import com.raczkowski.app.likes.ArticleLike;
import com.raczkowski.app.likes.ArticleLikeRepository;
import com.raczkowski.app.user.AppUser;
import com.raczkowski.app.user.UserRepository;
import com.raczkowski.app.user.UserService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final CommentService commentService;
    private final ArticleLikeRepository articleLikeRepository;
    private final ModerationArticleService moderationArticleService;
    private final DeletedArticleService deletedArticleService;

    public ArticleDto create(ArticleRequest request) {
        if (
                request.getTitle() == null
                        || request.getContent() == null
                        || request.getTitle().equals("")
                        || request.getContent().equals("")
        ) {
            throw new ResponseException("Title or content can't be empty");
        }
        AppUser user = userService.getLoggedUser();

        ArticleToConfirm articleToConfirm = new ArticleToConfirm(
                request.getTitle(),
                request.getContent(),
                ZonedDateTime.now(ZoneOffset.UTC),
                user
        );
        moderationArticleService.addArticle(articleToConfirm);

        return ArticleDtoMapper.nonConfirmedArticleMapper(articleToConfirm);
    }

    public PageResponse<ArticleDto> getAllArticles(int pageNumber, int pageSize, String sortBy, String sortDirection) {
        Page<Article> articlePage = GenericService
                .pagination(
                        articleRepository,
                        pageNumber,
                        pageSize,
                        sortBy,
                        sortDirection
                );
        AppUser user = userService.getLoggedUser();


        return new PageResponse<>(
                articlePage
                        .stream()
                        .map(article -> ArticleDtoMapper.articleDtoMapperWithAdditionalFieldsMapper(
                                article,
                                isArticleLiked(article, user),
                                commentService.getNumberCommentsOfArticle(article.getId())
                        ))
                        .toList(),
                new MetaData(
                        articlePage.getTotalElements(),
                        articlePage.getTotalPages(),
                        articlePage.getNumber() + 1,
                        articlePage.getSize()));
    }

    public ArticleDto getMostLikableArticle() {
        return ArticleDtoMapper.articleDtoMapper(articleRepository.getFirstByOrderByLikesNumberDesc());
    }

    public List<ArticleDto> getArticlesFromUser(Long userID) {
        return articleRepository
                .findAllByAppUser(userRepository.findById(userID))
                .stream()
                .map(ArticleDtoMapper::articleDtoMapper)
                .collect(Collectors.toList());
    }

    @Transactional
    public String removeArticle(Long id) {
        deletedArticleService.deleteArticle(id, ArticleStatus.DELETED, null);
        return "Removed";
    }

    public ArticleDto getArticleByID(Long id) {
        Article article = articleRepository.findArticleById(id);
        if (article == null) {
            throw new ResponseException("There is no article with provided id");
        }
        return ArticleDtoMapper.articleDtoMapper(article);
    }

    public void likeArticle(Long id) {
        AppUser user = userService.getLoggedUser();

        Article article = articleRepository.findArticleById(id);
        if (article == null) {
            throw new ResponseException("Article doesnt exists");
        }

        if (!articleLikeRepository.existsArticleLikesByAppUserAndArticle(user, article)) {
            articleLikeRepository.save(new ArticleLike(user, article, true));
            articleRepository.updateArticleLikes(id, 1);
        } else {
            articleLikeRepository.delete(articleLikeRepository.findByArticleAndAppUser(article, user));
            articleRepository.updateArticleLikes(id, -1);
        }
    }

    public void updateArticle(ArticleRequest articleRequest) {
        if ((articleRequest.getTitle() == null || articleRequest.getTitle().equals("")) &&
                (articleRequest.getContent() == null || articleRequest.getContent().equals(""))) {
            throw new ResponseException("Title or content can't be empty");
        }

        Article article = articleRepository.findArticleById(articleRequest.getId());

        if (article == null) {
            throw new ResponseException("There is no article with provided id:" + articleRequest.getId());
        } else if (!article.getAppUser().getId().equals(userService.getLoggedUser().getId())) {
            throw new ResponseException("User doesn't have permission to update this comment");
        }

        if (articleRequest.getTitle() == null) {
            articleRepository.updateArticle(
                    articleRequest.getId(),
                    article.getTitle(),
                    articleRequest.getContent(),
                    ZonedDateTime.now(ZoneOffset.UTC)
            );
        } else if (articleRequest.getContent() == null) {
            articleRepository.updateArticle(
                    articleRequest.getId(),
                    articleRequest.getTitle(),
                    article.getContent(),
                    ZonedDateTime.now(ZoneOffset.UTC)
            );
        } else {
            articleRepository.updateArticle(
                    articleRequest.getId(),
                    articleRequest.getTitle(),
                    articleRequest.getContent(),
                    ZonedDateTime.now(ZoneOffset.UTC));
        }
    }

    public boolean isArticleLiked(Article article, AppUser user) {
        return articleLikeRepository.existsArticleLikesByAppUserAndArticle(user, article);
    }
}
