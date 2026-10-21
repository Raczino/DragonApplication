package com.raczkowski.app.payment.dto;

import com.raczkowski.app.enums.CurrencyCode;
import com.raczkowski.app.enums.PremiumAccountRange;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckoutRequest {
    private PremiumAccountRange planType;
    private CurrencyCode currency;
}