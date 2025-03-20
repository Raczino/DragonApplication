package com.raczkowski.app.accountPremium;

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
    private final RedisService redisService;

    public Limits getFeaturesLimits(Long userId) {
        return new Limits(
                featuresLimitService.getFeatureLimit(userId, FeatureKeys.ARTICLE_CONTENT_MIN_LENGTH),
                featuresLimitService.getFeatureLimit(userId, FeatureKeys.ARTICLE_CONTENT_MAX_LENGTH),
                featuresLimitService.getFeatureLimit(userId, FeatureKeys.ARTICLE_TITLE_MIN_LENGTH),
                featuresLimitService.getFeatureLimit(userId, FeatureKeys.ARTICLE_TITLE_MAX_LENGTH),
                featuresLimitService.getFeatureLimit(userId, FeatureKeys.ARTICLE_HASHTAG_MAX_LENGTH),
                featuresLimitService.getFeatureLimit(userId, FeatureKeys.COMMENT_CONTENT_MIN_LENGTH),
                featuresLimitService.getFeatureLimit(userId, FeatureKeys.COMMENT_CONTENT_MAX_LENGTH),
                featuresLimitService.getFeatureLimit(userId, FeatureKeys.ARTICLE_COUNT_PER_WEEK),
                featuresLimitService.getFeatureLimit(userId, FeatureKeys.COMMENT_COUNT_PER_WEEK)
        );
    }

    public boolean canUseFeature(Long userId, String featureKey) {
        return getRemainingWeeklyLimit(userId, featureKey) < 0;
    }

    public int getRemainingWeeklyLimit(Long userId, String featureKey) {
        String usageKey = generateWeeklyKey(userId, featureKey);

        int maxLimit = featuresLimitService.getFeatureLimit(userId, featureKey);

        return redisService.getIntValue(usageKey, maxLimit);
    }

    private String generateWeeklyKey(Long userId, String featureKey) {
        String weekYear = Year.now() + "W" + LocalDate.now().get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
        return featureKey + ".usage." + userId + "." + weekYear;
    }

    public void incrementFeatureUsage(Long userId, String featureKey) {
        String usageKey = generateWeeklyKey(userId, featureKey);

        int maxLimit = featuresLimitService.getFeatureLimit(userId, featureKey);

        int redisLimitValue = redisService.getIntValue(usageKey, maxLimit);

        if (redisLimitValue >= maxLimit) {
            redisService.setIntValue(usageKey, maxLimit - 1, 7, TimeUnit.DAYS);
        } else {
            redisService.increment(usageKey, -1);
        }
    }
}