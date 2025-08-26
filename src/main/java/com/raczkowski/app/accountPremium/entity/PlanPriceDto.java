package com.raczkowski.app.accountPremium.entity;

import com.raczkowski.app.enums.CurrencyCode;
import com.sun.istack.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import java.time.ZonedDateTime;

public class PlanPriceDto {

    @Data
    @NoArgsConstructor
    public static class ChangePriceRequest {
        private Long subscriptionPlan;
        private CurrencyCode currency;

        @Min(1)
        private Long newAmount;

        private String reason;
    }

    @Data
    public static class PriceResponse {
        private Long planId;
        private CurrencyCode currency;
        private Long amount;
        private ZonedDateTime updatedAt;
    }

    @Data
    public static class PriceHistoryItem {
        private Long planId;
        private CurrencyCode currency;
        private Long oldAmount;
        private Long newAmount;
        private ZonedDateTime changedAt;
        private String changedBy;
        private String reason;
    }
}
