package com.carrental.carrent.service.telegram;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.carrental.carrent.dto.car.CarDto;
import com.carrental.carrent.dto.rental.RentalDto;
import com.carrental.carrent.dto.rental.RentalResponseDto;
import com.carrental.carrent.dto.user.UserResponseDto;
import com.carrental.carrent.model.CarType;
import com.carrental.carrent.service.CarService;
import com.carrental.carrent.service.UserService;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@ExtendWith(MockitoExtension.class)
class TelegramNotificationServiceTest {

    @Mock
    private CarRentalTelegramBot telegramBot;

    @Mock
    private UserService userService;

    @Mock
    private CarService carService;

    private TelegramNotificationService telegramNotificationService;

    @BeforeEach
    void setUp() {
        telegramNotificationService = new TelegramNotificationService(
                telegramBot, userService, carService);
        ReflectionTestUtils.setField(telegramNotificationService, "adminChatId", "12345");
    }

    @Test
    @DisplayName("Send new rental notification - should send message to admin")
    void sendNewRentalNotification_ValidData_ShouldSendMessage() throws TelegramApiException {
        // Given
        RentalDto rental = new RentalDto();
        rental.setId(1L);
        rental.setRentalDate(LocalDate.now());
        rental.setReturnDate(LocalDate.now().plusDays(7));

        UserResponseDto user = new UserResponseDto();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john@example.com");

        CarDto car = createValidCarDto();

        // When
        telegramNotificationService.sendNewRentalNotification(rental, user, car);

        // Then
        verify(telegramBot).execute(any(SendMessage.class));
    }

    @Test
    @DisplayName("Send successful payment notification - should send message to admin")
    void sendSuccessfulPaymentNotification_ValidPayment_ShouldSendMessage()
            throws TelegramApiException {
        // Given
        String paymentId = "pay_123";

        // When
        telegramNotificationService.sendSuccessfulPaymentNotification(paymentId);

        // Then
        verify(telegramBot).execute(any(SendMessage.class));
    }

    @Test
    @DisplayName("Send new car notification - should send message to admin")
    void sendNewCarNotification_ValidCar_ShouldSendMessage() throws TelegramApiException {
        // Given
        CarDto car = createValidCarDto();

        // When
        telegramNotificationService.sendNewCarNotification(car);

        // Then
        verify(telegramBot).execute(any(SendMessage.class));
    }

    @Test
    @DisplayName("Send overdue rental notification - should send message to admin")
    void sendOverdueRentalNotification_ValidData_ShouldSendMessage() throws TelegramApiException {
        // Given
        RentalResponseDto rental = new RentalResponseDto();
        rental.setId(1L);
        rental.setUserId(1L);
        rental.setCarId(1L);
        rental.setReturnDate(LocalDate.now().minusDays(3));

        UserResponseDto user = new UserResponseDto();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john@example.com");

        CarDto car = createValidCarDto();

        // When
        telegramNotificationService.sendOverdueRentalNotification(rental, user, car);

        // Then
        verify(telegramBot).execute(any(SendMessage.class));
    }

    @Test
    @DisplayName("Send fine notification - should send message to admin")
    void sendFineNotification_ValidData_ShouldSendMessage() throws TelegramApiException {
        // Given
        RentalResponseDto rental = new RentalResponseDto();
        rental.setId(1L);
        rental.setUserId(1L);
        rental.setCarId(1L);
        rental.setReturnDate(LocalDate.now().minusDays(5));

        UserResponseDto user = new UserResponseDto();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john@example.com");

        CarDto car = createValidCarDto();

        when(userService.findById(1L)).thenReturn(user);
        when(carService.findById(1L)).thenReturn(car);

        // When
        BigDecimal fineAmount = BigDecimal.valueOf(250.00);
        telegramNotificationService.sendFineNotification(rental, fineAmount);

        // Then
        verify(telegramBot).execute(any(SendMessage.class));
        verify(userService).findById(1L);
        verify(carService).findById(1L);
    }

    @Test
    @DisplayName("Send notification - should handle Telegram API exception gracefully")
    void sendNotification_TelegramApiException_ShouldHandleGracefully()
            throws TelegramApiException {
        // Given
        CarDto car = createValidCarDto();

        doThrow(new TelegramApiException("API error"))
                .when(telegramBot)
                .execute(any(SendMessage.class));

        // When
        telegramNotificationService.sendNewCarNotification(car);

        // Then
        verify(telegramBot).execute(any(SendMessage.class));
    }

