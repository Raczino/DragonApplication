package com.raczkowski.app.payment.entity;

import com.raczkowski.app.enums.CurrencyCode;
import com.raczkowski.app.enums.PremiumAccountRange;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
@Table(name = "payment_record")
@Getter
@Setter
@NoArgsConstructor
public class PaymentRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "stripe_session_id", nullable = false, unique = true)
    private String stripeSessionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type", nullable = false)
    private PremiumAccountRange planType;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false, length = 3)
    private CurrencyCode currency;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    public PaymentRecord(Long userId, String stripeSessionId, PremiumAccountRange planType,
                         CurrencyCode currency, BigDecimal amount, PaymentStatus status,
                         ZonedDateTime createdAt) {
        this.userId = userId;
        this.stripeSessionId = stripeSessionId;
        this.planType = planType;
        this.currency = currency;
        this.amount = amount;
        this.status = status;
        this.createdAt = createdAt;
    }
}