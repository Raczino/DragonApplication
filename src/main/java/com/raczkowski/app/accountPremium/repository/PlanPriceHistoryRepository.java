package com.raczkowski.app.accountPremium.repository;

import com.raczkowski.app.accountPremium.entity.PlanPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanPriceHistoryRepository extends JpaRepository<PlanPriceHistory, Long> {
}