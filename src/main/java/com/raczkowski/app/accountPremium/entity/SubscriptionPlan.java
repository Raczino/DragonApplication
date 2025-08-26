package com.raczkowski.app.accountPremium.entity;

import com.raczkowski.app.enums.PremiumAccountRange;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "subscription_plan")
@Getter
@Setter
public class SubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "duration_days", nullable = false)
    private int durationDays;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_type", nullable = false)
    private PremiumAccountRange subscriptionType;

    @ManyToMany
    @JoinTable(
            name = "subscription_plan_features",
            joinColumns = @JoinColumn(name = "subscription_plan_id"),
            inverseJoinColumns = @JoinColumn(name = "premium_feature_id")
    )
    private Set<PremiumFeature> premiumFeatures;
}
