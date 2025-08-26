package com.raczkowski.app.accountPremium.repository;

import com.raczkowski.app.accountPremium.entity.PlanPrice;
import com.raczkowski.app.enums.CurrencyCode;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import javax.persistence.LockModeType;
import java.util.Optional;

public interface PlanPriceRepository extends JpaRepository<PlanPrice, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PlanPrice p where p.subscriptionPlan.id = :planId and p.currency = :currency")
    Optional<PlanPrice> lockByPlanAndCurrency(@Param("planId") Long planId, @Param("currency") CurrencyCode currency);
}
