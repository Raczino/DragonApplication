package com.raczkowski.app.accountPremium;

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

    public int getFeatureLimit(String key) {
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

        return Integer.parseInt(adminSettingsService.getSetting(key).getSettingValue());
    }
}
