package com.raczkowski.app.admin;

import com.raczkowski.app.admin.common.PermissionValidator;
import com.raczkowski.app.admin.moderation.article.ArticleToConfirm;
import com.raczkowski.app.admin.moderation.article.ArticleToConfirmRepository;
import com.raczkowski.app.admin.moderation.article.ModerationArticleService;
import com.raczkowski.app.admin.operator.users.ModerationStatisticService;
import com.raczkowski.app.article.Article;
import com.raczkowski.app.article.ArticleService;
import com.raczkowski.app.dto.ArticleDto;
import com.raczkowski.app.dtoMappers.ArticleDtoMapper;
import com.raczkowski.app.enums.NotificationType;
import com.raczkowski.app.enums.UserRole;
import com.raczkowski.app.notification.Notification;
import com.raczkowski.app.notification.NotificationService;
import com.raczkowski.app.user.AppUser;
import com.raczkowski.app.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestArticleToConfirmService {

    @Mock
    private PermissionValidator permissionValidator;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ArticleToConfirmRepository articleToConfirmRepository;

    @Mock
    private ArticleService articleService;

    @Mock
    private UserService userService;

    @Mock
    private ArticleDtoMapper articleDtoMapper;

    @Mock
    private ModerationStatisticService moderationStatisticService;

    @InjectMocks
    private ModerationArticleService moderationArticleService;


    @Test
    public void shouldConfirmArticleAndSendVerification() {
        // Given
        Long articleId = 1L;

        AppUser author = new AppUser();
        author.setId(10L);

        ArticleToConfirm articleToConfirm = new ArticleToConfirm();
        articleToConfirm.setAppUser(author);

        when(articleToConfirmRepository.getArticleToConfirmById(articleId)).thenReturn(articleToConfirm);

        AppUser loggedUser = new AppUser();
        loggedUser.setUserRole(UserRole.ADMIN);
        when(userService.getLoggedUser()).thenReturn(loggedUser);

        when(permissionValidator.validateIfUserIsAdminOrOperator()).thenReturn(loggedUser);

        doAnswer(invocation -> {
            Article art = invocation.getArgument(0);
            art.setId(123L);
            return null;
        }).when(articleService).saveArticle(any(Article.class));

        when(articleDtoMapper.toArticleDto(any(Article.class))).thenAnswer(invocation -> {
            Article article = invocation.getArgument(0);
            ArticleDto dto = new ArticleDto();
            dto.setId(article.getId());
            return dto;
        });

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);

        // When
        ArticleDto result = moderationArticleService.confirmArticle(articleId);

        // Then
        verify(articleService, times(1)).saveArticle(any(Article.class));
        verify(articleToConfirmRepository, times(1)).deleteArticleToConfirmById(articleId);

        verify(notificationService, times(1)).sendNotification(
                eq(String.valueOf(articleToConfirm.getAppUser().getId())),
                notificationCaptor.capture()
        );

        Notification capturedNotification = notificationCaptor.getValue();

        assertEquals(String.valueOf(articleToConfirm.getAppUser().getId()), capturedNotification.getUserId());
        assertEquals(NotificationType.ARTICLE_PUBLISH, capturedNotification.getType());
        assertEquals("Your article has been accepted!", capturedNotification.getTitle());
        assertEquals("Accepted By", capturedNotification.getMessage());
        assertEquals("article/" + result.getId(), capturedNotification.getTargetUrl());
    }
}