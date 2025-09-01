package com.raczkowski.app.accountPremium.service;

import com.raczkowski.app.accountPremium.entity.PlanPrice;
import com.raczkowski.app.accountPremium.entity.PlanPriceHistory;
import com.raczkowski.app.accountPremium.entity.Subscription;
import com.raczkowski.app.accountPremium.entity.SubscriptionPlan;
import com.raczkowski.app.accountPremium.repository.PlanPriceHistoryRepository;
import com.raczkowski.app.accountPremium.repository.PlanPriceRepository;
import com.raczkowski.app.accountPremium.repository.SubscriptionPlanRepository;
import com.raczkowski.app.accountPremium.repository.SubscriptionRepository;
import com.raczkowski.app.dto.PlanPriceDto;
import com.raczkowski.app.dto.SubscriptionPlanDto;
import com.raczkowski.app.dtoMappers.SubscriptionPlanDtoMapper;
import com.raczkowski.app.enums.AccountType;
import com.raczkowski.app.enums.CurrencyCode;
import com.raczkowski.app.enums.PremiumAccountRange;
import com.raczkowski.app.exceptions.ErrorMessages;
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
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final UserRepository userRepository;
    private final SubscriptionPlanDtoMapper subscriptionPlanDtoMapper;
    private final PlanPriceRepository planPriceRepository;
    private final PlanPriceHistoryRepository planPriceHistoryRepository;

    public List<SubscriptionPlanDto> getAllSubscriptionPlans(CurrencyCode currency) {
        List<SubscriptionPlan> plans = subscriptionPlanRepository.findAll();
        if (plans.isEmpty()) return List.of();

        List<Long> planIds = plans.stream().map(SubscriptionPlan::getId).toList();

        Map<Long, PlanPrice> priceByPlanId = planPriceRepository
                .findBySubscriptionPlanIdInAndCurrency(planIds, currency).stream()
                .collect(Collectors.toMap(
                        p -> p.getSubscriptionPlan().getId(),
                        Function.identity()
                ));

        Map<Long, PlanPriceHistory> lastHistByPlanId = planPriceHistoryRepository
                .findLatestHistoryByPlanIdsAndCurrency(planIds, currency).stream()
                .collect(Collectors.toMap(
                        h -> h.getSubscriptionPlan().getId(),
                        Function.identity()
                ));

        return plans.stream().map(plan -> {
            SubscriptionPlanDto dto = subscriptionPlanDtoMapper.toDto(plan);

            PlanPrice current = priceByPlanId.get(plan.getId());
            PlanPriceHistory last = lastHistByPlanId.get(plan.getId());

            if (current != null) {
                PlanPriceDto priceDto = PlanPriceDto.builder()
                        .amount(current.getAmount())
                        .currency(current.getCurrency())
                        .createdAt(current.getCreatedAt())
                        .updatedAt(current.getUpdatedAt())
                        .build();

                if (last != null && last.getOldAmount() != null) {
                    priceDto.setPreviousPrice(last.getOldAmount());
                    priceDto.setChangedAt(last.getCreatedAt());
                }
                dto.setPrice(priceDto);
            }
            return dto;
        }).toList();
    }

    @Transactional
    public void createSubscriptionForUser(Long userId, PremiumAccountRange premiumAccountType) {
        AppUser user = userRepository.getAppUserById(userId);

        if (user == null) {
            throw new ResponseException(ErrorMessages.USER_NOT_EXITS);
        }

        if (subscriptionRepository.findByUserId(user.getId()).isPresent()) {
            throw new ResponseException(ErrorMessages.USER_HAS_SUBSCRIPTION);
        }

        SubscriptionPlan subscriptionPlan = subscriptionPlanRepository.getSubscriptionPlanBySubscriptionType(premiumAccountType)
                .orElseThrow(() -> new ResponseException(ErrorMessages.SUBSCRIPTION_NOT_FOUND));

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
                .orElseThrow(() -> new ResponseException(ErrorMessages.USER_HAS_NO_SUBSCRIPTION));

        if (subscription.isActive()) {
            throw new ResponseException(ErrorMessages.PLAN_ALREADY_ACTIVATED);
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
