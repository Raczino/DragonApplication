package com.raczkowski.app.admin.moderation.article;

import com.raczkowski.app.admin.common.PermissionValidator;
import com.raczkowski.app.admin.operator.users.ModerationStatisticService;
import com.raczkowski.app.article.Article;
import com.raczkowski.app.article.ArticleService;
import com.raczkowski.app.article.DeletedArticleRepository;
import com.raczkowski.app.article.DeletedArticleService;
import com.raczkowski.app.common.GenericService;
import com.raczkowski.app.common.MetaData;
import com.raczkowski.app.common.PageResponse;
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
import com.raczkowski.app.notification.Notification;
import com.raczkowski.app.notification.NotificationService;
import com.raczkowski.app.user.AppUser;
import com.raczkowski.app.user.UserService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

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

        return paginateAndMap(
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
        AppUser appUser = userService.getLoggedUser();

        ArticleToConfirm articleToConfirm = getArticleToConfirmOrThrow(articleId);

        Article article = new Article(
                articleToConfirm.getTitle(),
                articleToConfirm.getContent(),
                articleToConfirm.getContentHtml(),
                articleToConfirm.getPostedDate(),
                articleToConfirm.getAppUser(),
                ZonedDateTime.now(ZoneOffset.UTC),
                articleToConfirm.getScheduledForDate(),
                appUser
        );
        List<Hashtag> hashtags = new ArrayList<>(articleToConfirm.getHashtags());
        article.setHashtags(hashtags);
        if (articleToConfirm.scheduledForDate != null) {
            article.setStatus(ArticleStatus.SCHEDULED);
        } else {
            article.setStatus(ArticleStatus.APPROVED);
        }
        articleService.saveArticle(article);
        articleToConfirmRepository.deleteArticleToConfirmById(articleId);
        sendNotification(NotificationType.ARTICLE_PUBLISH,
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
        sendNotification(NotificationType.ARTICLE_REJECT,
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

        return paginateAndMap(
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

        return paginateAndMap(
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

        return paginateAndMap(
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

    @Transactional
    public void sendNotification(NotificationType type, String userId, AppUser createdBy, String title, String message, String targetUrl) {
        Notification notification = new Notification(
                userId,
                type,
                title,
                message,
                ZonedDateTime.now(ZoneOffset.UTC),
                createdBy.getFirstName(),
                targetUrl
        );
        notificationService.saveNotification(notification);
        notificationService.sendNotification(userId, notification);
    }

    public List<NonConfirmedArticleDto> getPendingArticlesForUser(Long id) {
        return articleToConfirmRepository.findAll()
                .stream()
                .filter(articleToConfirm -> articleToConfirm.getAppUser().getId().equals(id))
                .map(articleDtoMapper::toNonConfirmedArticleDto)
                .toList();
    }

    private ArticleToConfirm getArticleToConfirmOrThrow(Long id) {
        ArticleToConfirm article = articleToConfirmRepository.getArticleToConfirmById(id);
        if (article == null) {
            throw new ResponseException(ErrorMessages.ARTICLE_ID_NOT_EXISTS);
        }
        return article;
    }

    private <T, R> PageResponse<R> paginateAndMap(
            int pageNumber,
            int pageSize,
            String sortBy,
            String sortDirection,
            java.util.function.Function<org.springframework.data.domain.Pageable, Page<T>> pageSupplier,
            java.util.function.Function<T, R> mapper
    ) {
        Page<T> page = GenericService.paginate(pageNumber, pageSize, sortBy, sortDirection, pageSupplier);
        List<R> content = page.stream().map(mapper).toList();
        return new PageResponse<>(content, new MetaData(
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber() + 1,
                page.getSize()
        ));
    }
}
