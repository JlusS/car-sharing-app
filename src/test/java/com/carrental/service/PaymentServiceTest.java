package com.carrental.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.carrental.dto.car.CarDto;
import com.carrental.dto.payment.PaymentDto;
import com.carrental.dto.payment.PaymentRequestDto;
import com.carrental.mapper.PaymentMapper;
import com.carrental.model.Payment;
import com.carrental.model.PaymentType;
import com.carrental.model.Rental;
import com.carrental.model.Status;
import com.carrental.repository.payment.PaymentRepository;
import com.carrental.repository.rental.RentalRepository;
import com.carrental.service.impl.PaymentServiceImpl;
import com.stripe.model.checkout.Session;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private StripeService stripeService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private CarService carService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    @DisplayName("Create payment - should create payment session and return URL")
    void createPayment_ValidRequest_ShouldReturnSessionUrl() throws Exception {
        // Given
        PaymentRequestDto requestDto = new PaymentRequestDto();
        requestDto.setRentalId(1L);
        requestDto.setPaymentType(PaymentType.PAYMENT);

        Rental rental = new Rental();
        rental.setId(1L);
        rental.setCarId(1L);
        rental.setRentalDate(LocalDate.now().minusDays(2));
        rental.setReturnDate(LocalDate.now().plusDays(5));

        CarDto carDto = new CarDto();
        carDto.setId(1L);
        carDto.setDailyFee(BigDecimal.valueOf(50.00));

        Session session = new Session();
        session.setUrl("https://stripe.com/test-session");

        Payment payment = new Payment();
        payment.setId(1L);
        payment.setRentalId(1L);
        payment.setAmountToPay(BigDecimal.valueOf(350.00));
        payment.setPaymentType(PaymentType.PAYMENT);
        payment.setStatus(Status.PENDING);

        when(rentalRepository.findById(1L)).thenReturn(Optional.of(rental));
        when(carService.findById(1L)).thenReturn(carDto);
        when(stripeService.createCheckoutSession(any(BigDecimal.class))).thenReturn(session);
        when(paymentMapper.toEntityWithSession(any(), any(), any())).thenReturn(payment);
        when(paymentRepository.save(payment)).thenReturn(payment);

        // When
        String result = paymentService.createPayment(requestDto);

        // Then
        assertNotNull(result);
        assertEquals("https://stripe.com/test-session", result);
        verify(rentalRepository).findById(1L);
        verify(carService).findById(1L);
        verify(stripeService).createCheckoutSession(any(BigDecimal.class));
        verify(paymentMapper).toEntityWithSession(any(), any(), any());
        verify(paymentRepository).save(payment);
    }

    @Test
    @DisplayName("Create payment - should throw exception when rental not found")
    void createPayment_NonExistingRental_ShouldThrowException() {
        // Given
        PaymentRequestDto requestDto = new PaymentRequestDto();
        requestDto.setRentalId(999L);

        when(rentalRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> paymentService.createPayment(requestDto));
        assertEquals("Rental not found with id: 999", exception.getMessage());
        verify(rentalRepository).findById(999L);
    }

    @Test
    @DisplayName("Create payment - should handle Stripe service exception")
    void createPayment_StripeServiceException_ShouldThrowException() throws Exception {
        // Given
        PaymentRequestDto requestDto = new PaymentRequestDto();
        requestDto.setRentalId(1L);
        requestDto.setPaymentType(PaymentType.PAYMENT);

        Rental rental = new Rental();
        rental.setId(1L);
        rental.setCarId(1L);
        rental.setRentalDate(LocalDate.now().minusDays(2));
        rental.setReturnDate(LocalDate.now().plusDays(5));

        CarDto carDto = new CarDto();
        carDto.setId(1L);
        carDto.setDailyFee(BigDecimal.valueOf(50.00));

        when(rentalRepository.findById(1L)).thenReturn(Optional.of(rental));
        when(carService.findById(1L)).thenReturn(carDto);
        when(stripeService.createCheckoutSession(any(BigDecimal.class)))
                .thenThrow(new RuntimeException("Stripe error"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> paymentService.createPayment(requestDto));
        assertNotNull(exception);
        assertEquals("Stripe error", exception.getMessage());
        verify(rentalRepository).findById(1L);
        verify(carService).findById(1L);
        verify(stripeService).createCheckoutSession(any(BigDecimal.class));
    }

    @Test
    @DisplayName("Mark payment successful - should update payment status to PAID")
    void markPaymentSuccessful_ValidSessionId_ShouldUpdateStatus() {
        // Given
        String sessionId = "test_session_123";

        Payment payment = new Payment();
        payment.setId(1L);
        payment.setSessionId(sessionId);
        payment.setStatus(Status.PENDING);

        when(paymentRepository.findBySessionId(sessionId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(payment)).thenReturn(payment);

        // When
        paymentService.markPaymentSuccessful(sessionId);

        // Then
        assertEquals(Status.PAID, payment.getStatus());
        verify(paymentRepository).findBySessionId(sessionId);
        verify(paymentRepository).save(payment);
    }

    @Test
    @DisplayName("Mark payment successful - should throw exception when payment not found")
    void markPaymentSuccessful_NonExistingSessionId_ShouldThrowException() {
        // Given
        String sessionId = "non_existing_session";

        when(paymentRepository.findBySessionId(sessionId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> paymentService.markPaymentSuccessful(sessionId));
        assertEquals("Payment not found", exception.getMessage());
        verify(paymentRepository).findBySessionId(sessionId);
    }

    @Test
    @DisplayName("Get payments by rental ID - should return list of payment DTOs")
    void getPayments_ExistingRentalId_ShouldReturnPaymentDtos() {
        // Given
        Long rentalId = 1L;

        Payment payment1 = new Payment();
        payment1.setId(1L);
        payment1.setRentalId(rentalId);
        payment1.setAmountToPay(BigDecimal.valueOf(100.00));
        payment1.setPaymentType(PaymentType.PAYMENT);
        payment1.setStatus(Status.PAID);

        Payment payment2 = new Payment();
        payment2.setId(2L);
        payment2.setRentalId(rentalId);
        payment2.setAmountToPay(BigDecimal.valueOf(50.00));
        payment2.setPaymentType(PaymentType.FINE);
        payment2.setStatus(Status.PENDING);

        PaymentDto paymentDto1 = new PaymentDto();
        paymentDto1.setId(1L);
        paymentDto1.setRentalId(rentalId);
        paymentDto1.setAmountToPay(BigDecimal.valueOf(100.00));

        PaymentDto paymentDto2 = new PaymentDto();
        paymentDto2.setId(2L);
        paymentDto2.setRentalId(rentalId);
        paymentDto2.setAmountToPay(BigDecimal.valueOf(50.00));

        when(paymentRepository.findByRentalId(rentalId)).thenReturn(List.of(payment1, payment2));
        when(paymentMapper.toDto(payment1)).thenReturn(paymentDto1);
        when(paymentMapper.toDto(payment2)).thenReturn(paymentDto2);

        // When
        List<PaymentDto> result = paymentService.getPayments(rentalId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(paymentRepository).findByRentalId(rentalId);
        verify(paymentMapper).toDto(payment1);
        verify(paymentMapper).toDto(payment2);
    }

    @Test
    @DisplayName("Get all active payments - should return list of non-PAID payment DTOs")
    void getAllActivePayments_WithMixedStatuses_ShouldReturnNonPaidPayments() {
        // Given
        Payment pendingPayment = new Payment();
        pendingPayment.setId(1L);
        pendingPayment.setStatus(Status.PENDING);

        Payment expiredPayment = new Payment();
        expiredPayment.setId(2L);
        expiredPayment.setStatus(Status.EXPIRED);

        Payment paidPayment = new Payment();
        paidPayment.setId(3L);
        paidPayment.setStatus(Status.PAID);

        PaymentDto pendingDto = new PaymentDto();
        pendingDto.setId(1L);
        pendingDto.setStatus(Status.PENDING);

        PaymentDto expiredDto = new PaymentDto();
        expiredDto.setId(2L);
        expiredDto.setStatus(Status.EXPIRED);

        when(paymentRepository.findAll())
                .thenReturn(List.of(pendingPayment, expiredPayment, paidPayment));
        when(paymentMapper.toDto(pendingPayment)).thenReturn(pendingDto);
        when(paymentMapper.toDto(expiredPayment)).thenReturn(expiredDto);

        // When
        List<PaymentDto> result = paymentService.getAllActivePayments();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(paymentRepository).findAll();
        verify(paymentMapper).toDto(pendingPayment);
        verify(paymentMapper).toDto(expiredPayment);
    }

    @Test
    @DisplayName("Check expired payments - should update expired payments status")
    void checkExpiredPayments_WithExpiredPayments_ShouldUpdateStatus() {
        // Given
        Payment expiredPayment = new Payment();
        expiredPayment.setId(1L);
        expiredPayment.setStatus(Status.PENDING);

        when(paymentRepository.findExpiredPendingPayments()).thenReturn(List.of(expiredPayment));
        when(paymentRepository.save(expiredPayment)).thenReturn(expiredPayment);

        // When
        paymentService.checkExpiredPayments();

        // Then
        assertEquals(Status.EXPIRED, expiredPayment.getStatus());
        verify(paymentRepository).findExpiredPendingPayments();
        verify(paymentRepository).save(expiredPayment);
    }

    @Test
    @DisplayName("Check overdue rentals - should create fine payments for overdue rentals")
    void checkOverdueRentals_WithOverdueRentals_ShouldCreateFinePayments() {
        // Given
        Rental overdueRental = new Rental();
        overdueRental.setId(1L);
        overdueRental.setCarId(1L);
        overdueRental.setRentalDate(LocalDate.now().minusDays(10));
        overdueRental.setReturnDate(LocalDate.now().minusDays(3));
        overdueRental.setActualReturnDate(null);

        CarDto carDto = new CarDto();
        carDto.setId(1L);
        carDto.setDailyFee(BigDecimal.valueOf(50.00));

        when(rentalRepository.findOverdueActiveRentals(any())).thenReturn(List.of(overdueRental));
        when(carService.findById(1L)).thenReturn(carDto);
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        paymentService.checkOverdueRentals();

        // Then
        verify(rentalRepository).findOverdueActiveRentals(any());
        verify(carService).findById(1L);
        verify(paymentRepository).save(any(Payment.class));
    }
}
