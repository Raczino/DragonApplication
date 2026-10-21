package com.raczkowski.app.payment.service;

import com.raczkowski.app.accountPremium.entity.PlanPrice;
import com.raczkowski.app.accountPremium.entity.SubscriptionPlan;
import com.raczkowski.app.accountPremium.repository.PlanPriceRepository;
import com.raczkowski.app.accountPremium.repository.SubscriptionPlanRepository;
import com.raczkowski.app.accountPremium.repository.SubscriptionRepository;
import com.raczkowski.app.accountPremium.service.SubscriptionService;
import com.raczkowski.app.enums.CurrencyCode;
import com.raczkowski.app.enums.PremiumAccountRange;
import com.raczkowski.app.exceptions.ErrorMessages;
import com.raczkowski.app.exceptions.ResponseException;
import com.raczkowski.app.payment.entity.PaymentRecord;
import com.raczkowski.app.payment.entity.PaymentStatus;
import com.raczkowski.app.payment.exception.InvalidStripeSignatureException;
import com.raczkowski.app.payment.repository.PaymentRecordRepository;
import com.raczkowski.app.user.AppUser;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StripeService {
    private final PlanPriceRepository planPriceRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final SubscriptionService subscriptionService;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    @Value("${stripe.success-url}")
    private String successUrl;

    @Value("${stripe.cancel-url}")
    private String cancelUrl;

    @Transactional
    public String createCheckoutSession(AppUser user, PremiumAccountRange planType, CurrencyCode currency) {
        SubscriptionPlan plan = subscriptionPlanRepository.getSubscriptionPlanBySubscriptionType(planType)
                .orElseThrow(() -> new ResponseException(ErrorMessages.SUBSCRIPTION_NOT_FOUND));

        PlanPrice planPrice = planPriceRepository.lockByPlanAndCurrency(plan.getId(), currency)
                .orElseThrow(() -> new ResponseException(ErrorMessages.PLAN_NOT_FOUND));

        long amountInSmallestUnit = planPrice.getAmount()
                .multiply(BigDecimal.valueOf(100))
                .longValue();

        try {
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(cancelUrl)
                    .addLineItem(SessionCreateParams.LineItem.builder()
                            .setQuantity(1L)
                            .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency(currency.name().toLowerCase())
                                    .setUnitAmount(amountInSmallestUnit)
                                    .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                            .setName("Premium - " + planType.name())
                                            .build())
                                    .build())
                            .build())
                    .putMetadata("userId", String.valueOf(user.getId()))
                    .putMetadata("planType", planType.name())
                    .putMetadata("currency", currency.name())
                    .build();

            Session session = Session.create(params);

            paymentRecordRepository.save(new PaymentRecord(
                    user.getId(),
                    session.getId(),
                    planType,
                    currency,
                    planPrice.getAmount(),
                    PaymentStatus.PENDING,
                    ZonedDateTime.now(ZoneOffset.UTC)
            ));

            return session.getUrl();
        } catch (com.stripe.exception.StripeException e) {
            throw new ResponseException("Failed to create checkout session: " + e.getMessage());
        }
    }

    public void handleWebhookEvent(String payload, String sigHeader) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            throw new InvalidStripeSignatureException();
        }

        if (!"checkout.session.completed".equals(event.getType())) {
            return;
        }

        Optional<StripeObject> stripeObject = event.getDataObjectDeserializer().getObject();
        if (stripeObject.isEmpty()) {
            return;
        }
        Session session = (Session) stripeObject.get();

        Optional<PaymentRecord> recordOpt = paymentRecordRepository.findByStripeSessionId(session.getId());
        if (recordOpt.isPresent() && recordOpt.get().getStatus() == PaymentStatus.COMPLETED) {
            return;
        }

        Long userId = Long.valueOf(session.getMetadata().get("userId"));
        PremiumAccountRange planType = PremiumAccountRange.valueOf(session.getMetadata().get("planType"));
        CurrencyCode currency = CurrencyCode.valueOf(session.getMetadata().get("currency"));

        if (recordOpt.isPresent()) {
            PaymentRecord record = recordOpt.get();
            record.setStatus(PaymentStatus.COMPLETED);
            paymentRecordRepository.save(record);
        } else {
            BigDecimal amount = session.getAmountTotal() != null
                    ? BigDecimal.valueOf(session.getAmountTotal()).movePointLeft(2)
                    : BigDecimal.ZERO;
            paymentRecordRepository.save(new PaymentRecord(
                    userId, session.getId(), planType, currency,
                    amount, PaymentStatus.COMPLETED, ZonedDateTime.now(ZoneOffset.UTC)
            ));
        }

        if (subscriptionRepository.findByUserId(userId).isEmpty()) {
            subscriptionService.createSubscriptionForUser(userId, planType);
        }

        subscriptionRepository.findByUserId(userId)
                .filter(s -> !s.isActive())
                .ifPresent(s -> subscriptionService.activateSubscription(userId));
    }
}