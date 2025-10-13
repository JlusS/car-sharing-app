package com.carrental.carrent.service.impl;

import com.carrental.carrent.dto.car.CarDto;
import com.carrental.carrent.dto.payment.PaymentDto;
import com.carrental.carrent.dto.payment.PaymentRequestDto;
import com.carrental.carrent.mapper.PaymentMapper;
import com.carrental.carrent.model.Payment;
import com.carrental.carrent.model.PaymentType;
import com.carrental.carrent.model.Rental;
import com.carrental.carrent.model.Status;
import com.carrental.carrent.repository.payment.PaymentRepository;
import com.carrental.carrent.repository.rental.RentalRepository;
import com.carrental.carrent.service.CarService;
import com.carrental.carrent.service.PaymentService;
import com.carrental.carrent.service.StripeService;
import com.stripe.model.checkout.Session;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final StripeService stripeService;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final RentalRepository rentalRepository;
    private final CarService carService;

    @Override
    public String createPayment(PaymentRequestDto request) {
        Rental rental = rentalRepository.findById(request.getRentalId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Rental not found with id: " + request.getRentalId()));

        CarDto car = carService.findById(rental.getCarId());

        BigDecimal amountToPay = calculatePaymentAmount(rental, car);

        Session session = stripeService.createCheckoutSession(amountToPay);

        Payment payment = paymentMapper.toEntityWithSession(request, session, amountToPay);

        paymentRepository.save(payment);
        return session.getUrl();
    }

    private BigDecimal calculatePaymentAmount(Rental rental, CarDto car) {
        if (rental.getRentalDate() != null && rental.getReturnDate() != null) {
            long daysBetween = ChronoUnit.DAYS.between(
                    rental.getRentalDate(), rental.getReturnDate());
            return car.getDailyFee().multiply(BigDecimal.valueOf(Math.max(1, daysBetween)));
        } else {
            return car.getDailyFee();
        }
    }

    @Override
    public void markPaymentSuccessful(String sessionId) {
        Payment payment = paymentRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        payment.setStatus(Status.PAID);
        paymentRepository.save(payment);
    }

    @Override
    public List<PaymentDto> getPayments(Long rentalId) {
        return paymentRepository.findByRentalId(rentalId)
                .stream()
                .map(paymentMapper::toDto)
                .toList();
    }

    @Override
    public List<PaymentDto> getAllActivePayments() {
        return paymentRepository.findAll()
                .stream()
                .filter(payment -> payment.getStatus() != Status.PAID)
                .map(paymentMapper::toDto)
                .toList();
    }

    @Scheduled(fixedRate = 300000)
    @Transactional
    public void checkExpiredPayments() {
        List<Payment> expiredPayments = paymentRepository.findExpiredPendingPayments();

        for (Payment payment : expiredPayments) {
            payment.setStatus(Status.EXPIRED);
            paymentRepository.save(payment);
        }
    }

    @Scheduled(fixedRate = 300000)
    @Transactional
    public void checkOverdueRentals() {
        List<Rental> overdueRentals = rentalRepository.findOverdueActiveRentals(LocalDate.now());

        for (Rental rental : overdueRentals) {
            applyOverdueFine(rental);
        }
    }

    private void applyOverdueFine(Rental rental) {
        CarDto car = carService.findById(rental.getCarId());

        long overdueDays = ChronoUnit.DAYS.between(rental.getReturnDate(), LocalDate.now());

        if (overdueDays > 0) {
            BigDecimal dailyFine = car.getDailyFee();
            BigDecimal fineAmount = dailyFine.multiply(BigDecimal.valueOf(overdueDays));
            createFinePayment(rental, fineAmount);
        }
    }

    private void createFinePayment(Rental rental, BigDecimal fineAmount) {
        Payment finePayment = new Payment();
        finePayment.setRentalId(rental.getId());
        finePayment.setAmountToPay(fineAmount);
        finePayment.setPaymentType(PaymentType.FINE);
        finePayment.setStatus(Status.PENDING);
        finePayment.setDeleted(false);

        finePayment.setSessionId("FINE_" + rental.getId() + "_" + System.currentTimeMillis());
        finePayment.setSessionUrl("fine_payment_no_url");

        paymentRepository.save(finePayment);
    }
}

