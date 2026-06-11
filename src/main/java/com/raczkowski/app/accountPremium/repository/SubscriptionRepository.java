package com.raczkowski.app.accountPremium.repository;

import com.raczkowski.app.accountPremium.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByUserId(Long user);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
              update Subscription s
                 set s.active = false
               where s.endDate <= :nowMinute and s.active = true
            """)
    int deactivateExpiredSubscription(@Param("nowMinute") ZonedDateTime nowMinute);
}
