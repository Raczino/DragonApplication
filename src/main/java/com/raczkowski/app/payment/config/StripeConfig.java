package com.raczkowski.app.payment.config;

import com.stripe.Stripe;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StripeConfig implements InitializingBean {

    @Value("${stripe.secret-key}")
    private String secretKey;

    @Override
    public void afterPropertiesSet() {
        Stripe.apiKey = secretKey;
    }
}