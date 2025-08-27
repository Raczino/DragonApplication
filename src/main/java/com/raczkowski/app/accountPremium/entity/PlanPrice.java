package com.raczkowski.app.accountPremium.entity;

import com.raczkowski.app.enums.CurrencyCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
@Table(
        name = "plan_price",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_plan_price_plan_currency", columnNames = {"subscription_plan_id", "currency"})
        }
)
@Getter
@Setter
@NoArgsConstructor
public class PlanPrice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_plan_id", nullable = false)
    private SubscriptionPlan subscriptionPlan;


    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false, length = 3)
    private CurrencyCode currency;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    public PlanPrice(SubscriptionPlan subscriptionPlan, CurrencyCode currency, BigDecimal amount, ZonedDateTime updatedAt, ZonedDateTime createdAt) {
        this.subscriptionPlan = subscriptionPlan;
        this.currency = currency;
        this.amount = amount;
        this.updatedAt = updatedAt;
        this.createdAt = createdAt;
    }
}
