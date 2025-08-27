package com.raczkowski.app.accountPremium.repository;

import com.raczkowski.app.accountPremium.entity.PremiumFeature;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PremiumFeatureRepository extends JpaRepository<PremiumFeature, Long> {
     PremiumFeature findByFeatureKey(String key);
}
