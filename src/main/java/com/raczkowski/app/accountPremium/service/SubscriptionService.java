package com.raczkowski.app.accountPremium.service;

import com.raczkowski.app.accountPremium.repository.SubscriptionPlanRepository;
import com.raczkowski.app.accountPremium.repository.SubscriptionRepository;
import com.raczkowski.app.accountPremium.entity.Subscription;
import com.raczkowski.app.accountPremium.entity.SubscriptionPlan;
import com.raczkowski.app.enums.AccountType;
import com.raczkowski.app.enums.PremiumAccountRange;
import com.raczkowski.app.exceptions.ResponseException;
import com.raczkowski.app.user.AppUser;
import com.raczkowski.app.user.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final UserRepository userRepository;

    @Transactional
    public void create(Long userId, PremiumAccountRange premiumAccountType) {
        AppUser user = userRepository.getAppUserById(userId);

        if (user == null) {
            throw new ResponseException("User doesn't exists");
        }

        if (subscriptionRepository.findByUserId(user.getId()).isPresent()) {
            throw new ResponseException("User already has an active subscription"); // to jest chyba błędne
        }

        SubscriptionPlan subscriptionPlan = subscriptionPlanRepository.getSubscriptionPlanBySubscriptionType(premiumAccountType)
                .orElseThrow(() -> new ResponseException("Subscription plan not found"));

        subscriptionRepository.save(new Subscription(user,
                subscriptionPlan,
                ZonedDateTime.now(ZoneOffset.UTC),
                ZonedDateTime.now(ZoneOffset.UTC).plusDays(subscriptionPlan.getDurationDays()),
                false
        ));

        user.setAccountType(AccountType.PREMIUM);
        userRepository.save(user);
    }

    @Transactional
    public void activateSubscription(Long id) {
        Subscription subscription = subscriptionRepository.findByUserId(id)
                .orElseThrow(() -> new ResponseException("User has not a subscription"));

        if (subscription.isActive()) {
            throw new ResponseException("Plan already activated");
        }

        subscription.setActive(true);
        subscriptionRepository.save(subscription);
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkDeactivatedSubscription() {
        List<Subscription> subscriptions = subscriptionRepository.findAll();
        ZonedDateTime currentTime = ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MINUTES);

        for (Subscription subscription : subscriptions) {
            if (subscription.getEndDate().truncatedTo(ChronoUnit.MINUTES).isBefore(currentTime) ||
                    subscription.getEndDate().truncatedTo(ChronoUnit.MINUTES).equals(currentTime)) {
                subscription.setActive(false);
                subscriptionRepository.save(subscription);
            }
        }
    }

    public boolean isSubscriptionActive(Long userId) {
        Optional<Subscription> subscription = subscriptionRepository.findByUserId(userId);

        return subscription.map(Subscription::isActive).orElse(false);
    }
}
