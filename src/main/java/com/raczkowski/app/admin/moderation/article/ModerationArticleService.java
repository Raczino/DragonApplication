package com.raczkowski.app.admin.moderation.article;

import com.raczkowski.app.admin.common.PermissionValidator;
import com.raczkowski.app.admin.operator.users.ModerationStatisticService;
import com.raczkowski.app.article.Article;
import com.raczkowski.app.article.ArticleService;
import com.raczkowski.app.article.DeletedArticleRepository;
import com.raczkowski.app.article.DeletedArticleService;
import com.raczkowski.app.common.pagination.PageMappers;
import com.raczkowski.app.common.pagination.PageResponse;
import com.raczkowski.app.dto.ArticleDto;
import com.raczkowski.app.dto.DeletedArticleDto;
import com.raczkowski.app.dto.NonConfirmedArticleDto;
import com.raczkowski.app.dto.RejectedArticleDto;
import com.raczkowski.app.dtoMappers.ArticleDtoMapper;
import com.raczkowski.app.enums.ArticleStatus;
import com.raczkowski.app.enums.NotificationType;
import com.raczkowski.app.exceptions.ErrorMessages;
import com.raczkowski.app.exceptions.ResponseException;
import com.raczkowski.app.hashtags.Hashtag;
import com.raczkowski.app.notification.NotificationService;
import com.raczkowski.app.user.AppUser;
import com.raczkowski.app.user.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@AllArgsConstructor
@Transactional
public class ModerationArticleService {

    private final ArticleToConfirmRepository articleToConfirmRepository;
    private final RejectedArticleRepository rejectedArticleRepository;
    private final DeletedArticleRepository deletedArticleRepository;
    private final UserService userService;
    private final PermissionValidator permissionValidator;
    private final ArticleService articleService;
    private final DeletedArticleService deletedArticleService;
    private final NotificationService notificationService;
    private final ModerationStatisticService moderationStatisticService;
    private final ArticleDtoMapper articleDtoMapper;

    public void addArticle(ArticleToConfirm articleToConfirm) {
        articleToConfirmRepository.save(articleToConfirm);
    }

    public PageResponse<NonConfirmedArticleDto> getArticleToConfirm(int pageNumber, int pageSize, String sortBy, String sortDirection) {
        permissionValidator.validateIfUserIsAdminOrModerator();

        return PageMappers.paginateAndMap(
                pageNumber,
                pageSize,
                sortBy,
                sortDirection,
                articleToConfirmRepository::findAll,
                articleDtoMapper::toNonConfirmedArticleDto
        );
    }

    public ArticleDto confirmArticle(Long articleId) {
        permissionValidator.validateIfUserIsAdminOrModerator();
        AppUser moderator = userService.getLoggedUser();

        ArticleToConfirm articleToConfirm = getArticleToConfirmOrThrow(articleId);

        Article article = new Article(
                articleToConfirm.getTitle(),
                articleToConfirm.getContent(),
                articleToConfirm.getContentHtml(),
                articleToConfirm.getPostedDate(),
                articleToConfirm.getAppUser(),
                ZonedDateTime.now(ZoneOffset.UTC),
                articleToConfirm.getScheduledForDate(),
                moderator
        );

        Set<Hashtag> tags = new HashSet<>(
                Optional.ofNullable(articleToConfirm.getHashtags()).orElseGet(Collections::emptySet)
        );
        article.setHashtags(tags);

        article.setStatus(articleToConfirm.getScheduledForDate() != null
                ? ArticleStatus.SCHEDULED
                : ArticleStatus.APPROVED);

        articleService.saveArticle(article);
        articleToConfirmRepository.deleteArticleToConfirmById(articleId);

        notificationService.sendNotification(NotificationType.ARTICLE_PUBLISH,
                String.valueOf(article.getAppUser().getId()),
                article.getAcceptedBy(),
                "Your article has been accepted!",
                "Accepted By",
                "article/" + article.getId());

        moderationStatisticService.articleApprovedCounterIncrease(article.getAcceptedBy().getId());

        return articleDtoMapper.toArticleDto(article);
    }

