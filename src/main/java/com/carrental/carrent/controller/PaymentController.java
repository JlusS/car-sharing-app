package com.carrental.carrent.controller;

import com.carrental.carrent.dto.payment.PaymentDto;
import com.carrental.carrent.dto.payment.PaymentRequestDto;
import com.carrental.carrent.service.PaymentService;
import com.carrental.carrent.service.UserService;
import com.carrental.carrent.service.telegram.TelegramNotificationService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    private final UserService userService;
    private final TelegramNotificationService telegramNotificationService;

    @GetMapping
    public ResponseEntity<List<PaymentDto>> getPayments(@RequestParam("rental_id") Long rentalId) {
        List<PaymentDto> payments = paymentService.getPayments(rentalId);
        return ResponseEntity.ok(payments);
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createPayment(
            @Valid @RequestBody PaymentRequestDto request) {
        String sessionUrl = paymentService.createPayment(request);
        return ResponseEntity.ok(Map.of("url", sessionUrl));
    }

    @GetMapping("/success")
    public ResponseEntity<String> handleSuccess(@RequestParam("session_id") String sessionId) {
        paymentService.markPaymentSuccessful(sessionId);
        telegramNotificationService.sendSuccessfulPaymentNotification(sessionId);
        return ResponseEntity.ok("Payment successful");
    }

    @GetMapping("/cancel")
    public ResponseEntity<String> handleCancel() {
        return ResponseEntity.ok("Payment was cancelled or paused");
    }
}

