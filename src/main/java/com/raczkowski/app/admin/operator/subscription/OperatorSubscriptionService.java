package com.raczkowski.app.admin.operator.subscription;

import com.raczkowski.app.accountPremium.entity.PlanPrice;
import com.raczkowski.app.accountPremium.entity.PlanPriceDto;
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

    //TODO: Dodac walidacje permissions na operatora
    @Transactional
    public void changePlanPrice(PlanPriceDto.ChangePriceRequest req) {
        if (req.getNewAmount() == null || req.getNewAmount() <= 0) {
            throw new ResponseException("newAmount must be > 0");
        }
        if (req.getCurrency() == null) {
            throw new ResponseException("currency is required");
        }

        SubscriptionPlan plan = subscriptionPlanRepository
                .findById(req.getSubscriptionPlan())
                .orElseThrow(() -> new ResponseException("Plan not found"));

        AppUser user = userService.getLoggedUser();

        Optional<PlanPrice> opt = planPriceRepository.lockByPlanAndCurrency(plan.getId(), req.getCurrency());

        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);

        if (opt.isEmpty()) {
            PlanPrice created = new PlanPrice();
            created.setSubscriptionPlan(plan);
            created.setCurrency(req.getCurrency());
            created.setAmount(req.getNewAmount());
            created.setCreatedAt(now);
            planPriceRepository.save(created);

            PlanPriceHistory hist = new PlanPriceHistory();
            hist.setSubscriptionPlan(plan);
            hist.setCurrency(req.getCurrency());
            hist.setOldAmount(null);
            hist.setNewAmount(req.getNewAmount());
            hist.setCreatedAt(now);
            hist.setChangedBy(user);
            hist.setReason(req.getReason());
            planPriceHistoryRepository.save(hist);
            return;
        }

        PlanPrice current = opt.get();
        Long oldAmount = current.getAmount();
        Long newAmount = req.getNewAmount();

        if (oldAmount != null && oldAmount.equals(newAmount)) {
            return;
        }

        PlanPriceHistory hist = new PlanPriceHistory();
        hist.setSubscriptionPlan(plan);
        hist.setCurrency(req.getCurrency());
        hist.setOldAmount(oldAmount);
        hist.setNewAmount(newAmount);
        hist.setCreatedAt(now);
        hist.setChangedBy(user);
        hist.setReason(req.getReason());
        planPriceHistoryRepository.save(hist);

        current.setAmount(newAmount);
        current.setUpdatedAt(now);
        planPriceRepository.save(current);
    }
}
