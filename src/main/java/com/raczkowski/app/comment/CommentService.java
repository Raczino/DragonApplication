package com.raczkowski.app.comment;

import com.raczkowski.app.accountPremium.FeatureKeys;
import com.raczkowski.app.article.ArticleRepository;
import com.raczkowski.app.common.GenericService;
import com.raczkowski.app.common.MetaData;
import com.raczkowski.app.common.PageResponse;
import com.raczkowski.app.dto.CommentDto;
import com.raczkowski.app.dtoMappers.CommentDtoMapper;
import com.raczkowski.app.enums.UserRole;
import com.raczkowski.app.exceptions.ResponseException;
import com.raczkowski.app.likes.CommentLike;
import com.raczkowski.app.likes.CommentLikeRepository;
import com.raczkowski.app.limits.FeatureLimitHelperService;
import com.raczkowski.app.user.AppUser;
import com.raczkowski.app.user.UserService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

@AllArgsConstructor
@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;
    private final UserService userService;
    private final CommentLikeRepository commentLikeRepository;
    private final CommentRequestValidator commentRequestValidator;
    private final FeatureLimitHelperService featureLimitHelperService;
    private final CommentDtoMapper commentDtoMapper;

    public PageResponse<CommentDto> getCommentsForArticle(Long articleId, int pageNumber, int pageSize) {
        AppUser user = userService.getLoggedUser();

        return paginateAndMapCommentsWithLikes(
                pageNumber,
                pageSize,
                "likesCount",
                pageable -> commentRepository.findCommentsByArticleWithPinnedFirst(articleId, pageable),
                user
        );
    }

    public CommentDto addComment(CommentRequest commentRequest) {
        AppUser user = userService.getLoggedUser();
        commentRequestValidator.validateCreationRequest(commentRequest, user);


        if (!articleRepository.existsById(commentRequest.getId())) {
            throw new ResponseException("Article with this id doesnt exists");
        }

        Comment comment = new Comment(commentRequest.getContent(),
                ZonedDateTime.now(ZoneOffset.UTC),
                user,
                articleRepository.findArticleById(commentRequest.getId()
                ));

        commentRepository.save(comment);
        articleRepository.updateArticleLikesCount(commentRequest.getId(), 1);
        featureLimitHelperService.incrementFeatureUsage(user.getId(), FeatureKeys.COMMENT_COUNT_PER_WEEK);

        return commentDtoMapper.toCommentDto(comment);
    }

    public void createComment(Comment comment) {
        commentRepository.save(comment);
    }

    public void likeComment(Long id) {
        AppUser user = userService.getLoggedUser();
        Comment comment = commentRepository.findCommentById(id);
        if (comment == null) {
            throw new ResponseException("Comment doesnt exists");
        }

        if (!commentLikeRepository.existsCommentLikeByAppUserAndComment(userService.getLoggedUser(), comment)) {
            commentLikeRepository.save(new CommentLike(userService.getLoggedUser(), comment, true));
            if (commentLikeRepository.existsCommentLikeByAppUserAndComment(userService.getLoggedUser(), comment)) {
                commentRepository.updateCommentLikesCount(comment.getId(), 1);
            }
        } else {
            commentLikeRepository.delete(commentLikeRepository.findByCommentAndAppUser(comment, user));
            if (!commentLikeRepository.existsCommentLikeByAppUserAndComment(userService.getLoggedUser(), comment)) {
                commentRepository.updateCommentLikesCount(comment.getId(), -1);
            }
        }
    }

    public String removeComment(Long id) {
        Comment comment = commentRepository.findCommentById(id);
        if (comment == null) {
            throw new ResponseException("Comment doesn't exists");
        }

        AppUser user = userService.getLoggedUser();

        if (!comment.getAppUser().getId().equals(user.getId()) || (!user.getUserRole().equals(UserRole.ADMIN) && !user.getUserRole().equals(UserRole.MODERATOR))) {
            throw new ResponseException("User doesn't have permission to remove this comment");
        }

        commentRepository.deleteById(id);

        if (commentRepository.findCommentById(id) == null) {
            articleRepository.updateArticleLikesCount(comment.getArticle().getId(), -1);
        }
        return "Removed";
    }

    public void updateComment(CommentRequest commentRequest) {

        if (commentRequest.getContent() == null || commentRequest.getContent().isEmpty()) {
            throw new ResponseException("Comment can't be empty");
        }

        Comment comment = commentRepository.findCommentById(commentRequest.getId());

        if (comment == null) {
            throw new ResponseException("There is no comment with provided id:" + commentRequest.getId());
        } else if (!comment.getAppUser().getId().equals(userService.getLoggedUser().getId())) {
            throw new ResponseException("User doesn't have permission to update this comment");
        }

        commentRepository.updateCommentContent(
                commentRequest.getId(),
                commentRequest.getContent(),
                ZonedDateTime.now(ZoneOffset.UTC)
        );
    }

    public void pinComment(Long id) {
        commentRepository.pinComment(id);
    }

    public PageResponse<CommentDto> getCommentsForUser(Long userId, int pageNumber, int pageSize) {
        AppUser user = userService.getUserById(userId);

        return paginateAndMapCommentsWithLikes(
                pageNumber,
                pageSize,
                "postedDate",
                pageable -> commentRepository.getCommentsByAppUser(user, pageable),
                user
        );
    }

    public int getCommentsCountForUser(AppUser appUser) {
        return commentRepository.findAllByAppUser(appUser).size();
    }

    private Set<Long> getLikedCommentIdsByUser(List<Comment> comments, AppUser user) {
        return commentLikeRepository.findLikedCommentIdsByUserAndCommentIds(
                user,
                comments.stream().map(Comment::getId).toList()
        );
    }

    private PageResponse<CommentDto> paginateAndMapCommentsWithLikes(
            int pageNumber,
            int pageSize,
            String sortBy,
            Function<Pageable, Page<Comment>> pageSupplier,
            AppUser user
    ) {
        Page<Comment> page = GenericService.paginate(pageNumber, pageSize, sortBy, "DESC", pageSupplier);
        Set<Long> likedCommentIds = getLikedCommentIdsByUser(page.getContent(), user);

        List<CommentDto> commentDtos = page.getContent().stream()
                .map(comment -> {
                    CommentDto dto = commentDtoMapper.toCommentDto(comment);
                    dto.setLiked(likedCommentIds.contains(comment.getId()));
                    return dto;
                })
                .toList();

        return new PageResponse<>(
                commentDtos,
                new MetaData(
                        page.getTotalElements(),
                        page.getTotalPages(),
                        page.getNumber() + 1,
                        page.getSize()
                )
        );
    }
}
