package com.raczkowski.app.admin;

import com.raczkowski.app.admin.common.AdminValidator;
import com.raczkowski.app.admin.moderation.article.ArticleToConfirm;
import com.raczkowski.app.admin.moderation.article.ArticleToConfirmRepository;
import com.raczkowski.app.admin.moderation.article.ModerationService;
import com.raczkowski.app.admin.moderation.article.RejectedArticleRepository;
import com.raczkowski.app.article.Article;
import com.raczkowski.app.article.ArticleRepository;
import com.raczkowski.app.dto.ArticleDto;
import com.raczkowski.app.enums.UserRole;
import com.raczkowski.app.user.AppUser;
import com.raczkowski.app.user.UserRepository;
import com.raczkowski.app.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ArticleToConfirmServiceTest {

    @Mock
    private ArticleToConfirmRepository articleToConfirmRepository;

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RejectedArticleRepository rejectedArticleRepository;

    @Mock
    private AdminValidator adminValidator;

    @InjectMocks
    private ModerationService moderationService;

    @Mock
    private UserService userService;


    @Test
    public void shouldConfirmArticle() {
        // Given
        Long articleId = 1L;
        ArticleToConfirm articleToConfirm = new ArticleToConfirm();
        articleToConfirm.setAppUser(new AppUser());
        when(articleToConfirmRepository.getArticleToConfirmById(articleId)).thenReturn(articleToConfirm);

        // Ustaw odpowiednie zachowanie dla userService.getLoggedUser()
        AppUser loggedUser = new AppUser();
        loggedUser.setUserRole(UserRole.ADMIN);
        lenient().when(userService.getLoggedUser()).thenReturn(loggedUser);

        // When
        ArticleDto result = moderationService.confirmArticle(articleId);

        // Then
        verify(articleToConfirmRepository, times(1)).deleteArticleToConfirmById(articleId);
        verify(articleRepository, times(1)).save(any(Article.class));
    }
}
