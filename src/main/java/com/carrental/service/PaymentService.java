package com.carrental.service;

import com.carrental.dto.payment.PaymentDto;
import com.carrental.dto.payment.PaymentRequestDto;
import java.util.List;

public interface PaymentService {
    String createPayment(PaymentRequestDto request);

    void markPaymentSuccessful(String sessionId);

    List<PaymentDto> getPayments(Long rentalId);

    List<PaymentDto> getAllActivePayments();
}

