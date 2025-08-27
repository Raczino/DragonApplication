package com.raczkowski.app.admin.operator.subscription;

import com.raczkowski.app.accountPremium.entity.ChangePriceRequest;
import com.raczkowski.app.enums.CurrencyCode;
import com.raczkowski.app.exceptions.ErrorMessages;
import com.raczkowski.app.exceptions.ResponseException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@AllArgsConstructor
public class PlanPriceChangeValidator {
    public void validate(ChangePriceRequest request) {
        if (request.getSubscriptionPlan() == null) {
            throw new ResponseException(ErrorMessages.SUBSCRIPTION_PLAN_REQUIRED);
        }
        if (request.getNewAmount() == null || request.getNewAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseException(ErrorMessages.AMOUNT_TOO_LOW);
        }
        if (request.getNewAmount().compareTo(new BigDecimal("100000")) > 0) {
            throw new ResponseException(ErrorMessages.AMOUNT_TOO_HIGH);
        }
        if (request.getCurrency() == null) {
            throw new ResponseException(ErrorMessages.CURRENCY_REQUIRED);
        }
        if (request.getCurrency() != CurrencyCode.EUR && request.getCurrency() != CurrencyCode.USD) {
            throw new ResponseException(ErrorMessages.UNSUPPORTED_CURRENCY);
        }
    }
}