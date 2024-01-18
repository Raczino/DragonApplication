package com.raczkowski.app.article;

import com.raczkowski.app.comment.CommentRepository;
import com.raczkowski.app.dto.ArticleDto;
import com.raczkowski.app.dto.ArticleDtoMapper;
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
        if (request.getTitle().equals("") || request.getContent().equals("")) {
            throw new ArticleException("Title or content can't be empty");
        }
        articleRepository.save(new Article(
                request.getTitle(),
                request.getContent(),
                ZonedDateTime.now(ZoneOffset.UTC),
                userService.getLoggedUser()
        ));
        userService.getLoggedUser()
                .incrementArticlesCount();
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

    public String removeArticle(Long id) {
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
            articleRepository.updateArticle(id);
        } else {
            throw new ArticleException("Already liked");
        }
    }
}
