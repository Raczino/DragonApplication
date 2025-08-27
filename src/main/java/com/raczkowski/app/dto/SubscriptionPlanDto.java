package com.raczkowski.app.dto;

import com.raczkowski.app.enums.PremiumAccountRange;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class SubscriptionPlanDto {
    private Long id;
    private String description;
    private String name;
    private int durationDays;
    private PremiumAccountRange subscriptionType;
    private Set<PremiumFeatureDto> premiumFeatures;
    private PlanPriceDto price;
}