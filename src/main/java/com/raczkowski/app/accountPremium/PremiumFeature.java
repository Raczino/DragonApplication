package com.raczkowski.app.accountPremium;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "premium_feature")
@Getter
@Setter
public class PremiumFeature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "feature_name", nullable = false, unique = true)
    private String featureName;

    @ManyToMany(mappedBy = "premiumFeatures")
    private Set<SubscriptionPlan> subscriptionPlans;

    private String featureKey;

    private int value;
}
