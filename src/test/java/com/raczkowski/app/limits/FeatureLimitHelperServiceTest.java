package com.raczkowski.app.limits;

import com.raczkowski.app.accountPremium.FeatureKeys;
import com.raczkowski.app.accountPremium.service.FeaturesLimitService;
import com.raczkowski.app.enums.UserRole;
import com.raczkowski.app.redis.RedisService;
import com.raczkowski.app.user.AppUser;
import com.raczkowski.app.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.Year;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FeatureLimitHelperServiceTest {

    @Mock
    private FeaturesLimitService featuresLimitService;
    @Mock
    private RedisService redisService;
    @Mock
    private UserService userService;

    @InjectMocks
    private FeatureLimitHelperService featureLimitHelperService;

    @Test
    public void shouldAggregateAllFeatureLimits() {
        // Given
        Long userId = 77L;
        when(featuresLimitService.getFeatureLimit(eq(userId), anyString())).thenReturn(1);

        // When
        Object limits = featureLimitHelperService.getFeaturesLimits(userId);

        // Then
        assertNotNull(limits);
        verify(featuresLimitService).getFeatureLimit(userId, FeatureKeys.ARTICLE_CONTENT_MIN_LENGTH);
        verify(featuresLimitService).getFeatureLimit(userId, FeatureKeys.ARTICLE_CONTENT_MAX_LENGTH);
        verify(featuresLimitService).getFeatureLimit(userId, FeatureKeys.ARTICLE_TITLE_MIN_LENGTH);
        verify(featuresLimitService).getFeatureLimit(userId, FeatureKeys.ARTICLE_TITLE_MAX_LENGTH);
        verify(featuresLimitService).getFeatureLimit(userId, FeatureKeys.ARTICLE_HASHTAG_MAX_LENGTH);
        verify(featuresLimitService).getFeatureLimit(userId, FeatureKeys.COMMENT_CONTENT_MIN_LENGTH);
        verify(featuresLimitService).getFeatureLimit(userId, FeatureKeys.COMMENT_CONTENT_MAX_LENGTH);
        verify(featuresLimitService).getFeatureLimit(userId, FeatureKeys.ARTICLE_COUNT_PER_WEEK);
        verify(featuresLimitService).getFeatureLimit(userId, FeatureKeys.COMMENT_COUNT_PER_WEEK);
        verify(featuresLimitService).getFeatureLimit(userId, FeatureKeys.SURVEY_COUNT_PER_WEEK);
        verify(featuresLimitService).getFeatureLimit(userId, FeatureKeys.SURVEY_QUESTION_MAX_QUANTITY);
        verify(featuresLimitService).getFeatureLimit(userId, FeatureKeys.SURVEY_QUESTION_ANSWER_MAX_QUANTITY);
    }

    @Test
    public void shouldReturnTrueForAdminInCanUseFeature() {
        // Given
        Long userId = 1L;
        AppUser admin = new AppUser();
        admin.setId(userId);
        admin.setUserRole(UserRole.ADMIN);
        when(userService.getUserById(userId)).thenReturn(admin);

        // When
        boolean result = featureLimitHelperService.canUseFeature(userId, FeatureKeys.ARTICLE_COUNT_PER_WEEK);

        // Then
        assertTrue(result);
        verifyNoInteractions(redisService, featuresLimitService);
    }

    @Test
    public void shouldReturnTrueWhenRemainingLimitPositive() {
        // Given
        Long userId = 2L;
        AppUser user = new AppUser();
        user.setId(userId);
        user.setUserRole(UserRole.USER);
        when(userService.getUserById(userId)).thenReturn(user);

        when(featuresLimitService.getFeatureLimit(userId, FeatureKeys.COMMENT_COUNT_PER_WEEK)).thenReturn(5);
        when(redisService.getIntValue(anyString(), eq(5))).thenReturn(3);

        // When
        boolean result = featureLimitHelperService.canUseFeature(userId, FeatureKeys.COMMENT_COUNT_PER_WEEK);

        // Then
        assertTrue(result);
    }

    @Test
    public void shouldReturnFalseWhenRemainingLimitZeroOrLess() {
        // Given
        Long userId = 3L;
        AppUser user = new AppUser();
        user.setId(userId);
        user.setUserRole(UserRole.USER);
        when(userService.getUserById(userId)).thenReturn(user);

        when(featuresLimitService.getFeatureLimit(userId, FeatureKeys.SURVEY_COUNT_PER_WEEK)).thenReturn(2);
        when(redisService.getIntValue(anyString(), eq(2))).thenReturn(0);

        // When
        boolean result = featureLimitHelperService.canUseFeature(userId, FeatureKeys.SURVEY_COUNT_PER_WEEK);

        // Then
        assertFalse(result);
    }

    @Test
    public void shouldComputeWeeklyKeyAndReturnRemainingFromRedis() {
        // Given
        Long userId = 4L;
        String feature = FeatureKeys.ARTICLE_COUNT_PER_WEEK;

        int max = 10;
        when(featuresLimitService.getFeatureLimit(userId, feature)).thenReturn(max);

        String expectedKey = feature + ".usage." + userId + "." + currentWeekKey();

        when(redisService.getIntValue(expectedKey, max)).thenReturn(7);

        // When
        int remaining = featureLimitHelperService.getRemainingWeeklyLimit(userId, feature);

        // Then
        assertEquals(7, remaining);
        verify(redisService).getIntValue(expectedKey, max);
    }

    @Test
    public void shouldDoNothingForAdminOnIncrement() {
        // Given
        Long userId = 5L;
        AppUser admin = new AppUser();
        admin.setId(userId);
        admin.setUserRole(UserRole.ADMIN);
        when(userService.getUserById(userId)).thenReturn(admin);

        // When
        featureLimitHelperService.incrementFeatureUsage(userId, FeatureKeys.COMMENT_COUNT_PER_WEEK);

        // Then
        verifyNoInteractions(redisService, featuresLimitService);
    }

    @Test
    public void shouldInitializeCounterWhenNoValueInRedis() {
        // Given
        Long userId = 6L;
        String feature = FeatureKeys.SURVEY_COUNT_PER_WEEK;

        AppUser user = new AppUser();
        user.setId(userId);
        user.setUserRole(UserRole.USER);
        when(userService.getUserById(userId)).thenReturn(user);

        int max = 8;
        when(featuresLimitService.getFeatureLimit(userId, feature)).thenReturn(max);

        String expectedKey = feature + ".usage." + userId + "." + currentWeekKey();

        when(redisService.getIntValue(expectedKey, -1)).thenReturn(-1);

        // When
        featureLimitHelperService.incrementFeatureUsage(userId, feature);

        // Then
        verify(redisService).setIntValue(expectedKey, max - 1, 7, TimeUnit.DAYS);
        verify(redisService, never()).increment(anyString(), anyInt());
    }

    @Test
    public void shouldDecrementCounterWhenValueExistsInRedis() {
        // Given
        Long userId = 7L;
        String feature = FeatureKeys.SURVEY_COUNT_PER_WEEK;

        AppUser user = new AppUser();
        user.setId(userId);
        user.setUserRole(UserRole.USER);
        when(userService.getUserById(userId)).thenReturn(user);

        int max = 5;
        when(featuresLimitService.getFeatureLimit(userId, feature)).thenReturn(max);

        String expectedKey = feature + ".usage." + userId + "." + currentWeekKey();

        when(redisService.getIntValue(expectedKey, -1)).thenReturn(4);

        // When
        featureLimitHelperService.incrementFeatureUsage(userId, feature);

        // Then
        verify(redisService).increment(expectedKey, -1);
        verify(redisService, never()).setIntValue(anyString(), anyInt(), anyLong(), any());
    }

    private String currentWeekKey() {
        int week = LocalDate.now().get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
        return Year.now() + "W" + week;
    }
}
