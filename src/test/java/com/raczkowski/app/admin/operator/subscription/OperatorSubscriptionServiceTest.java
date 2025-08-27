package com.raczkowski.app.admin.operator.subscription;

import com.raczkowski.app.accountPremium.entity.ChangePriceRequest;
import com.raczkowski.app.accountPremium.entity.PlanPrice;
import com.raczkowski.app.accountPremium.entity.SubscriptionPlan;
import com.raczkowski.app.accountPremium.repository.PlanPriceHistoryRepository;
import com.raczkowski.app.accountPremium.repository.PlanPriceRepository;
import com.raczkowski.app.accountPremium.repository.SubscriptionPlanRepository;
import com.raczkowski.app.admin.common.PermissionValidator;
import com.raczkowski.app.enums.CurrencyCode;
import com.raczkowski.app.exceptions.ResponseException;
import com.raczkowski.app.user.AppUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OperatorSubscriptionServiceTest {

    @Mock
    private PlanPriceHistoryRepository planPriceHistoryRepository;
    @Mock
    private SubscriptionPlanRepository subscriptionPlanRepository;
    @Mock
    private PlanPriceRepository planPriceRepository;
    @Mock
    private PlanPriceChangeValidator planPriceChangeValidator;
    @Mock
    private PermissionValidator validator;

    @InjectMocks
    private OperatorSubscriptionService service;

    private AppUser user() {
        return new AppUser();
    }

    private SubscriptionPlan plan(long id) {
        SubscriptionPlan subscriptionPlan = new SubscriptionPlan();
        subscriptionPlan.setId(id);
        return subscriptionPlan;
    }

    private ChangePriceRequest req(long planId, CurrencyCode ccy, String amount, String reason) {
        ChangePriceRequest request = new ChangePriceRequest();
        request.setSubscriptionPlan(planId);
        request.setCurrency(ccy);
        request.setNewAmount(new BigDecimal(amount));
        request.setReason(reason);
        return request;
    }

    @Test
    void shouldThrowExceptionWhenPlanNotFound() {
        // given
        ChangePriceRequest request = req(1L, CurrencyCode.EUR, "10", "init");
        when(validator.validateOperatorOrAdmin()).thenReturn(user());
        doNothing().when(planPriceChangeValidator).validate(request);
        when(subscriptionPlanRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        ResponseException ex = assertThrows(ResponseException.class, () -> service.changePlanPrice(request));
        assertEquals("Plan not found", ex.getMessage());

        verify(planPriceRepository, never()).save(any());
        verify(planPriceHistoryRepository, never()).save(any());
    }

    @Test
    void shouldCreatePriceAndHistoryWhenPriceNotExisting() {
        // given
        ChangePriceRequest request = req(1L, CurrencyCode.EUR, "19.99", "First set");
        AppUser u = user();
        SubscriptionPlan p = plan(1L);

        when(validator.validateOperatorOrAdmin()).thenReturn(u);
        doNothing().when(planPriceChangeValidator).validate(request);
        when(subscriptionPlanRepository.findById(1L)).thenReturn(Optional.of(p));
        when(planPriceRepository.lockByPlanAndCurrency(1L, CurrencyCode.EUR))
                .thenReturn(Optional.empty());

        // when
        service.changePlanPrice(request);

        // then
        verify(planPriceRepository).save(argThat(saved ->
                saved.getSubscriptionPlan() == p &&
                        saved.getCurrency() == CurrencyCode.EUR &&
                        new BigDecimal("19.99").compareTo(saved.getAmount()) == 0 &&
                        saved.getCreatedAt() != null
        ));

        verify(planPriceHistoryRepository).save(argThat(hist ->
                hist.getSubscriptionPlan() == p &&
                        hist.getCurrency() == CurrencyCode.EUR &&
                        hist.getOldAmount() == null &&
                        new BigDecimal("19.99").compareTo(hist.getNewAmount()) == 0 &&
                        u == hist.getChangedBy() &&
                        "First set".equals(hist.getReason()) &&
                        hist.getCreatedAt() != null
        ));

        verifyNoMoreInteractions(planPriceRepository, planPriceHistoryRepository);
    }

    @Test
    void shouldDoNothingWhenNewAmountEqualsCurrent() {
        // given
        ChangePriceRequest request = req(2L, CurrencyCode.EUR, "10.00", "no change");
        AppUser u = user();
        SubscriptionPlan p = plan(2L);

        PlanPrice existing = new PlanPrice();
        existing.setId(100L);
        existing.setSubscriptionPlan(p);
        existing.setCurrency(CurrencyCode.EUR);
        existing.setAmount(new BigDecimal("10.00"));

        when(validator.validateOperatorOrAdmin()).thenReturn(u);
        doNothing().when(planPriceChangeValidator).validate(request);
        when(subscriptionPlanRepository.findById(2L)).thenReturn(Optional.of(p));
        when(planPriceRepository.lockByPlanAndCurrency(2L, CurrencyCode.EUR))
                .thenReturn(Optional.of(existing));

        // when
        service.changePlanPrice(request);

        // then
        verify(planPriceHistoryRepository, never()).save(any());
        verify(planPriceRepository, never()).save(any(PlanPrice.class));
    }

    @Test
    void shouldUpdatePriceAndWriteHistoryWhenNewAmountDiffers() {
        // given
        ChangePriceRequest request = req(3L, CurrencyCode.USD, "25.00", "promotion end");
        AppUser u = user();
        SubscriptionPlan p = plan(3L);

        PlanPrice existing = new PlanPrice();
        existing.setId(200L);
        existing.setSubscriptionPlan(p);
        existing.setCurrency(CurrencyCode.USD);
        existing.setAmount(new BigDecimal("20.00"));

        when(validator.validateOperatorOrAdmin()).thenReturn(u);
        doNothing().when(planPriceChangeValidator).validate(request);
        when(subscriptionPlanRepository.findById(3L)).thenReturn(Optional.of(p));
        when(planPriceRepository.lockByPlanAndCurrency(3L, CurrencyCode.USD))
                .thenReturn(Optional.of(existing));

        // when
        service.changePlanPrice(request);

        // then:
        verify(planPriceHistoryRepository).save(argThat(hist ->
                hist.getSubscriptionPlan() == p &&
                        hist.getCurrency() == CurrencyCode.USD &&
                        new BigDecimal("20.00").compareTo(hist.getOldAmount()) == 0 &&
                        new BigDecimal("25.00").compareTo(hist.getNewAmount()) == 0 &&
                        u == hist.getChangedBy() &&
                        "promotion end".equals(hist.getReason()) &&
                        hist.getCreatedAt() != null
        ));

        verify(planPriceRepository).save(argThat(saved ->
                saved.getId().equals(200L) &&
                        saved.getSubscriptionPlan() == p &&
                        saved.getCurrency() == CurrencyCode.USD &&
                        new BigDecimal("25.00").compareTo(saved.getAmount()) == 0 &&
                        saved.getUpdatedAt() != null
        ));
    }
}
