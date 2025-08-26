package com.raczkowski.app.accountPremium.repository;

import com.raczkowski.app.accountPremium.entity.SubscriptionPlan;
import com.raczkowski.app.enums.PremiumAccountRange;
import org.apache.poi.sl.draw.geom.GuideIf;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {
    Optional<SubscriptionPlan> getSubscriptionPlanBySubscriptionType(PremiumAccountRange subscriptionType);

    Optional<SubscriptionPlan> findSubscriptionPlanById(Long id);
}
