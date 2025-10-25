package com.carrental.controller;

import com.carrental.dto.payment.PaymentDto;
import com.carrental.dto.payment.PaymentRequestDto;
import com.carrental.service.PaymentService;
import com.carrental.service.telegram.TelegramNotificationService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    private final TelegramNotificationService telegramNotificationService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    public ResponseEntity<List<PaymentDto>> getPayments(@RequestParam("rental_id") Long rentalId) {
        List<PaymentDto> payments = paymentService.getPayments(rentalId);
        return ResponseEntity.ok(payments);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    public ResponseEntity<Map<String, String>> createPayment(
            @Valid @RequestBody PaymentRequestDto request) {
        String sessionUrl = paymentService.createPayment(request);
        return ResponseEntity.ok(Map.of("url", sessionUrl));
    }

    @GetMapping("/success")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    public ResponseEntity<String> handleSuccess(@RequestParam("session_id") String sessionId) {
        paymentService.markPaymentSuccessful(sessionId);
        telegramNotificationService.sendSuccessfulPaymentNotification(sessionId);
        return ResponseEntity.ok("Payment successful");
    }

    @GetMapping("/cancel")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    public ResponseEntity<String> handleCancel() {
        return ResponseEntity.ok("Payment was cancelled or paused");
    }
}

