package com.raczkowski.app.article;

import com.raczkowski.app.comment.CommentRepository;
import com.raczkowski.app.dto.ArticleDto;
import com.raczkowski.app.dtoMappers.ArticleDtoMapper;
import com.raczkowski.app.exceptions.ArticleException;
import com.raczkowski.app.likes.ArticleLike;
import com.raczkowski.app.likes.ArticleLikeRepository;
import com.raczkowski.app.user.AppUser;
import com.raczkowski.app.user.UserRepository;
import com.raczkowski.app.user.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

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
    private final CommentRepository commentRepository;
    private final ArticleComparator articleComparator;
    private final ArticleLikeRepository articleLikeRepository;

    public String create(ArticleRequest request) {
        if (
                request.getTitle() == null
                        || request.getContent() == null
                        || request.getTitle().equals("")
                        || request.getContent().equals("")
        ) {
            throw new ArticleException("Title or content can't be empty");
        }
        AppUser user = userService.getLoggedUser();
        articleRepository.save(new Article(
                request.getTitle(),
                request.getContent(),
                ZonedDateTime.now(ZoneOffset.UTC),
                user
        ));
        userRepository.updateArticlesCount(user.getId());
        return "saved";
    }

    public List<ArticleDto> getAllArticles() {
        return articleRepository
                .findAll()
                .stream()
                .sorted(articleComparator)
                .map(ArticleDtoMapper::articleDtoMapper)
                .collect(Collectors.toList());
    }

    public List<ArticleDto> getArticlesFromUser(Long userID) {
        return articleRepository
                .findAllByAppUser(userRepository.findById(userID))
                .stream()
                .map(ArticleDtoMapper::articleDtoMapper)
                .collect(Collectors.toList());
    }

    public String removeArticle(Long id) { //TODO: dorobić admin permission allows all
        Article article = articleRepository.findArticleById(id);
        if (!article.getAppUser().getId().equals(userService.getLoggedUser().getId())) {
            throw new ArticleException("User doesn't have permission to remove this article");
        }
        commentRepository.deleteCommentByArticle(article);
        articleRepository.deleteById(id);
        return "Removed";
    }

    public ArticleDto getArticleByID(Long id) {
        Article article = articleRepository.findArticleById(id);
        if (article == null) {
            throw new ArticleException("There is no article with provided id");
        }
        return ArticleDtoMapper.articleDtoMapper(articleRepository.findArticleById(id));
    }

    public void likeArticle(Long id) {
        AppUser user = userService.getLoggedUser();

        Article article = articleRepository.findArticleById(id);
        if (article == null) {
            throw new ArticleException("Article doesnt exists");
        }

        if (!articleLikeRepository.existsArticleLikesByAppUserAndArticle(user, article)) {
            articleLikeRepository.save(new ArticleLike(user, article, true));
            articleRepository.updateArticleLikes(id);
        } else {
            throw new ArticleException("Already liked");
        }
    }

    public void updateArticle(ArticleRequest articleRequest) {
        if ((articleRequest.getTitle() == null || articleRequest.getTitle().equals("")) &&
                (articleRequest.getContent() == null || articleRequest.getContent().equals(""))) {
            throw new ArticleException("Title or content can't be empty");
        }

        Article article = articleRepository.findArticleById(articleRequest.getId());

        if (article == null) {
            throw new ArticleException("There is no article with provided id:" + articleRequest.getId());
        } else if (!article.getAppUser().getId().equals(userService.getLoggedUser().getId())) {
            throw new ArticleException("User doesn't have permission to update this comment");
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
                    article.getContent(),
                    ZonedDateTime.now(ZoneOffset.UTC));
        }
    }
}
