package com.raczkowski.app.comment;

import com.raczkowski.app.User.AppUser;
import com.raczkowski.app.User.UserRepository;
import com.raczkowski.app.User.UserService;
import com.raczkowski.app.article.Article;
import com.raczkowski.app.article.ArticleRepository;
import com.raczkowski.app.dto.CommentDto;
import com.raczkowski.app.dto.DtoMapper;
import com.raczkowski.app.exceptions.ArticleException;
import com.raczkowski.app.exceptions.CommentException;
import com.raczkowski.app.likes.CommentLike;
import com.raczkowski.app.likes.CommentLikeRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ArticleRepository articleRepository;
    private final CommentComparator commentComparator;
    private final UserService userService;
    private final CommentLikeRepository commentLikeRepository;

    public List<CommentDto> getAllCommentsFromArticle(Long id) {
        return commentRepository.findAll().stream()
                .filter(comment -> comment.getArticle().getId().equals(id))
                .sorted(commentComparator)
                .map(DtoMapper::commentDtoMapper)
                .collect(Collectors.toList());
    }

    public String addComment(CommentRequest commentRequest) {
        AppUser appUser = userRepository.findByEmail(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName());
        appUser.incrementCommentsCount();
        if (!articleRepository.existsById(commentRequest.getId())) {
            throw new ArticleException("Article with this id doesnt exists");
        } else {
            Article article = articleRepository.findArticleById(commentRequest.getId());

            commentRepository.save(new Comment(
                    commentRequest.getContent(),
                    ZonedDateTime.now(ZoneOffset.UTC),
                    appUser,
                    article
            ));
        }
        return "Added";
    }

    public String likeComment(Long id) {
        AppUser user = userRepository.findByEmail(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName());
        Optional<Comment> comment = commentRepository.findById(id);
        if (comment.isEmpty()) {
            throw new CommentException("Comment doesnt exists");
        }

        if(!commentLikeRepository.existsAllByAppUser(user)){
            commentLikeRepository.save(new CommentLike(user, comment.get(), true));
            commentRepository.updateComment(id);
        }
        return "Liked";
    }

    public String removeComment(Long id) {
        Optional<Comment> comment = commentRepository.findById(id);
        if (comment.isPresent() && !comment.get().getAppUser().getId().equals(userService.getLoggedUser().getId())) {
            throw new ArticleException("User doesn't have permission to remove this article");
        }
        commentRepository.deleteById(id);
        return "Removed";
    }
}
