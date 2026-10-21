package com.raczkowski.app.payment.exception;

public class InvalidStripeSignatureException extends RuntimeException {
    public InvalidStripeSignatureException() {
        super("Invalid Stripe webhook signature");
    }
}