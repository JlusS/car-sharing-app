package com.carrental.carrent.repository.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.carrental.carrent.model.Payment;
import com.carrental.carrent.model.PaymentType;
import com.carrental.carrent.model.Status;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    @DisplayName("Find by session ID - should return payment when session exists")
    void findBySessionId_ExistingSession_ShouldReturnPayment() {
        // Given
        Payment payment = new Payment();
        payment.setSessionId("test_session_123");
        payment.setRentalId(1L);
        payment.setAmountToPay(BigDecimal.valueOf(100.00));
        payment.setStatus(Status.PENDING);
        payment.setPaymentType(PaymentType.PAYMENT);
        payment.setSessionUrl("https://stripe.com/test");
        paymentRepository.save(payment);

        // When
        Optional<Payment> foundPayment = paymentRepository.findBySessionId("test_session_123");

        // Then
        assertTrue(foundPayment.isPresent());
        assertEquals("test_session_123", foundPayment.get().getSessionId());
        assertEquals(Status.PENDING, foundPayment.get().getStatus());
    }

    @Test
    @DisplayName("Find by session ID - should return empty when session not exists")
    void findBySessionId_NonExistingSession_ShouldReturnEmpty() {
        // When
        Optional<Payment> foundPayment = paymentRepository.findBySessionId("non_existing_session");

        // Then
        assertTrue(foundPayment.isEmpty());
    }

    @Test
    @DisplayName("Find by rental ID - should return all payments for rental")
    void findByRentalId_ExistingRental_ShouldReturnPayments() {
        // Given
        Long rentalId = 1L;

        Payment payment1 = new Payment();
        payment1.setSessionId("session_1");
        payment1.setRentalId(rentalId);
        payment1.setAmountToPay(BigDecimal.valueOf(100.00));
        payment1.setStatus(Status.PENDING);
        payment1.setPaymentType(PaymentType.PAYMENT);
        payment1.setSessionUrl("https://stripe.com/test1");

        Payment payment2 = new Payment();
        payment2.setSessionId("session_2");
        payment2.setRentalId(rentalId);
        payment2.setAmountToPay(BigDecimal.valueOf(50.00));
        payment2.setStatus(Status.PAID);
        payment2.setPaymentType(PaymentType.FINE);
        payment2.setSessionUrl("https://stripe.com/test2");

        paymentRepository.save(payment1);
        paymentRepository.save(payment2);

        // When
        List<Payment> payments = paymentRepository.findByRentalId(rentalId);

        // Then
        assertEquals(2, payments.size());
        assertTrue(payments.stream().allMatch(p -> p.getRentalId().equals(rentalId)));
    }

    @Test
    @DisplayName("Find by rental ID - should return empty list when no payments for rental")
    void findByRentalId_NonExistingRental_ShouldReturnEmptyList() {
        // When
        List<Payment> payments = paymentRepository.findByRentalId(999L);

        // Then
        assertTrue(payments.isEmpty());
    }

    @Test
    @DisplayName("Find expired pending payments - should return only pending payments")
    void findExpiredPendingPayments_WithPendingPayments_ShouldReturnPendingOnly() {
        // Given
        Payment pendingPayment = new Payment();
        pendingPayment.setSessionId("pending_session");
        pendingPayment.setRentalId(1L);
        pendingPayment.setAmountToPay(BigDecimal.valueOf(100.00));
        pendingPayment.setStatus(Status.PENDING);
        pendingPayment.setPaymentType(PaymentType.PAYMENT);
        pendingPayment.setSessionUrl("https://stripe.com/pending");

        Payment paidPayment = new Payment();
        paidPayment.setSessionId("paid_session");
        paidPayment.setRentalId(2L);
        paidPayment.setAmountToPay(BigDecimal.valueOf(150.00));
        paidPayment.setStatus(Status.PAID);
        paidPayment.setPaymentType(PaymentType.PAYMENT);
        paidPayment.setSessionUrl("https://stripe.com/paid");

        paymentRepository.save(pendingPayment);
        paymentRepository.save(paidPayment);

        // When
        List<Payment> expiredPayments = paymentRepository.findExpiredPendingPayments();

        // Then
        assertEquals(1, expiredPayments.size());
        assertEquals(Status.PENDING, expiredPayments.get(0).getStatus());
        assertEquals("pending_session", expiredPayments.get(0).getSessionId());
    }

    @Test
    @DisplayName("Save payment - should save and return payment with generated ID")
    void save_ValidPayment_ShouldSavePayment() {
        // Given
        Payment payment = new Payment();
        payment.setSessionId("new_session_123");
        payment.setRentalId(1L);
        payment.setAmountToPay(BigDecimal.valueOf(75.50));
        payment.setStatus(Status.PENDING);
        payment.setPaymentType(PaymentType.PAYMENT);
        payment.setSessionUrl("https://stripe.com/new");

        // When
        Payment savedPayment = paymentRepository.save(payment);

        // Then
        assertNotNull(savedPayment);
        assertNotNull(savedPayment.getId());
        assertEquals("new_session_123", savedPayment.getSessionId());
        assertEquals(BigDecimal.valueOf(75.50), savedPayment.getAmountToPay());
        assertEquals(Status.PENDING, savedPayment.getStatus());
    }

    @Test
    @DisplayName("Update payment status - should update payment details")
    void updatePayment_ExistingPayment_ShouldUpdateStatus() {
        // Given
        Payment payment = new Payment();
        payment.setSessionId("update_session");
        payment.setRentalId(1L);
        payment.setAmountToPay(BigDecimal.valueOf(100.00));
        payment.setStatus(Status.PENDING);
        payment.setPaymentType(PaymentType.PAYMENT);
        payment.setSessionUrl("https://stripe.com/update");
        Payment savedPayment = paymentRepository.save(payment);

        // When
        savedPayment.setStatus(Status.PAID);
        Payment updatedPayment = paymentRepository.save(savedPayment);

        // Then
        assertEquals(savedPayment.getId(), updatedPayment.getId());
        assertEquals(Status.PAID, updatedPayment.getStatus());
    }
}
