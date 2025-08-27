package com.raczkowski.app.accountPremium.repository;

import com.raczkowski.app.accountPremium.entity.PlanPriceHistory;
import com.raczkowski.app.enums.CurrencyCode;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface PlanPriceHistoryRepository extends JpaRepository<PlanPriceHistory, Long> {
    @Query("""
                select h from PlanPriceHistory h
                where h.subscriptionPlan.id in :planIds
                  and h.currency = :currency
                  and h.createdAt = (
                     select max(h2.createdAt)
                     from PlanPriceHistory h2
                     where h2.subscriptionPlan.id = h.subscriptionPlan.id
                       and h2.currency = h.currency
                  )
            """)
    List<PlanPriceHistory> findLatestHistoryByPlanIdsAndCurrency(
            @Param("planIds") Collection<Long> planIds,
            @Param("currency") CurrencyCode currency
    );
}