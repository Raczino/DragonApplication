package com.raczkowski.app.accountPremium;

import com.raczkowski.app.admin.adminSettings.AdminSettingsService;
import com.raczkowski.app.article.Limits;
import com.raczkowski.app.redis.RedisService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Year;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
public class FeatureLimitHelperService {

    private final FeaturesLimitService featuresLimitService;
    private final SubscriptionService subscriptionService;
    private final AdminSettingsService adminSettingsService;
    private final RedisService redisService;

    public Limits getLimits(Long userId) {
        boolean isPremium = subscriptionService.isSubscriptionActive(userId);
        if (isPremium) {
            return new Limits(
                    featuresLimitService.getFeatureLimit(FeatureKeys.ARTICLE_CONTENT_MIN_LENGTH),
                    featuresLimitService.getFeatureLimit(FeatureKeys.ARTICLE_CONTENT_MAX_LENGTH),
                    featuresLimitService.getFeatureLimit(FeatureKeys.ARTICLE_TITLE_MIN_LENGTH),
                    featuresLimitService.getFeatureLimit(FeatureKeys.ARTICLE_TITLE_MAX_LENGTH),
                    featuresLimitService.getFeatureLimit(FeatureKeys.ARTICLE_HASHTAG_MAX_LENGTH),
                    featuresLimitService.getFeatureLimit(FeatureKeys.COMMENT_CONTENT_MIN_LENGTH),
                    featuresLimitService.getFeatureLimit(FeatureKeys.COMMENT_CONTENT_MAX_LENGTH),
                    featuresLimitService.getFeatureLimit(FeatureKeys.ARTICLE_COUNT_PER_WEEK),
                    featuresLimitService.getFeatureLimit(FeatureKeys.COMMENT_COUNT_PER_WEEK)
            );
        } else {
            return new Limits(
                    Integer.parseInt(adminSettingsService.getSetting(FeatureKeys.ARTICLE_CONTENT_MIN_LENGTH).getSettingValue()),
                    Integer.parseInt(adminSettingsService.getSetting(FeatureKeys.ARTICLE_CONTENT_MAX_LENGTH).getSettingValue()),
                    Integer.parseInt(adminSettingsService.getSetting(FeatureKeys.ARTICLE_TITLE_MIN_LENGTH).getSettingValue()),
                    Integer.parseInt(adminSettingsService.getSetting(FeatureKeys.ARTICLE_TITLE_MAX_LENGTH).getSettingValue()),
                    Integer.parseInt(adminSettingsService.getSetting(FeatureKeys.ARTICLE_HASHTAG_MAX_LENGTH).getSettingValue()),
                    Integer.parseInt(adminSettingsService.getSetting(FeatureKeys.COMMENT_CONTENT_MIN_LENGTH).getSettingValue()),
                    Integer.parseInt(adminSettingsService.getSetting(FeatureKeys.COMMENT_CONTENT_MAX_LENGTH).getSettingValue()),
                    Integer.parseInt(adminSettingsService.getSetting(FeatureKeys.ARTICLE_COUNT_PER_WEEK).getSettingValue()),
                    Integer.parseInt(adminSettingsService.getSetting(FeatureKeys.COMMENT_COUNT_PER_WEEK).getSettingValue())
            );
        }
    }

    public boolean canUseFeature(Long userId, String featureKey) {//TODO: Przerobić to i naprawić zapis poprawnego limitu w redis
        return getRemainingWeeklyLimit(userId, featureKey) > 0;
    }

    public int getRemainingWeeklyLimit(Long userId, String featureKey) {
        String usageKey = generateWeeklyKey(userId, featureKey);
        int defaultLimit = featuresLimitService.getFeatureLimit(featureKey);

        return redisService.getIntValue(usageKey, defaultLimit);
    }

    private String generateWeeklyKey(Long userId, String featureKey) {
        String weekYear = Year.now() + "W" + LocalDate.now().get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
        return featureKey + ".usage." + userId + "." + weekYear;
    }

    public void incrementFeatureUsage(Long userId, String featureKey) {
        String usageKey = generateWeeklyKey(userId, featureKey);

        int used = redisService.getIntValue(usageKey, -1);

        if (used == -1) {
            int maxLimit = featuresLimitService.getFeatureLimit(featureKey);

            redisService.setIntValue(usageKey, maxLimit, 7, TimeUnit.DAYS);
        } else {
            redisService.increment(usageKey, -1);
        }
    }
}