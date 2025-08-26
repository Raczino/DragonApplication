package com.raczkowski.app.accountPremium.repository;

import com.raczkowski.app.accountPremium.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByUserId(Long user);
}
