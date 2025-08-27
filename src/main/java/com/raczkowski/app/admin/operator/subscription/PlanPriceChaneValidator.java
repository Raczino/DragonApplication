package com.raczkowski.app.admin.operator.subscription;

import com.raczkowski.app.accountPremium.entity.ChangePriceRequest;
import com.raczkowski.app.admin.common.PermissionValidator;
import com.raczkowski.app.exceptions.ResponseException;
import com.raczkowski.app.user.AppUser;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class PlanPriceChaneValidator {
    private final PermissionValidator validator;

    public void validate(ChangePriceRequest request, AppUser user) {
        if (!validator.validateOperatorOrAdmin(user)) {
            throw new ResponseException("You don't have the permission!");
        }
        if (request.getSubscriptionPlan() == null) {
            throw new ResponseException("Subscription plan is required");
        }
        if (request.getNewAmount() == null || request.getNewAmount() <= 0) {
            throw new ResponseException("Amount must be > 0");
        }
        if (request.getCurrency() == null) {
            throw new ResponseException("currency is required");
        }
    }
}