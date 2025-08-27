package com.raczkowski.app.dto;

import com.raczkowski.app.enums.CurrencyCode;
import lombok.*;

import java.time.ZonedDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlanPriceDto {
    private Long amount;
    private CurrencyCode currency;
    private ZonedDateTime updatedAt;
    private ZonedDateTime createdAt;
    private Long previousPrice;
    private ZonedDateTime changedAt;
}