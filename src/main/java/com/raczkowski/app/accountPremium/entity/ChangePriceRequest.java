package com.raczkowski.app.accountPremium.entity;

import com.raczkowski.app.enums.CurrencyCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Min;
import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ChangePriceRequest {
        private Long subscriptionPlan;
        private CurrencyCode currency;
        @Min(1)
        private BigDecimal newAmount;
        private String reason;
}