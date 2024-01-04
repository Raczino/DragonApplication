package com.raczkowski.app.article;

import com.raczkowski.app.User.AppUser;
import com.raczkowski.app.User.UserService;
import com.raczkowski.app.comment.Comment;
import com.raczkowski.app.comment.CommentRepository;
import com.raczkowski.app.dto.ArticleDto;
import com.raczkowski.app.dto.DtoMapper;
import com.raczkowski.app.exceptions.ArticleException;
import com.raczkowski.app.exceptions.CommentException;
import com.raczkowski.app.likes.ArticleLike;
import com.raczkowski.app.likes.ArticleLikeRepository;
import com.raczkowski.app.likes.CommentLike;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final UserService userService;
    private final CommentRepository commentRepository;
    private final ArticleComparator articleComparator;
    private final ArticleLikeRepository articleLikeRepository;

    public String create(ArticleRequest request) {
        if (request.getTitle().equals("") || request.getContent().equals("")) {
            throw new ArticleException("Title or content can't be empty");
        }
        userService.getLoggedUser()
                .incrementArticlesCount();
        articleRepository.save(new Article(
                request.getTitle(),
                request.getContent(),
                ZonedDateTime.now(ZoneOffset.UTC),
                userService.getLoggedUser()
        ));
        return "saved";
    }

    public List<ArticleDto> getAllArticles() {
        return articleRepository.findAll().stream()
                .sorted(articleComparator)
                .map(DtoMapper::articleDtoMapper)
                .collect(Collectors.toList());
    }

    public List<ArticleDto> getArticlesFromUser(Long userID) {
        return articleRepository.findAll().stream()
                .filter(article -> article.getAppUser().getId().equals(userID))
                .map(DtoMapper::articleDtoMapper)
                .collect(Collectors.toList());
    }

    public String removeArticle(Long id) {
        Article article = articleRepository.findArticleById(id);
        if (!article.getAppUser().getId().equals(userService.getLoggedUser().getId())) {
            throw new ArticleException("User doesn't have permission to remove this article");
        }
        List<Comment> comments = commentRepository.findAll()
                .stream()
                .filter(comment -> comment.getArticle().getId().equals(id))
                .toList();
        for (Comment comment : comments) {
            commentRepository.deleteById(comment.getId());
        }
        articleRepository.deleteById(id);
        return "Removed";
    }

    public Optional<ArticleDto> getArticleByID(Long id) {
        Optional<ArticleDto> articleDto = articleRepository.findAll()
                .stream()
                .filter(article -> article.getId().equals(id))
                .map(DtoMapper::articleDtoMapper)
                .findAny();

        if (articleDto.isEmpty()) {
            throw new ArticleException("There is no article with provided id");
        }
        return articleDto;
    }

    public void likeArticle(Long id) {
        AppUser user = userService.getLoggedUser();

        Optional<Article> article = articleRepository.findById(id);
        if (article.isEmpty()) {
            throw new ArticleException("Article doesnt exists");
        }
        Article article1 = articleRepository.findArticleById(id);
        if (!articleLikeRepository.existsArticleByAppUser(user, article1)) {
            articleLikeRepository.save(new ArticleLike(user, article.get(), true));
            articleRepository.updateArticle(id);
        } else {
            throw new ArticleException("Already liked");
        }
    }
}
