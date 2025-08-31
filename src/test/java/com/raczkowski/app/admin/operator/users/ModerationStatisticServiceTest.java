package com.raczkowski.app.admin.operator.users;

import com.raczkowski.app.dto.ModeratorStatisticDto;
import com.raczkowski.app.dtoMappers.StatisticsMapper;
import com.raczkowski.app.user.AppUser;
import com.raczkowski.app.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ModerationStatisticServiceTest {

    @Mock
    private ModeratorStatisticRepository moderatorStatisticRepository;

    @Mock
    private UserService userService;

    @Mock
    private StatisticsMapper statisticsMapper;

    @InjectMocks
    private ModerationStatisticService moderationStatisticService;

    @Test
    public void shouldCreateStatisticWhenNotExists() {
        // Given
        AppUser user = new AppUser();
        user.setId(10L);
        when(userService.getUserById(10L)).thenReturn(user);
        when(moderatorStatisticRepository.existsByAppUser(user)).thenReturn(false);

        // When
        moderationStatisticService.createStatisticForUser(10L);

        // Then
        ArgumentCaptor<ModeratorStatistic> captor = ArgumentCaptor.forClass(ModeratorStatistic.class);
        verify(moderatorStatisticRepository).save(captor.capture());
        assertSame(user, captor.getValue().getAppUser());
    }

    @Test
    public void shouldNotCreateStatisticWhenAlreadyExists() {
        // Given
        AppUser user = new AppUser();
        user.setId(11L);
        when(userService.getUserById(11L)).thenReturn(user);
        when(moderatorStatisticRepository.existsByAppUser(user)).thenReturn(true);

        // When
        moderationStatisticService.createStatisticForUser(11L);

        // Then
        verify(moderatorStatisticRepository, never()).save(any());
    }

    // ===== getStatisticsForUser =====

    @Test
    public void shouldGetStatisticsForUserAndMapToDto() {
        // Given
        AppUser user = new AppUser();
        user.setId(5L);
        when(userService.getUserById(5L)).thenReturn(user);

        ModeratorStatistic stat = new ModeratorStatistic(user);
        when(moderatorStatisticRepository.getModeratorStatisticByAppUser(user)).thenReturn(stat);

        ModeratorStatisticDto dto = mock(ModeratorStatisticDto.class);
        when(statisticsMapper.toModeratorStatisticDto(stat)).thenReturn(dto);

        // When
        ModeratorStatisticDto result = moderationStatisticService.getStatisticsForUser(5L);

        // Then
        assertSame(dto, result);
        verify(userService).getUserById(5L);
        verify(moderatorStatisticRepository).getModeratorStatisticByAppUser(user);
        verify(statisticsMapper).toModeratorStatisticDto(stat);
    }

    @Test
    public void shouldIncreaseApprovedArticleCount() {
        // When
        moderationStatisticService.articleApprovedCounterIncrease(1L);
        // Then
        verify(moderatorStatisticRepository).increaseApprovedArticleCount(1L);
    }

    @Test
    public void shouldIncreaseRejectedArticleCount() {
        moderationStatisticService.articleRejectedCounterIncrease(2L);
        verify(moderatorStatisticRepository).increaseRejectedArticleCount(2L);
    }

    @Test
    public void shouldIncreaseDeletedArticleCount() {
        moderationStatisticService.articleDeletedCounterIncrease(3L);
        verify(moderatorStatisticRepository).increaseDeletedArticleCount(3L);
    }

    @Test
    public void shouldIncreasePinnedArticleCount() {
        moderationStatisticService.articlePinnedCounterIncrease(4L);
        verify(moderatorStatisticRepository).increasePinnedArticleCount(4L);
    }

    @Test
    public void shouldIncreaseDeletedCommentCount() {
        moderationStatisticService.commentDeletedCounterIncrease(5L);
        verify(moderatorStatisticRepository).increaseDeletedCommentCount(5L);
    }

    @Test
    public void shouldIncreaseDeletedSurveyCount() {
        moderationStatisticService.surveyDeletedCounterIncrease(6L);
        verify(moderatorStatisticRepository).increaseDeletedSurveyCount(6L);
    }
}