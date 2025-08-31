package com.raczkowski.app.accountPremium.service;

import com.raczkowski.app.accountPremium.entity.PlanPrice;
import com.raczkowski.app.accountPremium.entity.PlanPriceHistory;
import com.raczkowski.app.accountPremium.entity.Subscription;
import com.raczkowski.app.accountPremium.entity.SubscriptionPlan;
import com.raczkowski.app.accountPremium.repository.PlanPriceHistoryRepository;
import com.raczkowski.app.accountPremium.repository.PlanPriceRepository;
import com.raczkowski.app.accountPremium.repository.SubscriptionPlanRepository;
import com.raczkowski.app.accountPremium.repository.SubscriptionRepository;
import com.raczkowski.app.dto.SubscriptionPlanDto;
import com.raczkowski.app.dtoMappers.SubscriptionPlanDtoMapper;
import com.raczkowski.app.enums.AccountType;
import com.raczkowski.app.enums.CurrencyCode;
import com.raczkowski.app.enums.PremiumAccountRange;
import com.raczkowski.app.exceptions.ErrorMessages;
import com.raczkowski.app.exceptions.ResponseException;
import com.raczkowski.app.user.AppUser;
import com.raczkowski.app.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private SubscriptionPlanRepository subscriptionPlanRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubscriptionPlanDtoMapper subscriptionPlanDtoMapper;

    @Mock
    private PlanPriceRepository planPriceRepository;

    @Mock
    private PlanPriceHistoryRepository planPriceHistoryRepository;

    @InjectMocks
    private SubscriptionService subscriptionService;

    @Test
    public void shouldReturnEmptyListWhenNoPlans() {
        // Given
        when(subscriptionPlanRepository.findAll()).thenReturn(List.of());

        // When
        List<SubscriptionPlanDto> result = subscriptionService.getAllSubscriptionPlans(CurrencyCode.EUR);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verifyNoInteractions(planPriceRepository, planPriceHistoryRepository, subscriptionPlanDtoMapper);
    }

    @Test
    public void shouldMapPlansWithCurrentAndPreviousPrice() {
        // Given
        SubscriptionPlan plan1 = new SubscriptionPlan();
        plan1.setId(1L);
        plan1.setDurationDays(30);
        plan1.setSubscriptionType(PremiumAccountRange.MONTHLY);

        when(subscriptionPlanRepository.findAll()).thenReturn(List.of(plan1));

        SubscriptionPlanDto dto1 = new SubscriptionPlanDto();
        when(subscriptionPlanDtoMapper.toDto(plan1)).thenReturn(dto1);

        PlanPrice price1 = mock(PlanPrice.class);
        when(price1.getSubscriptionPlan()).thenReturn(plan1);
        when(price1.getAmount()).thenReturn(new BigDecimal("999"));
        when(price1.getCurrency()).thenReturn(CurrencyCode.EUR);
        ZonedDateTime created = ZonedDateTime.now(ZoneOffset.UTC).minusDays(2);
        ZonedDateTime updated = ZonedDateTime.now(ZoneOffset.UTC).minusDays(1);
        when(price1.getCreatedAt()).thenReturn(created);
        when(price1.getUpdatedAt()).thenReturn(updated);

        when(planPriceRepository.findBySubscriptionPlanIdInAndCurrency(List.of(1L), CurrencyCode.EUR))
                .thenReturn(List.of(price1));

        PlanPriceHistory hist1 = mock(PlanPriceHistory.class);
        when(hist1.getSubscriptionPlan()).thenReturn(plan1);
        when(hist1.getOldAmount()).thenReturn(new BigDecimal("1299"));
        ZonedDateTime changedAt = ZonedDateTime.now(ZoneOffset.UTC).minusDays(1);
        when(hist1.getCreatedAt()).thenReturn(changedAt);

        when(planPriceHistoryRepository.findLatestHistoryByPlanIdsAndCurrency(List.of(1L), CurrencyCode.EUR))
                .thenReturn(List.of(hist1));

        // When
        List<SubscriptionPlanDto> result = subscriptionService.getAllSubscriptionPlans(CurrencyCode.EUR);

        // Then
        assertEquals(1, result.size());
        SubscriptionPlanDto got = result.get(0);
        assertNotNull(got.getPrice());
        assertEquals(new BigDecimal("999"), got.getPrice().getAmount());
        assertEquals(CurrencyCode.EUR, got.getPrice().getCurrency());
        assertEquals(created, got.getPrice().getCreatedAt());
        assertEquals(updated, got.getPrice().getUpdatedAt());
        assertEquals(new BigDecimal("1299"), got.getPrice().getPreviousPrice());
        assertEquals(changedAt, got.getPrice().getChangedAt());
    }

    @Test
    public void shouldMapPlansWithCurrentPriceOnlyWhenNoHistory() {
        // Given
        SubscriptionPlan plan = new SubscriptionPlan();
        plan.setId(5L);
        plan.setDurationDays(30);
        plan.setSubscriptionType(PremiumAccountRange.HALF_YEAR);

        when(subscriptionPlanRepository.findAll()).thenReturn(List.of(plan));

        SubscriptionPlanDto dto = new SubscriptionPlanDto();
        when(subscriptionPlanDtoMapper.toDto(plan)).thenReturn(dto);

        PlanPrice price = mock(PlanPrice.class);
        when(price.getSubscriptionPlan()).thenReturn(plan);
        when(price.getAmount()).thenReturn(new BigDecimal("500"));
        when(price.getCurrency()).thenReturn(CurrencyCode.USD);
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        when(price.getCreatedAt()).thenReturn(now.minusDays(3));
        when(price.getUpdatedAt()).thenReturn(now.minusDays(1));

        when(planPriceRepository.findBySubscriptionPlanIdInAndCurrency(List.of(5L), CurrencyCode.USD))
                .thenReturn(List.of(price));

        when(planPriceHistoryRepository.findLatestHistoryByPlanIdsAndCurrency(List.of(5L), CurrencyCode.USD))
                .thenReturn(List.of());

        // When
        List<SubscriptionPlanDto> result = subscriptionService.getAllSubscriptionPlans(CurrencyCode.USD);

        // Then
        assertEquals(1, result.size());
        SubscriptionPlanDto got = result.get(0);
        assertNotNull(got.getPrice());
        assertEquals(new BigDecimal("500"), got.getPrice().getAmount());
        assertNull(got.getPrice().getPreviousPrice());
        assertNull(got.getPrice().getChangedAt());
    }

    @Test
    public void shouldReturnDtosWithoutPriceWhenNoCurrentPrice() {
        // Given
        SubscriptionPlan plan = new SubscriptionPlan();
        plan.setId(10L);
        plan.setDurationDays(90);
        plan.setSubscriptionType(PremiumAccountRange.YEARLY);

        when(subscriptionPlanRepository.findAll()).thenReturn(List.of(plan));

        SubscriptionPlanDto dto = new SubscriptionPlanDto();
        when(subscriptionPlanDtoMapper.toDto(plan)).thenReturn(dto);

        when(planPriceRepository.findBySubscriptionPlanIdInAndCurrency(List.of(10L), CurrencyCode.PLN))
                .thenReturn(List.of());

        when(planPriceHistoryRepository.findLatestHistoryByPlanIdsAndCurrency(List.of(10L), CurrencyCode.PLN))
                .thenReturn(List.of());

        // When
        List<SubscriptionPlanDto> result = subscriptionService.getAllSubscriptionPlans(CurrencyCode.PLN);

        // Then
        assertEquals(1, result.size());
        assertNull(result.get(0).getPrice());
    }

    @Test
    public void shouldThrowWhenUserNotFound() {
        // Given
        when(userRepository.getAppUserById(77L)).thenReturn(null);

        // When & Then
        ResponseException ex = assertThrows(ResponseException.class,
                () -> subscriptionService.createSubscriptionForUser(77L, PremiumAccountRange.MONTHLY));
        assertEquals(ErrorMessages.USER_NOT_EXITS, ex.getMessage());
        verifyNoInteractions(subscriptionPlanRepository, subscriptionRepository);
    }

    @Test
    public void shouldThrowWhenUserAlreadyHasSubscription() {
        // Given
        AppUser user = new AppUser();
        user.setId(5L);
        when(userRepository.getAppUserById(5L)).thenReturn(user);

        Subscription existing = mock(Subscription.class);
        when(subscriptionRepository.findByUserId(5L)).thenReturn(Optional.of(existing));

        // When & Then
        ResponseException ex = assertThrows(ResponseException.class,
                () -> subscriptionService.createSubscriptionForUser(5L, PremiumAccountRange.HALF_YEAR));
        assertEquals(ErrorMessages.USER_HAS_SUBSCRIPTION, ex.getMessage());
        verify(subscriptionPlanRepository, never()).getSubscriptionPlanBySubscriptionType(any());
        verify(subscriptionRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    public void shouldThrowWhenSubscriptionPlanNotFound() {
        // Given
        AppUser user = new AppUser();
        user.setId(10L);
        when(userRepository.getAppUserById(10L)).thenReturn(user);
        when(subscriptionRepository.findByUserId(10L)).thenReturn(Optional.empty());
        when(subscriptionPlanRepository.getSubscriptionPlanBySubscriptionType(PremiumAccountRange.YEARLY))
                .thenReturn(Optional.empty());

        // When & Then
        ResponseException ex = assertThrows(ResponseException.class,
                () -> subscriptionService.createSubscriptionForUser(10L, PremiumAccountRange.YEARLY));
        assertEquals(ErrorMessages.SUBSCRIPTION_NOT_FOUND, ex.getMessage());
        verify(subscriptionRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    public void shouldCreateSubscriptionAndUpgradeUserToPremium() {
        // Given
        AppUser user = new AppUser();
        user.setId(20L);
        when(userRepository.getAppUserById(20L)).thenReturn(user);
        when(subscriptionRepository.findByUserId(20L)).thenReturn(Optional.empty());

        SubscriptionPlan plan = new SubscriptionPlan();
        plan.setId(2L);
        plan.setDurationDays(30);
        plan.setSubscriptionType(PremiumAccountRange.HALF_YEAR);

        when(subscriptionPlanRepository.getSubscriptionPlanBySubscriptionType(PremiumAccountRange.HALF_YEAR))
                .thenReturn(Optional.of(plan));

        // When
        subscriptionService.createSubscriptionForUser(20L, PremiumAccountRange.HALF_YEAR);

        // Then
        ArgumentCaptor<Subscription> subCaptor = ArgumentCaptor.forClass(Subscription.class);
        verify(subscriptionRepository).save(subCaptor.capture());
        Subscription saved = subCaptor.getValue();

        assertSame(user, saved.getUser());
        assertSame(plan, saved.getSubscriptionPlan());
        assertFalse(saved.isActive());
        assertNotNull(saved.getStartDate());
        assertNotNull(saved.getEndDate());
        long days = ChronoUnit.DAYS.between(
                saved.getStartDate().truncatedTo(ChronoUnit.DAYS),
                saved.getEndDate().truncatedTo(ChronoUnit.DAYS)
        );
        assertEquals(plan.getDurationDays(), days);

        ArgumentCaptor<AppUser> userCaptor = ArgumentCaptor.forClass(AppUser.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals(AccountType.PREMIUM, userCaptor.getValue().getAccountType());
    }
}
