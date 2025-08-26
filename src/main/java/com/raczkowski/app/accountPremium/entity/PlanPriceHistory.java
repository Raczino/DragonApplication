package com.raczkowski.app.accountPremium.entity;

import com.raczkowski.app.enums.CurrencyCode;
import com.raczkowski.app.user.AppUser;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "plan_price_history",
        indexes = {
                @Index(name = "idx_pph_plan_currency_changed", columnList = "subscription_plan_id,currency,created_at")
        })
@Getter
@Setter
@NoArgsConstructor
public class PlanPriceHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_plan_id", nullable = false)
    private SubscriptionPlan subscriptionPlan;


    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false, length = 3)
    private CurrencyCode currency;


    @Column(name = "old_amount")
    private Long oldAmount;

    @Column(name = "new_amount", nullable = false)
    private Long newAmount;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "changed_by")
    private AppUser changedBy;


    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    public PlanPriceHistory(SubscriptionPlan subscriptionPlan,
                            CurrencyCode currency,
                            Long oldAmount,
                            Long newAmount,
                            ZonedDateTime createdAt,
                            AppUser changedBy,
                            String reason) {
        this.subscriptionPlan = subscriptionPlan;
        this.currency = currency;
        this.oldAmount = oldAmount;
        this.newAmount = newAmount;
        this.createdAt = createdAt;
        this.changedBy = changedBy;
        this.reason = reason;
    }
}
