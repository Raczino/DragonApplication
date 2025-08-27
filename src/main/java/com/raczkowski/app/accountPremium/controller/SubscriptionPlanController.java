package com.raczkowski.app.accountPremium.controller;

import com.raczkowski.app.accountPremium.service.SubscriptionService;
import com.raczkowski.app.dto.SubscriptionPlanDto;
import com.raczkowski.app.enums.CurrencyCode;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/plan")
public class SubscriptionPlanController {
    private final SubscriptionService subscriptionService;

    @GetMapping("/all")
    ResponseEntity<List<SubscriptionPlanDto>> getAllSubscriptionPlans(@RequestParam CurrencyCode currencyCode) {
        return ResponseEntity.ok(subscriptionService.getAllSubscriptionPlans(currencyCode));
    }
}