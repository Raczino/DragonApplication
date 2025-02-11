package com.raczkowski.app.accountPremium;

import com.raczkowski.app.enums.PremiumAccountRange;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {
    Optional<SubscriptionPlan> getSubscriptionPlanBySubscriptionType(PremiumAccountRange subscriptionType);
}
