package com.carrental.service.telegram;

import com.carrental.dto.car.CarDto;
import com.carrental.dto.rental.RentalResponseDto;
import com.carrental.dto.user.UserResponseDto;
import com.carrental.mapper.CarMapper;
import com.carrental.mapper.UserMapper;
import com.carrental.repository.car.CarRepository;
import com.carrental.repository.user.UserRepository;
import com.carrental.service.RentalService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "telegram.bot.enabled", havingValue = "true", matchIfMissing = true)
public class OverdueRentalScheduler {

    private final RentalService rentalService;
    private final TelegramNotificationService telegramNotificationService;
    private final UserRepository userRepository;
    private final CarRepository carRepository;
    private final CarMapper carMapper;
    private final UserMapper userMapper;

    @Scheduled(fixedRate = 300000)
    public void checkOverdueRentals() {
        try {
            System.out.println("🔍 Checking for overdue rentals...");

            List<RentalResponseDto> activeRentals = rentalService.getAllRentals();
            List<RentalResponseDto> overdueRentals = activeRentals.stream()
                    .filter(rental -> rental.getReturnDate().isBefore(LocalDate.now()))
                    .toList();

            if (overdueRentals.isEmpty()) {
                System.out.println("✅ No overdue rentals found");
                return;
            }

            for (RentalResponseDto rental : overdueRentals) {
                try {
                    UserResponseDto user = userMapper.toUserResponseDto(userRepository
                            .findById(rental.getUserId())
                            .orElseThrow(() -> new RuntimeException("User not found")));
                    CarDto car = carRepository.findById(rental.getCarId())
                            .map(carMapper::toDto)
                            .orElseThrow(() -> new RuntimeException("Car not found"));

                    telegramNotificationService.sendOverdueRentalNotification(rental, user, car);

                    Thread.sleep(1000);

                } catch (Exception e) {
                    System.err.println("❌ Error processing rental "
                            + rental.getId() + ": " + e.getMessage());
                }
            }

            System.out.println("✅ Sent notifications for "
                    + overdueRentals.size() + " overdue rentals");

        } catch (Exception e) {
            System.err.println("❌ Error in overdue rental scheduler: " + e.getMessage());
        }
    }
}
