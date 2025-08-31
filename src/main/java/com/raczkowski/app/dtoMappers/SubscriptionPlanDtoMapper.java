package com.raczkowski.app.dtoMappers;

import com.raczkowski.app.accountPremium.entity.PremiumFeature;
import com.raczkowski.app.accountPremium.entity.SubscriptionPlan;
import com.raczkowski.app.dto.PremiumFeatureDto;
import com.raczkowski.app.dto.SubscriptionPlanDto;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SubscriptionPlanDtoMapper {
    public SubscriptionPlanDto toDto(SubscriptionPlan subscriptionPlan) {
        return SubscriptionPlanDto.builder()
                .id(subscriptionPlan.getId())
                .description(subscriptionPlan.getDescription())
                .name(subscriptionPlan.getName())
                .durationDays(subscriptionPlan.getDurationDays())
                .subscriptionType(subscriptionPlan.getSubscriptionType())
                .premiumFeatures(mapFeatures(subscriptionPlan.getPremiumFeatures()))
                .build();
    }

    private Set<PremiumFeatureDto> mapFeatures(Set<PremiumFeature> features) {
        if (features == null) return Set.of();
        return features.stream()
                .map(f -> PremiumFeatureDto.builder()
                        .id(f.getId())
                        .featureName(f.getFeatureName())
                        .value(f.getValue())
                        .build())
                .collect(Collectors.toSet());
    }
}