    @Test
    @DisplayName("Send notification - should handle generic exception gracefully")
    void sendNotification_GenericException_ShouldHandleGracefully()
            throws TelegramApiException {
        // Given
        CarDto car = createValidCarDto();

        doThrow(new RuntimeException("Generic error"))
                .when(telegramBot)
                .execute(any(SendMessage.class));

        // When
        telegramNotificationService.sendNewCarNotification(car);

        // Then
        verify(telegramBot).execute(any(SendMessage.class));
    }

    @Test
    @DisplayName("Send fine notification - should handle user not found exception")
    void sendFineNotification_UserNotFound_ShouldHandleException() throws TelegramApiException {
        // Given
        RentalResponseDto rental = new RentalResponseDto();
        rental.setId(1L);
        rental.setUserId(999L);
        rental.setCarId(1L);
        rental.setReturnDate(LocalDate.now().minusDays(5));

        BigDecimal fineAmount = BigDecimal.valueOf(250.00);

        when(userService.findById(999L)).thenThrow(new IllegalArgumentException("User not found"));

        // When
        telegramNotificationService.sendFineNotification(rental, fineAmount);

        // Then
        verify(telegramBot, never()).execute(any(SendMessage.class));
        verify(userService).findById(999L);
    }

    @Test
    @DisplayName("Send fine notification - should handle car not found exception")
    void sendFineNotification_CarNotFound_ShouldHandleException() throws TelegramApiException {
        // Given
        RentalResponseDto rental = new RentalResponseDto();
        rental.setId(1L);
        rental.setUserId(1L);
        rental.setCarId(999L);
        rental.setReturnDate(LocalDate.now().minusDays(5));

        UserResponseDto user = new UserResponseDto();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john@example.com");

        when(userService.findById(1L)).thenReturn(user);
        when(carService.findById(999L)).thenThrow(new IllegalArgumentException("Car not found"));

        // When
        BigDecimal fineAmount = BigDecimal.valueOf(250.00);
        telegramNotificationService.sendFineNotification(rental, fineAmount);

        // Then
        verify(telegramBot, never()).execute(any(SendMessage.class));
        verify(userService).findById(1L);
        verify(carService).findById(999L);
    }

    @Test
    @DisplayName("Send notification with null admin chat ID - should handle gracefully")
    void sendNotification_NullAdminChatId_ShouldHandleGracefully() throws TelegramApiException {
        // Given
        ReflectionTestUtils.setField(telegramNotificationService, "adminChatId", null);

        CarDto car = createValidCarDto();

        // When
        telegramNotificationService.sendNewCarNotification(car);

        // Then
        verify(telegramBot, never()).execute(any(SendMessage.class));
    }

    @Test
    @DisplayName("Send notification with empty admin chat ID - should handle gracefully")
    void sendNotification_EmptyAdminChatId_ShouldHandleGracefully() throws TelegramApiException {
        // Given
        ReflectionTestUtils.setField(telegramNotificationService, "adminChatId", "");

        CarDto car = createValidCarDto();

        // When
        telegramNotificationService.sendNewCarNotification(car);

        // Then
        verify(telegramBot, never()).execute(any(SendMessage.class));
    }

    @Test
    @DisplayName("Send notification with blank admin chat ID - should handle gracefully")
    void sendNotification_BlankAdminChatId_ShouldHandleGracefully() throws TelegramApiException {
        // Given
        ReflectionTestUtils.setField(telegramNotificationService, "adminChatId", "   ");

        CarDto car = createValidCarDto();

        // When
        telegramNotificationService.sendNewCarNotification(car);

        // Then
        verify(telegramBot, never()).execute(any(SendMessage.class));
    }

    private CarDto createValidCarDto() {
        CarDto car = new CarDto();
        car.setId(1L);
        car.setBrand("Toyota");
        car.setModel("Camry");
        car.setCarType(CarType.SEDAN);
        car.setInventory(5);
        car.setDailyFee(BigDecimal.valueOf(50.00));
        return car;
    }
}
