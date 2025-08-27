package com.raczkowski.app.admin.operator.subscription;

import com.raczkowski.app.accountPremium.entity.ChangePriceRequest;
import com.raczkowski.app.accountPremium.entity.PlanPrice;
import com.raczkowski.app.accountPremium.entity.PlanPriceHistory;
import com.raczkowski.app.accountPremium.entity.SubscriptionPlan;
import com.raczkowski.app.accountPremium.repository.PlanPriceHistoryRepository;
import com.raczkowski.app.accountPremium.repository.PlanPriceRepository;
import com.raczkowski.app.accountPremium.repository.SubscriptionPlanRepository;
import com.raczkowski.app.exceptions.ResponseException;
import com.raczkowski.app.user.AppUser;
import com.raczkowski.app.user.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;

@Service
@AllArgsConstructor
public class OperatorSubscriptionService {
    private final PlanPriceHistoryRepository planPriceHistoryRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final PlanPriceRepository planPriceRepository;
    private final UserService userService;
    private final PlanPriceChaneValidator planPriceChaneValidator;

    @Transactional
    public void changePlanPrice(ChangePriceRequest request) {
        AppUser user = userService.getLoggedUser();

        planPriceChaneValidator.validate(request, user);

        SubscriptionPlan plan = subscriptionPlanRepository
                .findById(request.getSubscriptionPlan())
                .orElseThrow(() -> new ResponseException("Plan not found"));

        Optional<PlanPrice> planPrice = planPriceRepository.lockByPlanAndCurrency(plan.getId(), request.getCurrency());

        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);

        if (planPrice.isEmpty()) {
            PlanPrice created = new PlanPrice();
            created.setSubscriptionPlan(plan);
            created.setCurrency(request.getCurrency());
            created.setAmount(request.getNewAmount());
            created.setCreatedAt(now);
            planPriceRepository.save(created);

            PlanPriceHistory history = new PlanPriceHistory();
            history.setSubscriptionPlan(plan);
            history.setCurrency(request.getCurrency());
            history.setOldAmount(null);
            history.setNewAmount(request.getNewAmount());
            history.setCreatedAt(now);
            history.setChangedBy(user);
            history.setReason(request.getReason());
            planPriceHistoryRepository.save(history);
            return;
        }

        PlanPrice current = planPrice.get();
        Long oldAmount = current.getAmount();
        Long newAmount = request.getNewAmount();

        if (oldAmount != null && oldAmount.equals(newAmount)) {
            return;
        }

        PlanPriceHistory history = new PlanPriceHistory();
        history.setSubscriptionPlan(plan);
        history.setCurrency(request.getCurrency());
        history.setOldAmount(oldAmount);
        history.setNewAmount(newAmount);
        history.setCreatedAt(now);
        history.setChangedBy(user);
        history.setReason(request.getReason());
        planPriceHistoryRepository.save(history);

        current.setAmount(newAmount);
        current.setUpdatedAt(now);
        planPriceRepository.save(current);
    }
}
