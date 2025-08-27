package com.raczkowski.app.limits;

import com.raczkowski.app.accountPremium.FeatureKeys;
import com.raczkowski.app.accountPremium.service.FeaturesLimitService;
import com.raczkowski.app.enums.UserRole;
import com.raczkowski.app.redis.RedisService;
import com.raczkowski.app.user.AppUser;
import com.raczkowski.app.user.UserService;
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
    private final UserService userService;

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
                featuresLimitService.getFeatureLimit(userId, FeatureKeys.COMMENT_COUNT_PER_WEEK),
                featuresLimitService.getFeatureLimit(userId, FeatureKeys.SURVEY_COUNT_PER_WEEK),
                featuresLimitService.getFeatureLimit(userId, FeatureKeys.SURVEY_QUESTION_MAX_QUANTITY),
                featuresLimitService.getFeatureLimit(userId, FeatureKeys.SURVEY_QUESTION_ANSWER_MAX_QUANTITY)
        );
    }

    public boolean canUseFeature(Long userId, String featureKey) {
        AppUser user = userService.getUserById(userId);
        if (user != null && user.getUserRole().equals(UserRole.ADMIN)) {
            return true;
        }

        return getRemainingWeeklyLimit(userId, featureKey) > 0;
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
        AppUser user = userService.getUserById(userId);

        if (user != null && user.getUserRole().equals(UserRole.ADMIN)) {
            return;
        }

        int maxLimit = featuresLimitService.getFeatureLimit(userId, featureKey);

        int redisLimitValue = redisService.getIntValue(usageKey, -1);

        if (redisLimitValue == -1) {
            redisService.setIntValue(usageKey, maxLimit - 1, 7, TimeUnit.DAYS);
        } else {
            redisService.increment(usageKey, -1);
        }
    }
}