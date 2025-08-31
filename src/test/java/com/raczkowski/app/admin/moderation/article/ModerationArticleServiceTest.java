package com.raczkowski.app.admin.moderation.article;

import com.raczkowski.app.admin.common.PermissionValidator;
import com.raczkowski.app.admin.operator.users.ModerationStatisticService;
import com.raczkowski.app.article.*;
import com.raczkowski.app.common.GenericService;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ModerationArticleServiceTest {

    @Mock
    private ArticleToConfirmRepository articleToConfirmRepository;
    @Mock
    private RejectedArticleRepository rejectedArticleRepository;
    @Mock
    private DeletedArticleRepository deletedArticleRepository;
    @Mock
    private UserService userService;
    @Mock
    private PermissionValidator permissionValidator;
    @Mock
    private ArticleService articleService;
    @Mock
    private DeletedArticleService deletedArticleService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private ModerationStatisticService moderationStatisticService;
    @Mock
    private ArticleDtoMapper articleDtoMapper;

    @InjectMocks
    private ModerationArticleService moderationArticleService;

    @Test
    public void shouldAddArticleToConfirm() {
        // Given
        ArticleToConfirm atc = new ArticleToConfirm();

        // When
        moderationArticleService.addArticle(atc);

        // Then
        verify(articleToConfirmRepository).save(atc);
    }

    @Test
    public void shouldGetArticleToConfirmWithPaginationAndMapping() {
        // Given
        AppUser moderator = new AppUser();
        when(permissionValidator.validateIfUserIsAdminOrModerator()).thenReturn(moderator);

        ArticleToConfirm a1 = new ArticleToConfirm();
        ArticleToConfirm a2 = new ArticleToConfirm();

        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        Page<ArticleToConfirm> page = new PageImpl<>(List.of(a1, a2), pageable, 2);

        try (MockedStatic<GenericService> mocked = mockStatic(GenericService.class)) {
            mocked.when(() -> GenericService.paginate(eq(1), eq(10), eq("postedDate"), eq("DESC"), any()))
                    .thenAnswer(invocation -> {
                        java.util.function.Function<Pageable, Page<ArticleToConfirm>> supplier =
                                invocation.getArgument(4);
                        when(articleToConfirmRepository.findAll(pageable)).thenReturn(page);
                        return supplier.apply(pageable);
                    });

            NonConfirmedArticleDto d1 = mock(NonConfirmedArticleDto.class);
            NonConfirmedArticleDto d2 = mock(NonConfirmedArticleDto.class);
            when(articleDtoMapper.toNonConfirmedArticleDto(a1)).thenReturn(d1);
            when(articleDtoMapper.toNonConfirmedArticleDto(a2)).thenReturn(d2);

            // When
            PageResponse<NonConfirmedArticleDto> result =
                    moderationArticleService.getArticleToConfirm(1, 10, "postedDate", "DESC");

            // Then
            assertEquals(2, result.getItems().size());
            assertEquals(2, result.getMeta().getTotalItems());
            assertEquals(1, result.getMeta().getTotalPages());
            assertEquals(10, result.getMeta().getPageSize());
        }
    }

    @Test
    public void shouldConfirmArticleAsApprovedWhenNoSchedule() {
        // Given
        AppUser approver = new AppUser();
        approver.setId(99L);
        approver.setFirstName("Mod");

        AppUser author = new AppUser();
        author.setId(7L);

        ArticleToConfirm atc = new ArticleToConfirm();
        atc.setId(1L);
        atc.setTitle("T");
        atc.setContent("C");
        atc.setContentHtml("<p>C</p>");
        atc.setPostedDate(ZonedDateTime.now(ZoneOffset.UTC).minusDays(1));
        atc.setAppUser(author);
        atc.setHashtags(List.of(new Hashtag("tag")));

        when(permissionValidator.validateIfUserIsAdminOrModerator()).thenReturn(approver);
        when(userService.getLoggedUser()).thenReturn(approver);
        when(articleToConfirmRepository.getArticleToConfirmById(1L)).thenReturn(atc);

        ArticleDto mapped = mock(ArticleDto.class);
        when(articleDtoMapper.toArticleDto(any(Article.class))).thenReturn(mapped);

        ArgumentCaptor<Article> articleCaptor = ArgumentCaptor.forClass(Article.class);

        // When
        ArticleDto result = moderationArticleService.confirmArticle(1L);

        // Then
        assertSame(mapped, result);
        verify(articleService).saveArticle(articleCaptor.capture());
        Article saved = articleCaptor.getValue();
        assertEquals(ArticleStatus.APPROVED, saved.getStatus());
        assertEquals("T", saved.getTitle());
        assertEquals(author, saved.getAppUser());
        assertEquals(approver, saved.getAcceptedBy());
        assertNotNull(saved.getAcceptedAt());

        verify(articleToConfirmRepository).deleteArticleToConfirmById(1L);
        verify(notificationService).saveNotification(any(Notification.class));
        verify(notificationService).sendNotification(eq(String.valueOf(author.getId())), any(Notification.class));
        verify(moderationStatisticService).articleApprovedCounterIncrease(approver.getId());
    }

    @Test
    public void shouldConfirmArticleAsScheduledWhenHasScheduleDate() {
        // Given
        AppUser approver = new AppUser();
        approver.setId(10L);
        approver.setFirstName("Anna");
        AppUser author = new AppUser();
        author.setId(1L);

        ArticleToConfirm atc = new ArticleToConfirm();
        atc.setId(2L);
        atc.setTitle("T2");
        atc.setContent("C2");
        atc.setContentHtml("<p>C2</p>");
        atc.setPostedDate(ZonedDateTime.now(ZoneOffset.UTC).minusDays(2));
        atc.setAppUser(author);
        atc.setScheduledForDate(ZonedDateTime.now(ZoneOffset.UTC).plusDays(3));

        when(permissionValidator.validateIfUserIsAdminOrModerator()).thenReturn(approver);
        when(userService.getLoggedUser()).thenReturn(approver);
        when(articleToConfirmRepository.getArticleToConfirmById(2L)).thenReturn(atc);
        when(articleDtoMapper.toArticleDto(any(Article.class))).thenReturn(new ArticleDto());

        ArgumentCaptor<Article> articleCaptor = ArgumentCaptor.forClass(Article.class);

        // When
        moderationArticleService.confirmArticle(2L);

        // Then
        verify(articleService).saveArticle(articleCaptor.capture());
        assertEquals(ArticleStatus.SCHEDULED, articleCaptor.getValue().getStatus());
        verify(moderationStatisticService).articleApprovedCounterIncrease(approver.getId());
    }

    @Test
    public void shouldThrowWhenConfirmArticleNotFound() {
        // Given
        when(permissionValidator.validateIfUserIsAdminOrModerator()).thenReturn(new AppUser());
        when(articleToConfirmRepository.getArticleToConfirmById(123L)).thenReturn(null);

        // When & Then
        ResponseException ex = assertThrows(ResponseException.class,
                () -> moderationArticleService.confirmArticle(123L));
        assertEquals(ErrorMessages.ARTICLE_ID_NOT_EXISTS, ex.getMessage());
        verifyNoInteractions(articleService);
    }

    @Test
    public void shouldRejectArticleAndNotify() {
        // Given
        AppUser moderator = new AppUser();
        moderator.setId(3L);
        moderator.setFirstName("Eva");

        AppUser author = new AppUser();
        author.setId(55L);

        ArticleToConfirm atc = new ArticleToConfirm();
        atc.setId(9L);
        atc.setTitle("Rejected");
        atc.setContent("X");
        atc.setContentHtml("<p>X</p>");
        atc.setPostedDate(ZonedDateTime.now(ZoneOffset.UTC).minusDays(1));
        atc.setAppUser(author);

        when(permissionValidator.validateIfUserIsAdminOrModerator()).thenReturn(moderator);
        when(userService.getLoggedUser()).thenReturn(moderator);
        when(articleToConfirmRepository.getArticleToConfirmById(9L)).thenReturn(atc);

        RejectedArticleDto dto = mock(RejectedArticleDto.class);
        when(articleDtoMapper.toRejectedArticleDto(any(RejectedArticle.class))).thenReturn(dto);

        // When
        RejectedArticleDto res = moderationArticleService.rejectArticle(9L);

        // Then
        assertSame(dto, res);
        verify(articleToConfirmRepository).deleteArticleToConfirmById(9L);
        verify(rejectedArticleRepository).save(any(RejectedArticle.class));
        verify(notificationService).saveNotification(any(Notification.class));
        verify(notificationService).sendNotification(eq(String.valueOf(author.getId())), any(Notification.class));
        verify(moderationStatisticService).articleRejectedCounterIncrease(moderator.getId());
    }

    @Test
    public void shouldGetRejectedArticlesWithPaginationAndMapping() {
        // Given
        when(permissionValidator.validateIfUserIsAdminOrModerator()).thenReturn(new AppUser());

        RejectedArticle r1 = new RejectedArticle();
        RejectedArticle r2 = new RejectedArticle();

        when(rejectedArticleRepository.findAll(any(Pageable.class))).thenAnswer(inv -> {
            Pageable used = inv.getArgument(0);
            return new PageImpl<>(List.of(r1, r2), used, 2);
        });

        RejectedArticleDto d1 = mock(RejectedArticleDto.class);
        RejectedArticleDto d2 = mock(RejectedArticleDto.class);
        when(articleDtoMapper.toRejectedArticleDto(r1)).thenReturn(d1);
        when(articleDtoMapper.toRejectedArticleDto(r2)).thenReturn(d2);

        // When
        PageResponse<RejectedArticleDto> out =
                moderationArticleService.getRejectedArticles(1, 5, "rejectedDate", "ASC");

        // Then
        assertEquals(2, out.getItems().size());
        assertEquals(2, out.getMeta().getTotalItems());
        assertEquals(1, out.getMeta().getTotalPages());
        assertEquals(1, out.getMeta().getCurrentPage());
        assertEquals(5, out.getMeta().getPageSize());
    }

    @Test
    public void shouldThrowWhenAcceptedArticlesUserNotFound() {
        // Given
        when(permissionValidator.validateIfUserIsAdminOrModerator()).thenReturn(new AppUser());
        when(userService.getUserById(77L)).thenReturn(null);

        // When & Then
        ResponseException ex = assertThrows(ResponseException.class,
                () -> moderationArticleService.getAcceptedArticlesByUser(77L, 1, 10, "created", "DESC"));
        assertEquals(ErrorMessages.USER_NOT_EXITS, ex.getMessage());
    }

    @Test
    public void shouldGetAcceptedArticlesByUserWithPaginationAndMapping() {
        // Given
        when(permissionValidator.validateIfUserIsAdminOrModerator()).thenReturn(new AppUser());

        AppUser reviewer = new AppUser();
        reviewer.setId(5L);
        when(userService.getUserById(5L)).thenReturn(reviewer);

        Article a1 = new Article();
        Article a2 = new Article();

        Pageable pageable = PageRequest.of(0, 3);
        Page<Article> page = new PageImpl<>(List.of(a1, a2), pageable, 2);

        try (MockedStatic<GenericService> mocked = mockStatic(GenericService.class)) {
            mocked.when(() -> GenericService.paginate(eq(1), eq(3), eq("acceptedDate"), eq("DESC"), any()))
                    .thenAnswer(invocation -> {
                        java.util.function.Function<Pageable, Page<Article>> supplier =
                                invocation.getArgument(4);

                        when(articleService.getArticlesAcceptedByUser(eq(reviewer), eq(pageable)))
                                .thenReturn(page);

                        return supplier.apply(pageable);
                    });

            ArticleDto d1 = mock(ArticleDto.class);
            ArticleDto d2 = mock(ArticleDto.class);
            when(articleDtoMapper.toArticleDto(a1)).thenReturn(d1);
            when(articleDtoMapper.toArticleDto(a2)).thenReturn(d2);

            // When
            PageResponse<ArticleDto> out =
                    moderationArticleService.getAcceptedArticlesByUser(5L, 1, 3, "acceptedDate", "DESC");

            // Then
            assertEquals(2, out.getItems().size());
            assertEquals(2, out.getMeta().getTotalItems());
            assertEquals(1, out.getMeta().getTotalPages());
            assertEquals(3, out.getMeta().getPageSize());
        }
    }


    @Test
    public void shouldDeleteArticleAndIncreaseStat() {
        // Given
        AppUser admin = new AppUser();
        admin.setId(1L);
        when(permissionValidator.validateIfUserIsAdminOrModerator()).thenReturn(admin);
        when(userService.getLoggedUser()).thenReturn(admin);

        // When
        moderationArticleService.deleteArticle(100L);

        // Then
        verify(deletedArticleService).deleteArticle(100L, ArticleStatus.DELETED_BY_ADMIN, admin);
        verify(moderationStatisticService).articleDeletedCounterIncrease(1L);
    }

    @Test
    public void shouldGetAllDeletedArticlesByAdminsWithPaginationAndMapping() {
        // Given
        when(permissionValidator.validateIfUserIsAdminOrModerator()).thenReturn(new AppUser());

        DeletedArticle d1 = new DeletedArticle();
        DeletedArticle d2 = new DeletedArticle();

        when(deletedArticleRepository.findAll(any(Pageable.class))).thenAnswer(inv -> {
            Pageable used = inv.getArgument(0);
            // totalElements = 2
            return new PageImpl<>(List.of(d1, d2), used, 2);
        });

        DeletedArticleDto dto1 = mock(DeletedArticleDto.class);
        DeletedArticleDto dto2 = mock(DeletedArticleDto.class);
        when(articleDtoMapper.toDeletedArticleDto(d1)).thenReturn(dto1);
        when(articleDtoMapper.toDeletedArticleDto(d2)).thenReturn(dto2);

        // When
        PageResponse<DeletedArticleDto> out =
                moderationArticleService.getAllDeletedArticlesByAdmins(1, 4, "deletedDate", "DESC");

        // Then
        assertEquals(2, out.getItems().size());
        assertEquals(2, out.getMeta().getTotalItems());
        assertEquals(1, out.getMeta().getTotalPages());
        assertEquals(1, out.getMeta().getCurrentPage());
        assertEquals(4, out.getMeta().getPageSize());
    }


    @Test
    public void shouldThrowWhenPinArticleNotFound() {
        // Given
        when(permissionValidator.validateIfUserIsAdminOrModerator()).thenReturn(new AppUser());
        when(articleService.getArticleByID(55L)).thenReturn(null);

        // When & Then
        ResponseException ex = assertThrows(ResponseException.class,
                () -> moderationArticleService.pinArticle(55L));
        assertEquals(ErrorMessages.ARTICLE_ID_NOT_EXISTS, ex.getMessage());
        verify(moderationStatisticService, never()).articlePinnedCounterIncrease(anyLong());
    }

    @Test
    public void shouldPinArticleAndIncreaseStat() {
        // Given
        AppUser mod = new AppUser();
        mod.setId(8L);
        when(permissionValidator.validateIfUserIsAdminOrModerator()).thenReturn(mod);
        when(userService.getLoggedUser()).thenReturn(mod);

        Article exists = new Article();
        when(articleService.getArticleByID(9L)).thenReturn(exists);

        // When
        moderationArticleService.pinArticle(9L);

        // Then
        verify(articleService).pinArticle(9L, mod);
        verify(moderationStatisticService).articlePinnedCounterIncrease(8L);
    }

    @Test
    public void shouldSaveAndSendNotification() {
        // Given
        AppUser by = new AppUser();
        by.setFirstName("Kate");

        // When
        moderationArticleService.sendNotification(
                NotificationType.ARTICLE_PUBLISH,
                "7",
                by,
                "t", "m", "/article/1"
        );

        // Then
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationService).saveNotification(captor.capture());
        verify(notificationService).sendNotification(eq("7"), any(Notification.class));

        Notification n = captor.getValue();
        assertEquals("7", n.getUserId());
        assertEquals(NotificationType.ARTICLE_PUBLISH, n.getType());
        assertEquals("t", n.getTitle());
        assertEquals("m", n.getMessage());
        assertEquals("Kate", n.getCreatedBy());
        assertEquals("/article/1", n.getTargetUrl());
        assertNotNull(n.getCreatedAt());
    }

    @Test
    public void shouldReturnPendingArticlesForSpecificUserOnly() {
        // Given
        AppUser u1 = new AppUser();
        u1.setId(1L);
        AppUser u2 = new AppUser();
        u2.setId(2L);

        ArticleToConfirm a1 = new ArticleToConfirm();
        a1.setAppUser(u1);
        ArticleToConfirm a2 = new ArticleToConfirm();
        a2.setAppUser(u2);
        ArticleToConfirm a3 = new ArticleToConfirm();
        a3.setAppUser(u1);

        when(articleToConfirmRepository.findAll()).thenReturn(List.of(a1, a2, a3));

        NonConfirmedArticleDto d1 = mock(NonConfirmedArticleDto.class);
        NonConfirmedArticleDto d3 = mock(NonConfirmedArticleDto.class);
        when(articleDtoMapper.toNonConfirmedArticleDto(a1)).thenReturn(d1);
        when(articleDtoMapper.toNonConfirmedArticleDto(a3)).thenReturn(d3);

        // When
        List<NonConfirmedArticleDto> out = moderationArticleService.getPendingArticlesForUser(1L);

        // Then
        assertEquals(2, out.size());
        verify(articleDtoMapper, times(1)).toNonConfirmedArticleDto(a1);
        verify(articleDtoMapper, times(1)).toNonConfirmedArticleDto(a3);
        verify(articleDtoMapper, never()).toNonConfirmedArticleDto(a2);
    }
}
