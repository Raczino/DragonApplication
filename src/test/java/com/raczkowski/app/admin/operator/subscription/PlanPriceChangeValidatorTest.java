package com.raczkowski.app.admin.operator.subscription;

import com.raczkowski.app.accountPremium.entity.ChangePriceRequest;
import com.raczkowski.app.enums.CurrencyCode;
import com.raczkowski.app.exceptions.ResponseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PlanPriceChangeValidatorTest {

    @InjectMocks
    private PlanPriceChangeValidator planPriceChangeValidator;

    @Test
    public void ShouldThrowExceptionWhenSubscriptionIdIsNull() {
        //Given:
        ChangePriceRequest changePriceRequest = new ChangePriceRequest();

        //When & then
        ResponseException result = assertThrows(ResponseException.class, () -> planPriceChangeValidator.validate(changePriceRequest));
        assertEquals("Subscription plan is required", result.getMessage());
    }

    @Test
    public void ShouldThrowExceptionWhenPriceAmountIsHigherThanLimit() {
        //Given:
        ChangePriceRequest changePriceRequest = new ChangePriceRequest();
        changePriceRequest.setSubscriptionPlan(1L);
        changePriceRequest.setNewAmount(new BigDecimal(100001));

        //When & then
        ResponseException result = assertThrows(ResponseException.class, () -> planPriceChangeValidator.validate(changePriceRequest));
        assertEquals("Amount must be <= 100000", result.getMessage());
    }

    @Test
    public void ShouldThrowExceptionWhenPriceIsBelowZeroOrNull() {
        //Given:
        ChangePriceRequest priceBelowZero = new ChangePriceRequest();
        ChangePriceRequest nullPrice = new ChangePriceRequest();
        priceBelowZero.setSubscriptionPlan(1L);
        nullPrice.setSubscriptionPlan(2L);
        priceBelowZero.setNewAmount(new BigDecimal(-1));
        nullPrice.setNewAmount(null);

        //When & then
        ResponseException first = assertThrows(ResponseException.class, () -> planPriceChangeValidator.validate(priceBelowZero));
        assertEquals("Amount must be > 0", first.getMessage());

        ResponseException second = assertThrows(ResponseException.class, () -> planPriceChangeValidator.validate(nullPrice));
        assertEquals("Amount must be > 0", second.getMessage());
    }

    @Test
    public void ShouldThrowExceptionWhenCurrencyIsNull() {
        //Given:
        ChangePriceRequest changePriceRequest = new ChangePriceRequest();
        changePriceRequest.setSubscriptionPlan(1L);
        changePriceRequest.setNewAmount(new BigDecimal(1));

        //When & then
        ResponseException result = assertThrows(ResponseException.class, () -> planPriceChangeValidator.validate(changePriceRequest));
        assertEquals("Currency is required", result.getMessage());
    }

    @Test
    void shouldPassWhenAllValidMinAndMaxBoundaries() {
        // min boundary
        ChangePriceRequest min = new ChangePriceRequest();
        min.setSubscriptionPlan(1L);
        min.setNewAmount(new BigDecimal(1));
        min.setCurrency(CurrencyCode.USD);
        assertDoesNotThrow(() -> planPriceChangeValidator.validate(min));

        // max boundary
        ChangePriceRequest max = new ChangePriceRequest();
        max.setSubscriptionPlan(2L);
        max.setNewAmount(new BigDecimal(100000));
        max.setCurrency(CurrencyCode.EUR);
        assertDoesNotThrow(() -> planPriceChangeValidator.validate(max));
    }
}