package com.carrental.carrent.service.telegram;

import com.carrental.carrent.dto.car.CarDto;
import com.carrental.carrent.dto.rental.RentalDto;
import com.carrental.carrent.dto.rental.RentalResponseDto;
import com.carrental.carrent.dto.user.UserResponseDto;
import com.carrental.carrent.service.CarService;
import com.carrental.carrent.service.UserService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "telegram.bot.enabled", havingValue = "true", matchIfMissing = true)
public class TelegramNotificationService {
    private final CarRentalTelegramBot telegramBot;
    private final UserService userService;
    private final CarService carService;

    @Value("${telegram.bot.admin-chat-id:}")
    private String adminChatId;

    public void sendNewRentalNotification(RentalDto rental, UserResponseDto user, CarDto car) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String message = String.format(
                """
                        🚗 NEW RENTAL
                        Customer: %s %s (%s)
                        Car: %s %s %s
                        Period: %s - %s
                        Total: $%.2f
                        Rental ID: %d""",
                user.getFirstName(), user.getLastName(), user.getEmail(),
                car.getBrand(), car.getModel(), car.getCarType(),
                rental.getRentalDate(), rental.getReturnDate(),
                calculateRentalPrice(rental, car),
                rental.getId()
        );
        sendToAdmin(message);
    }

    public void sendSuccessfulPaymentNotification(String payment) {
        String message = String.format(
                "✅ PAYMENT SUCCESSFUL\n"
                        + "Payment ID: %s",
                payment
        );
        sendToAdmin(message);
    }

    public void sendNewCarNotification(CarDto car) {
        String message = String.format(
                """
                        🆕 NEW CAR ADDED
                        Car: %s %s %s
                        Inventory: %d
                        Daily fee: $%.2f
                        Car ID: %d""",
                car.getBrand(), car.getModel(), car.getCarType(),
                car.getInventory(),
                car.getDailyFee(),
                car.getId()
        );
        sendToAdmin(message);
    }

    public void sendOverdueRentalNotification(RentalResponseDto rental,
                                              UserResponseDto user,
                                              CarDto car) {
        long daysOverdue = ChronoUnit.DAYS.between(rental.getReturnDate(), LocalDate.now());

        String message = String.format(
                """
                        ⚠️ OVERDUE RENTAL
                        Customer: %s %s (%s)
                        Car: %s %s %s
                        Expected Return: %s
                        Overdue by: %d days
                        Rental ID: %d
                        Potential Fine: $%.2f""",
                user.getFirstName(), user.getLastName(), user.getEmail(),
                car.getBrand(), car.getModel(), car.getCarType(),
                rental.getReturnDate(),
                daysOverdue,
                rental.getId(),
                calculatePotentialFine(rental, car, daysOverdue)
        );
        sendToAdmin(message);
    }

    public void sendFineNotification(RentalResponseDto rental, BigDecimal fineAmount) {
        try {
            UserResponseDto user = userService.findById(rental.getUserId());
            CarDto car = carService.findById(rental.getCarId());

            long overdueDays = ChronoUnit.DAYS.between(rental.getReturnDate(), LocalDate.now());

            String message = String.format(
                    """
                            💰 FINE APPLIED
                            Customer: %s %s (%s)
                            Car: %s %s
                            Due date: %s
                            Overdue: %d days
                            Fine amount: $%.2f
                            Rental ID: %d""",
                    user.getFirstName(), user.getLastName(), user.getEmail(),
                    car.getBrand(), car.getModel(),
                    rental.getReturnDate(),
                    overdueDays,
                    fineAmount,
                    rental.getId()
            );

            sendToAdmin(message);
        } catch (Exception e) {
            System.err.println("Error sending fine notification: " + e.getMessage());
        }
    }

    private void sendToAdmin(String message) {
        if (adminChatId == null || adminChatId.trim().isEmpty()) {
            System.err.println("❌ Telegram chat ID is not configured or empty");
            return;
        }

        String trimmedChatId = adminChatId.trim();
        if (trimmedChatId.isEmpty()) {
            System.err.println("❌ Telegram chat ID is empty after trimming");
            return;
        }

        try {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(trimmedChatId);
            sendMessage.setText(message);
            telegramBot.execute(sendMessage);

            System.out.println("✅ Telegram notification sent: " + message);
        } catch (TelegramApiException e) {
            System.err.println("❌ Error sending Telegram message: " + e.getMessage());
            System.err.println("Admin Chat ID: " + trimmedChatId);
            System.err.println("Message: " + message);
        } catch (Exception e) {
            System.err.println("❌ Unexpected error: " + e.getMessage());
        }
    }

    private BigDecimal calculateRentalPrice(RentalDto rental, CarDto car) {
        long days = ChronoUnit.DAYS.between(rental.getRentalDate(), rental.getReturnDate());
        return car.getDailyFee().multiply(BigDecimal.valueOf(Math.max(1, days)));
    }

    private BigDecimal calculatePotentialFine(RentalResponseDto rental,
                                              CarDto car,
                                              long daysOverdue) {
        BigDecimal dailyFee = car.getDailyFee();
        BigDecimal fineMultiplier = BigDecimal.valueOf(1.5); // 50% penalty
        return dailyFee.multiply(BigDecimal.valueOf(daysOverdue)).multiply(fineMultiplier);
    }
}
