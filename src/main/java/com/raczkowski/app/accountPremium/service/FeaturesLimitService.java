package com.raczkowski.app.accountPremium.service;

import com.raczkowski.app.accountPremium.FeatureKeys;
import com.raczkowski.app.accountPremium.repository.PremiumFeatureRepository;
import com.raczkowski.app.accountPremium.entity.PremiumFeature;
import com.raczkowski.app.admin.adminSettings.AdminSettingsService;
import com.raczkowski.app.redis.RedisService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
public class FeaturesLimitService {
    private final RedisService redisService;
    private final AdminSettingsService adminSettingsService;
    private final PremiumFeatureRepository premiumFeatureRepository;
    private final SubscriptionService subscriptionService;

    public int getPremiumFeatureLimit(String key) {
        String cacheKey = "premium." + key;
        String cachedValue = redisService.getValue(cacheKey);

        if (cachedValue != null) {
            return Integer.parseInt(cachedValue);
        }

        PremiumFeature featureValues = premiumFeatureRepository.findByFeatureKey(cacheKey);

        if (featureValues != null) {
            redisService.setValue(cacheKey, String.valueOf(featureValues.getValue()), 1, TimeUnit.DAYS);
            return featureValues.getValue();
        }

        return -1;
    }

    public int getStandardFeatureLimit(String key) {
        return Integer.parseInt(adminSettingsService.getSetting(key).getSettingValue());
    }

    public int getFeatureLimit(Long userId, String key) {
        boolean isPremium = subscriptionService.isSubscriptionActive(userId);
        if (isGlobalSetting(key)) {
            return getStandardFeatureLimit(key);
        }

        if (isPremium) {
            int premiumLimit = getPremiumFeatureLimit(key);
            if (premiumLimit != -1) {
                return premiumLimit;
            }
        }
        return getStandardFeatureLimit(key);
    }

    private boolean isGlobalSetting(String key) {
        return key.equals(FeatureKeys.COMMENT_CONTENT_MIN_LENGTH) ||
                key.equals(FeatureKeys.ARTICLE_CONTENT_MIN_LENGTH) ||
                key.equals(FeatureKeys.ARTICLE_TITLE_MIN_LENGTH);
    }
}