    public RejectedArticleDto rejectArticle(Long articleId) {
        permissionValidator.validateIfUserIsAdminOrModerator();
        AppUser user = userService.getLoggedUser();

        ArticleToConfirm articleToConfirm = getArticleToConfirmOrThrow(articleId);

        articleToConfirmRepository.deleteArticleToConfirmById(articleId);
        RejectedArticle rejectedArticle = new RejectedArticle(
                articleToConfirm.getTitle(),
                articleToConfirm.getContent(),
                articleToConfirm.getContentHtml(),
                articleToConfirm.getPostedDate(),
                articleToConfirm.getAppUser(),
                ZonedDateTime.now(ZoneOffset.UTC),
                user
        );
        rejectedArticleRepository.save(rejectedArticle);
        notificationService.sendNotification(NotificationType.ARTICLE_REJECT,
                String.valueOf(rejectedArticle.getAppUser().getId()),
                rejectedArticle.getRejectedBy(),
                "Your article has been rejected!",
                "Rejected By",
                null);
        moderationStatisticService.articleRejectedCounterIncrease(rejectedArticle.getRejectedBy().getId());
        return articleDtoMapper.toRejectedArticleDto(rejectedArticle);
    }

    public PageResponse<RejectedArticleDto> getRejectedArticles(int pageNumber, int pageSize, String sortBy, String sortDirection) {
        permissionValidator.validateIfUserIsAdminOrModerator();

        return PageMappers.paginateAndMap(
                pageNumber,
                pageSize,
                sortBy,
                sortDirection,
                rejectedArticleRepository::findAll,
                articleDtoMapper::toRejectedArticleDto
        );
    }

    public PageResponse<ArticleDto> getAcceptedArticlesByUser(Long id, int pageNumber, int pageSize, String sortBy, String sortDirection) {
        permissionValidator.validateIfUserIsAdminOrModerator();
        AppUser user = userService.getUserById(id);

        if (user == null) {
            throw new ResponseException(ErrorMessages.USER_NOT_EXITS);
        }

        return PageMappers.paginateAndMap(
                pageNumber,
                pageSize,
                sortBy,
                sortDirection,
                pageable -> articleService.getArticlesAcceptedByUser(user, pageable),
                articleDtoMapper::toArticleDto
        );
    }

    public void deleteArticle(Long articleId) {
        permissionValidator.validateIfUserIsAdminOrModerator();
        AppUser user = userService.getLoggedUser();
        deletedArticleService.deleteArticle(articleId, ArticleStatus.DELETED_BY_ADMIN, user);
        moderationStatisticService.articleDeletedCounterIncrease(user.getId());
    }

    public PageResponse<DeletedArticleDto> getAllDeletedArticlesByAdmins(int pageNumber, int pageSize, String sortBy, String sortDirection) {
        permissionValidator.validateIfUserIsAdminOrModerator();

        return PageMappers.paginateAndMap(
                pageNumber,
                pageSize,
                sortBy,
                sortDirection,
                deletedArticleRepository::findAll,
                articleDtoMapper::toDeletedArticleDto
        );
    }

    public void pinArticle(Long id) {
        permissionValidator.validateIfUserIsAdminOrModerator();
        AppUser user = userService.getLoggedUser();
        if (articleService.getArticleByID(id) == null) {
            throw new ResponseException(ErrorMessages.ARTICLE_ID_NOT_EXISTS);
        }

        articleService.pinArticle(id, user);
        moderationStatisticService.articlePinnedCounterIncrease(user.getId());
    }

    public PageResponse<NonConfirmedArticleDto> getPendingArticlesForUser(Long id, int page, int size, String sortBy, String sortDirection) {
        AppUser user = userService.getUserById(id);
        if (user == null) {
            throw new ResponseException(ErrorMessages.USER_NOT_EXITS);
        }

        return PageMappers.paginateAndMap(
                page,
                size,
                sortBy,
                sortDirection,
                pageable -> articleToConfirmRepository.findByAppUser(user, pageable),
                articleDtoMapper::toNonConfirmedArticleDto
        );
    }

    private ArticleToConfirm getArticleToConfirmOrThrow(Long id) {
        ArticleToConfirm article = articleToConfirmRepository.getArticleToConfirmById(id);
        if (article == null) {
            throw new ResponseException(ErrorMessages.ARTICLE_ID_NOT_EXISTS);
        }
        return article;
    }
}
