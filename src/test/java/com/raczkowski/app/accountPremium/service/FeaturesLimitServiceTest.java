package com.raczkowski.app.accountPremium.service;

import com.raczkowski.app.accountPremium.FeatureKeys;
import com.raczkowski.app.accountPremium.entity.PremiumFeature;
import com.raczkowski.app.accountPremium.repository.PremiumFeatureRepository;
import com.raczkowski.app.admin.adminSettings.AdminSetting;
import com.raczkowski.app.admin.adminSettings.AdminSettingsService;
import com.raczkowski.app.redis.RedisService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FeaturesLimitServiceTest {

    @Mock
    private RedisService redisService;

    @Mock
    private AdminSettingsService adminSettingsService;

    @Mock
    private PremiumFeatureRepository premiumFeatureRepository;

    @Mock
    private SubscriptionService subscriptionService;

    @InjectMocks
    private FeaturesLimitService featuresLimitService;

    private static final String NON_GLOBAL_KEY = "some.feature.key";

    @Test
    public void shouldReturnValueFromCacheForPremium() {
        // Given
        when(redisService.getValue("premium." + NON_GLOBAL_KEY)).thenReturn("42");

        // When
        int result = featuresLimitService.getPremiumFeatureLimit(NON_GLOBAL_KEY);

        // Then
        assertEquals(42, result);
        verifyNoInteractions(premiumFeatureRepository);
        verify(redisService, never()).setValue(anyString(), anyString(), anyLong(), any());
    }

    @Test
    public void shouldLoadFromRepoAndCacheForPremiumWhenCacheMiss() {
        // Given
        when(redisService.getValue("premium." + NON_GLOBAL_KEY)).thenReturn(null);
        PremiumFeature pf = mock(PremiumFeature.class);
        when(pf.getValue()).thenReturn(10);
        when(premiumFeatureRepository.findByFeatureKey("premium." + NON_GLOBAL_KEY)).thenReturn(pf);

        // When
        int result = featuresLimitService.getPremiumFeatureLimit(NON_GLOBAL_KEY);

        // Then
        assertEquals(10, result);
        verify(redisService).setValue("premium." + NON_GLOBAL_KEY, "10", 1L, TimeUnit.DAYS);
    }

    @Test
    public void shouldReturnMinusOneWhenPremiumMissingEverywhere() {
        // Given
        when(redisService.getValue("premium." + NON_GLOBAL_KEY)).thenReturn(null);
        when(premiumFeatureRepository.findByFeatureKey("premium." + NON_GLOBAL_KEY)).thenReturn(null);

        // When
        int result = featuresLimitService.getPremiumFeatureLimit(NON_GLOBAL_KEY);

        // Then
        assertEquals(-1, result);
        verify(redisService, never()).setValue(anyString(), anyString(), anyLong(), any());
    }

    @Test
    public void shouldParseStandardFeatureLimitFromAdminSettings() {
        // Given
        AdminSetting setting = mock(AdminSetting.class);
        when(setting.getSettingValue()).thenReturn("5");
        when(adminSettingsService.getSetting("std.key")).thenReturn(setting);

        // When
        int result = featuresLimitService.getStandardFeatureLimit("std.key");

        // Then
        assertEquals(5, result);
    }

    @Test
    public void shouldReturnStandardForGlobalKey_commentMinLength() {
        // Given
        AdminSetting setting = mock(AdminSetting.class);
        when(setting.getSettingValue()).thenReturn("12");
        when(adminSettingsService.getSetting(FeatureKeys.COMMENT_CONTENT_MIN_LENGTH)).thenReturn(setting);
        when(subscriptionService.isSubscriptionActive(100L)).thenReturn(true);

        // When
        int result = featuresLimitService.getFeatureLimit(100L, FeatureKeys.COMMENT_CONTENT_MIN_LENGTH);

        // Then
        assertEquals(12, result);
        verifyNoInteractions(premiumFeatureRepository, redisService);
    }

    @Test
    public void shouldReturnStandardForGlobalKey_articleContentMinLength() {
        // Given
        AdminSetting setting = mock(AdminSetting.class);
        when(setting.getSettingValue()).thenReturn("30");
        when(adminSettingsService.getSetting(FeatureKeys.ARTICLE_CONTENT_MIN_LENGTH)).thenReturn(setting);
        when(subscriptionService.isSubscriptionActive(101L)).thenReturn(false);

        // When
        int result = featuresLimitService.getFeatureLimit(101L, FeatureKeys.ARTICLE_CONTENT_MIN_LENGTH);

        // Then
        assertEquals(30, result);
        verifyNoInteractions(premiumFeatureRepository, redisService);
    }

    @Test
    public void shouldReturnStandardForGlobalKey_articleTitleMinLength() {
        // Given
        AdminSetting setting = mock(AdminSetting.class);
        when(setting.getSettingValue()).thenReturn("8");
        when(adminSettingsService.getSetting(FeatureKeys.ARTICLE_TITLE_MIN_LENGTH)).thenReturn(setting);
        when(subscriptionService.isSubscriptionActive(102L)).thenReturn(true);

        // When
        int result = featuresLimitService.getFeatureLimit(102L, FeatureKeys.ARTICLE_TITLE_MIN_LENGTH);

        // Then
        assertEquals(8, result);
        verifyNoInteractions(premiumFeatureRepository, redisService);
    }

    @Test
    public void shouldReturnPremiumWhenSubscriptionActiveAndPremiumExists() {
        // Given
        when(subscriptionService.isSubscriptionActive(1L)).thenReturn(true);
        when(redisService.getValue("premium." + NON_GLOBAL_KEY)).thenReturn(null);
        PremiumFeature pf = mock(PremiumFeature.class);
        when(pf.getValue()).thenReturn(7);
        when(premiumFeatureRepository.findByFeatureKey("premium." + NON_GLOBAL_KEY)).thenReturn(pf);

        // When
        int result = featuresLimitService.getFeatureLimit(1L, NON_GLOBAL_KEY);

        // Then
        assertEquals(7, result);
        verifyNoInteractions(adminSettingsService);
    }

    @Test
    public void shouldFallbackToStandardWhenPremiumActiveButMissing() {
        // Given
        when(subscriptionService.isSubscriptionActive(2L)).thenReturn(true);
        when(redisService.getValue("premium." + NON_GLOBAL_KEY)).thenReturn(null);
        when(premiumFeatureRepository.findByFeatureKey("premium." + NON_GLOBAL_KEY)).thenReturn(null);

        AdminSetting setting = mock(AdminSetting.class);
        when(setting.getSettingValue()).thenReturn("11");
        when(adminSettingsService.getSetting(NON_GLOBAL_KEY)).thenReturn(setting);

        // When
        int result = featuresLimitService.getFeatureLimit(2L, NON_GLOBAL_KEY);

        // Then
        assertEquals(11, result);
    }

    @Test
    public void shouldReturnStandardWhenSubscriptionInactive() {
        // Given
        when(subscriptionService.isSubscriptionActive(3L)).thenReturn(false);

        AdminSetting setting = mock(AdminSetting.class);
        when(setting.getSettingValue()).thenReturn("4");
        when(adminSettingsService.getSetting(NON_GLOBAL_KEY)).thenReturn(setting);

        // When
        int result = featuresLimitService.getFeatureLimit(3L, NON_GLOBAL_KEY);

        // Then
        assertEquals(4, result);
        verifyNoInteractions(premiumFeatureRepository, redisService);
    }
}
