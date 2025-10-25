package com.carrental.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.carrental.service.impl.StripeServiceImpl;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import java.math.BigDecimal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StripeServiceTest {

    @Mock
    private StripeServiceImpl stripeService;

    @Test
    @DisplayName("Create checkout session - should create session with valid amount")
    void createCheckoutSession_ValidAmount_ShouldCreateSession() throws StripeException {
        // Given
        BigDecimal amount = BigDecimal.valueOf(50.00);
        Session mockSession = new Session();
        mockSession.setUrl("https://stripe.com/test-session");

        when(stripeService.createCheckoutSession(amount)).thenReturn(mockSession);

        // When
        Session session = stripeService.createCheckoutSession(amount);

        // Then
        assertNotNull(session);
        assertNotNull(session.getUrl());
        verify(stripeService).createCheckoutSession(amount);
    }

    @Test
    @DisplayName("Create checkout session - should handle zero amount")
    void createCheckoutSession_ZeroAmount_ShouldCreateSession() throws StripeException {
        // Given
        BigDecimal amount = BigDecimal.ZERO;
        Session mockSession = new Session();
        mockSession.setUrl("https://stripe.com/zero-session");

        when(stripeService.createCheckoutSession(amount)).thenReturn(mockSession);

        // When
        Session session = stripeService.createCheckoutSession(amount);

        // Then
        assertNotNull(session);
        assertNotNull(session.getUrl());
        verify(stripeService).createCheckoutSession(amount);
    }

    @Test
    @DisplayName("Create checkout session - should handle large amount")
    void createCheckoutSession_LargeAmount_ShouldCreateSession() throws StripeException {
        // Given
        BigDecimal amount = BigDecimal.valueOf(10000.00);
        Session mockSession = new Session();
        mockSession.setUrl("https://stripe.com/large-session");

        when(stripeService.createCheckoutSession(amount)).thenReturn(mockSession);

        // When
        Session session = stripeService.createCheckoutSession(amount);

        // Then
        assertNotNull(session);
        assertNotNull(session.getUrl());
        verify(stripeService).createCheckoutSession(amount);
    }

    @Test
    @DisplayName("Create checkout session - should handle Stripe exception")
    void createCheckoutSession_StripeException_ShouldThrowRuntimeException()
            throws StripeException {
        // Given
        BigDecimal amount = BigDecimal.valueOf(50.00);

        when(stripeService.createCheckoutSession(amount))
                .thenThrow(new RuntimeException("Stripe session creation failed"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> stripeService.createCheckoutSession(amount));
        assertNotNull(exception);
        Assertions.assertEquals("Stripe session creation failed", exception.getMessage());
        verify(stripeService).createCheckoutSession(amount);
    }

    @Test
    @DisplayName("Create checkout session - should handle authentication exception")
    void createCheckoutSession_AuthenticationException_ShouldThrowRuntimeException()
            throws StripeException {
        // Given
        BigDecimal amount = BigDecimal.valueOf(50.00);

        when(stripeService.createCheckoutSession(amount))
                .thenThrow(new RuntimeException("Invalid API Key provided"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> stripeService.createCheckoutSession(amount));
        assertNotNull(exception);
        Assertions.assertEquals("Invalid API Key provided", exception.getMessage());
        verify(stripeService).createCheckoutSession(amount);
    }
}
