package com.raczkowski.app.accountPremium.entity;

import com.raczkowski.app.enums.CurrencyCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ChangePriceRequest {
        private Long subscriptionPlan;
        private CurrencyCode currency;
        @Min(1)
        private Long newAmount;
        private String reason;
}