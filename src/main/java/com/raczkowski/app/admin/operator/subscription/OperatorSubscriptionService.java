package com.raczkowski.app.admin.operator.subscription;

import com.raczkowski.app.accountPremium.entity.ChangePriceRequest;
import com.raczkowski.app.accountPremium.entity.PlanPrice;
import com.raczkowski.app.accountPremium.entity.PlanPriceHistory;
import com.raczkowski.app.accountPremium.entity.SubscriptionPlan;
import com.raczkowski.app.accountPremium.repository.PlanPriceHistoryRepository;
import com.raczkowski.app.accountPremium.repository.PlanPriceRepository;
import com.raczkowski.app.accountPremium.repository.SubscriptionPlanRepository;
import com.raczkowski.app.admin.common.PermissionValidator;
import com.raczkowski.app.exceptions.ResponseException;
import com.raczkowski.app.user.AppUser;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;

@Service
@AllArgsConstructor
public class OperatorSubscriptionService {
    private final PlanPriceHistoryRepository planPriceHistoryRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final PlanPriceRepository planPriceRepository;
    private final PlanPriceChangeValidator planPriceChangeValidator;
    private final PermissionValidator validator;

    @Transactional
    public void changePlanPrice(ChangePriceRequest request) {
        AppUser user = validator.validateOperatorOrAdmin();
        planPriceChangeValidator.validate(request);

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
        BigDecimal oldAmount = current.getAmount();
        BigDecimal newAmount = request.getNewAmount();

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
