package com.raczkowski.app.admin.moderation.article;

import com.raczkowski.app.admin.common.PermissionValidator;
import com.raczkowski.app.admin.users.ModerationStatisticService;
import com.raczkowski.app.article.*;
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
import com.raczkowski.app.exceptions.ResponseException;
import com.raczkowski.app.hashtags.Hashtag;
import com.raczkowski.app.notification.Notification;
import com.raczkowski.app.notification.NotificationRepository;
import com.raczkowski.app.notification.NotificationService;
import com.raczkowski.app.surveys.survey.SurveyService;
import com.raczkowski.app.user.AppUser;
import com.raczkowski.app.user.UserRepository;
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

    public void addArticle(ArticleToConfirm articleToConfirm) {
        articleToConfirmRepository.save(articleToConfirm);
    }

    public PageResponse<NonConfirmedArticleDto> getArticleToConfirm(int pageNumber, int pageSize, String sortBy, String sortDirection) {
        permissionValidator.validateIfUserIsAdminOrOperator();
        Page<ArticleToConfirm> article = GenericService
                .paginate(
                        pageNumber,
                        pageSize,
                        sortBy,
                        sortDirection,
                        articleToConfirmRepository::findAll
                );

        return new PageResponse<>(
                article.stream()
                        .map(ArticleDtoMapper::nonConfirmedArticleMapper)
                        .toList(),
                new MetaData(
                        article.getTotalElements(),
                        article.getTotalPages(),
                        article.getNumber() + 1,
                        article.getSize())
        );
    }

    public ArticleDto confirmArticle(Long articleId) {
        permissionValidator.validateIfUserIsAdminOrOperator();
        AppUser appUser = userService.getLoggedUser();

        ArticleToConfirm articleToConfirm = articleToConfirmRepository.getArticleToConfirmById(articleId);
        if (articleToConfirm == null) {
            throw new ResponseException("Article with provided id doesn't not exists");
        }
        Article article = new Article(
                articleToConfirm.getTitle(),
                articleToConfirm.getContent(),
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
        return ArticleDtoMapper.articleDtoMapper(article);
    }

    public RejectedArticleDto rejectArticle(Long articleId) {
        permissionValidator.validateIfUserIsAdminOrOperator();
        AppUser user = userService.getLoggedUser();

        ArticleToConfirm articleToConfirm = articleToConfirmRepository.getArticleToConfirmById(articleId);
        if (articleToConfirm == null) {
            throw new ResponseException("Article with provided id doesn't not exists");
        }
        articleToConfirmRepository.deleteArticleToConfirmById(articleId);
        RejectedArticle rejectedArticle = new RejectedArticle(
                articleToConfirm.getTitle(),
                articleToConfirm.getContent(),
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
        return ArticleDtoMapper.rejectedArticleDtoMapper(rejectedArticle);
    }

    public PageResponse<RejectedArticleDto> getRejectedArticles(int pageNumber, int pageSize, String sortBy, String sortDirection) {
        permissionValidator.validateIfUserIsAdminOrOperator();

        Page<RejectedArticle> article = GenericService
                .paginate(
                        pageNumber,
                        pageSize,
                        sortBy,
                        sortDirection,
                        rejectedArticleRepository::findAll
                );

        return new PageResponse<>(
                article
                        .stream()
                        .map(ArticleDtoMapper::rejectedArticleDtoMapper)
                        .toList(),
                new MetaData(
                        article.getTotalElements(),
                        article.getTotalPages(),
                        article.getNumber() + 1,
                        article.getSize())
        );
    }

    public PageResponse<ArticleDto> getAcceptedArticlesByUser(Long id, int pageNumber, int pageSize, String sortBy, String sortDirection) {
        permissionValidator.validateIfUserIsAdminOrOperator();
        AppUser user = userService.getUserById(id);
        if (user == null) {
            throw new ResponseException("User doesn't exists");
        }

        Page<Article> articles = GenericService
                .paginate(
                        pageNumber,
                        pageSize,
                        sortBy,
                        sortDirection,
                        pageable -> articleService.getArticlesAcceptedByUser(user, pageable)
                );

        return new PageResponse<>(articles
                .stream()
                .map(ArticleDtoMapper::articleDtoMapper)
                .toList(),
                new MetaData(
                        articles.getTotalElements(),
                        articles.getTotalPages(),
                        articles.getNumber() + 1,
                        articles.getSize())
        );
    }

    public void deleteArticle(Long articleId) {
        permissionValidator.validateIfUserIsAdminOrOperator();
        AppUser user = userService.getLoggedUser();
        deletedArticleService.deleteArticle(articleId, ArticleStatus.DELETED_BY_ADMIN, user);
        moderationStatisticService.articleDeletedCounterIncrease(user.getId());
    }

    public PageResponse<DeletedArticleDto> getAllDeletedArticlesByAdmins(int page, int size, String sortBy, String sortDirection) {
        permissionValidator.validateIfUserIsAdminOrOperator();
        Page<DeletedArticle> articles = GenericService
                .paginate(
                        page,
                        size,
                        sortBy,
                        sortDirection,
                        deletedArticleRepository::findAll
                );
        return new PageResponse<>(
                articles
                        .stream()
                        .map(ArticleDtoMapper::deletedArticle)
                        .toList(),
                new MetaData(articles.getTotalElements(),
                        articles.getTotalPages(),
                        articles.getNumber() + 1,
                        articles.getSize())
        );
    }

    public void pinArticle(Long id) {
        permissionValidator.validateIfUserIsAdminOrOperator();
        AppUser user = userService.getLoggedUser();
        if (articleService.getArticleByID(id) == null) {
            throw new ResponseException("There is no article with provided id");
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
                .map(ArticleDtoMapper::nonConfirmedArticleMapper)
                .toList();
    }
}
