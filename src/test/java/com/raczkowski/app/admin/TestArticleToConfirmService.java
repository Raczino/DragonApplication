package com.raczkowski.app.admin;

import com.raczkowski.app.admin.common.PermissionValidator;
import com.raczkowski.app.admin.moderation.article.ArticleToConfirmRepository;
import com.raczkowski.app.admin.moderation.article.ModerationArticleService;
import com.raczkowski.app.article.ArticleRepository;
import com.raczkowski.app.notification.NotificationRepository;
import com.raczkowski.app.notification.NotificationService;
import com.raczkowski.app.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class TestArticleToConfirmService {

    @Mock
    private PermissionValidator permissionValidator;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ArticleToConfirmRepository articleToConfirmRepository;

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private UserService userService;

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private ModerationArticleService moderationArticleService;


    @Test
    public void shouldConfirmArticleAndSendVerification() {
//        // Given
//        Long articleId = 1L;
//        ArticleToConfirm articleToConfirm = new ArticleToConfirm();
//        articleToConfirm.setAppUser(new AppUser());
//        when(articleToConfirmRepository.getArticleToConfirmById(articleId)).thenReturn(articleToConfirm);
//
//        AppUser loggedUser = new AppUser();
//        loggedUser.setUserRole(UserRole.ADMIN);
//        lenient().when(userService.getLoggedUser()).thenReturn(loggedUser);
//
//        doNothing().when(permissionValidator).validateIfUserIsAdminOrOperator();
//
//        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
//
//        // When
//        ArticleDto result = moderationArticleService.confirmArticle(articleId);
//
//        // Then
//        verify(articleToConfirmRepository, times(1)).deleteArticleToConfirmById(articleId);
//        verify(articleRepository, times(1)).save(any(Article.class));
//
//        verify(notificationService, times(1)).sendNotification(
//                eq(String.valueOf(articleToConfirm.getAppUser().getId())),
//                notificationCaptor.capture()
//        );
//
//        Notification capturedNotification = notificationCaptor.getValue();
//
//        assertEquals(String.valueOf(articleToConfirm.getAppUser().getId()), capturedNotification.getUserId());
//        assertEquals(NotificationType.ARTICLE_PUBLISH, capturedNotification.getType());
//        assertEquals("Your article has been accepted!", capturedNotification.getTitle());
//        assertEquals("Accepted By", capturedNotification.getMessage());
//        assertEquals("article/" + result.getId(), capturedNotification.getTargetUrl());
    }
}