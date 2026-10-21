package com.raczkowski.app.payment.controller;

import com.raczkowski.app.payment.dto.CheckoutRequest;
import com.raczkowski.app.payment.dto.CheckoutResponse;
import com.raczkowski.app.payment.service.StripeService;
import com.raczkowski.app.user.AppUser;
import com.raczkowski.app.user.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/payment")
public class PaymentController {
    private final StripeService stripeService;
    private final UserService userService;

    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponse> createCheckoutSession(@RequestBody CheckoutRequest request) {
        AppUser user = userService.getLoggedUser();
        String checkoutUrl = stripeService.createCheckoutSession(user, request.getPlanType(), request.getCurrency());
        return ResponseEntity.ok(new CheckoutResponse(checkoutUrl));
    }
}