package com.raczkowski.app.accountPremium;

import com.raczkowski.app.enums.AccountType;
import com.raczkowski.app.enums.PremiumAccountRange;
import com.raczkowski.app.exceptions.ResponseException;
import com.raczkowski.app.user.AppUser;
import com.raczkowski.app.user.UserRepository;
import com.raczkowski.app.user.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

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
            throw new ResponseException("User already has an active subscription");
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
                .orElseThrow(() -> new ResponseException("Subscription plan not found"));

        if (subscription.isActive()) {
            throw new ResponseException("Plan already activated");
        }

        subscription.setActive(true);
        subscriptionRepository.save(subscription);
    }
}
