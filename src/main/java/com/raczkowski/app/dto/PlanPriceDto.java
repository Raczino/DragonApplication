package com.raczkowski.app.dto;

import com.raczkowski.app.enums.CurrencyCode;
import lombok.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlanPriceDto {
    private BigDecimal amount;
    private CurrencyCode currency;
    private ZonedDateTime updatedAt;
    private ZonedDateTime createdAt;
    private BigDecimal previousPrice;
    private ZonedDateTime changedAt;
}