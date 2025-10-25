package com.carrental.service.impl;

import com.carrental.service.StripeService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StripeServiceImpl implements StripeService {
    @Value("${stripe.secret.key}")
    private String secretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }

    @Override
    public Session createCheckoutSession(BigDecimal amountToPay) {
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:8080/api/payments/success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl("http://localhost:8080/api/payments/cancel")
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("usd")
                                                .setUnitAmount(amountToPay.multiply(
                                                        BigDecimal.valueOf(100)).longValue())
                                                .setProductData(
                                                        SessionCreateParams
                                                                .LineItem.PriceData
                                                                .ProductData
                                                                .builder()
                                                                .setName("Car rental")
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .build();

        try {
            return Session.create(params);
        } catch (StripeException e) {
            throw new RuntimeException("Stripe session creation failed", e);
        }
    }
}

