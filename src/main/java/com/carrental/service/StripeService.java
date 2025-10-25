package com.carrental.service;

import com.stripe.model.checkout.Session;
import java.math.BigDecimal;

public interface StripeService {
    Session createCheckoutSession(BigDecimal amountToPay);
}
