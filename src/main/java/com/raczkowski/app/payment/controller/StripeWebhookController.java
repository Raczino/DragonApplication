package com.raczkowski.app.payment.controller;

import com.raczkowski.app.payment.exception.InvalidStripeSignatureException;
import com.raczkowski.app.payment.service.StripeService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/stripe")
public class StripeWebhookController {
    private final StripeService stripeService;

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            stripeService.handleWebhookEvent(payload, sigHeader);
            return ResponseEntity.ok().build();
        } catch (InvalidStripeSignatureException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}