package com.carrental.carrent.service.telegram;

import static org.telegram.abilitybots.api.objects.Locality.USER;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;

import com.carrental.carrent.dto.payment.PaymentDto;
import com.carrental.carrent.dto.rental.RentalResponseDto;
import com.carrental.carrent.dto.user.UserResponseDto;
import com.carrental.carrent.mapper.CarMapper;
import com.carrental.carrent.model.Car;
import com.carrental.carrent.service.CarService;
import com.carrental.carrent.service.PaymentService;
import com.carrental.carrent.service.RentalService;
import com.carrental.carrent.service.UserService;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;

@Component
public class CarRentalTelegramBot extends AbilityBot {

    private final CarService carService;
    private final RentalService rentalService;
    private final UserService userService;
    @Lazy
    private final PaymentService paymentService;
    private final CarMapper carMapper;

    public CarRentalTelegramBot(
            @Value("${telegram.bot.token}") String botToken,
            @Value("${telegram.bot.username}") String botUsername,
            CarService carService,
            RentalService rentalService,
            UserService userService,
            PaymentService paymentService,
            CarMapper carMapper) {
        super(botToken, botUsername);
        this.carService = carService;
        this.rentalService = rentalService;
        this.userService = userService;
        this.paymentService = paymentService;
        this.carMapper = carMapper;
    }

    public Ability start() {
        return Ability.builder()
                .name("start")
                .info("Start the bot")
                .locality(USER)
                .privacy(PUBLIC)
                .action(ctx -> {
                    String welcomeText = """
                            🚗 Welcome to CarRental Bot!
                            
                            Available commands:
                            /help - Show help
                            /status - System status
                            /cars - All cars
                            /rentals_active - Active rentals
                            /rentals_overdue - Overdue rentals
                            /payments_pending - Pending payments
                            
                            For administrators:
                            /notifications on - Enable notifications
                            /notifications off - Disable notifications
                            """;
                    silent.send(welcomeText, ctx.chatId());
                })
                .build();
    }

    public Ability help() {
        return Ability.builder()
                .name("help")
                .info("Show help")
                .locality(USER)
                .privacy(PUBLIC)
                .action(ctx -> {
                    String helpText = """
                            📋 Available commands:
                            
                            Basic:
                            /start - Start bot
                            
                            Information:
                            /cars - All cars
                            /cars_available - Available cars
                            /rentals_active - Active rentals
                            /payments_pending - Pending payments
                            
                            Notifications:
                            /notifications on - Enable
                            /notifications off - Disable
                            """;
                    silent.send(helpText, ctx.chatId());
                })
                .build();
    }

    public Ability getAllCars() {
        return Ability.builder()
                .name("cars")
                .info("List all cars")
                .locality(USER)
                .privacy(PUBLIC)
                .action(ctx -> {
                    try {
                        Page<Car> cars = carService.findAll(Pageable.unpaged())
                                .map(carMapper::toEntity);
                        String carsInfo = formatCarsInfo(cars.getContent());
                        silent.send(carsInfo, ctx.chatId());
                    } catch (Exception e) {
                        silent.send("❌ Error getting cars list", ctx.chatId());
                    }
                })
                .build();
    }

    public Ability getActiveRentals() {
        return Ability.builder()
                .name("rentals_active")
                .info("Active rentals")
                .locality(USER)
                .privacy(PUBLIC)
                .action(ctx -> {
                    try {
                        List<RentalResponseDto> activeRentals = rentalService.getAllRentals();
                        String activeRentalsInfo = formatRentalsInfo(activeRentals);
                        silent.send(activeRentalsInfo, ctx.chatId());
                    } catch (Exception e) {
                        silent.send("❌ Error getting active rentals", ctx.chatId());
                    }
                })
                .build();
    }

    public Ability getPendingPayments() {
        return Ability.builder()
                .name("payments_pending")
                .info("Pending payments")
                .locality(USER)
                .privacy(PUBLIC)
                .action(ctx -> {
                    try {
                        List<PaymentDto> pendingPayments = paymentService.getAllActivePayments();
                        String pendingPaymentsInfo = formatPaymentsInfo(pendingPayments);
                        silent.send(pendingPaymentsInfo, ctx.chatId());
                    } catch (Exception e) {
                        silent.send("❌ Error getting pending payments", ctx.chatId());
                    }
                })
                .build();
    }

    private String formatCarsInfo(List<Car> cars) {
        if (cars.isEmpty()) {
            return "No cars found";
        }

        StringBuilder sb = new StringBuilder("🚗 Cars:\n\n");
        int size = 0;
        for (int i = 0; i < cars.size(); i++) {
            Car car = cars.get(i);
            size += car.getInventory();
            sb.append(String.format("%d. %s %s (%s)\n",
                    i + 1, car.getBrand(), car.getModel(), car.getCarType()));
            sb.append(String.format("   Inventory: %d | Daily Fee: $%.2f\n\n",
                    car.getInventory(), car.getDailyFee()));
        }
        sb.append(String.format("Total: %d cars", size));
        return sb.toString();
    }

    private String formatRentalsInfo(List<RentalResponseDto> rentals) {
        if (rentals.isEmpty()) {
            return "No " + "Active Rentals".toLowerCase();
        }

        StringBuilder sb = new StringBuilder("""
                🔄 Active Rentals:
                
                """);
        for (int i = 0; i < rentals.size(); i++) {
            RentalResponseDto rental = rentals.get(i);
            try {
                Car car = carMapper.toEntity(carService.findById(rental.getCarId()));
                UserResponseDto user = userService.findById(1L);

                sb.append(String.format("%d. %s %s - %s %s\n",
                        i + 1, user.getFirstName(), user.getLastName(),
                        car.getBrand(), car.getModel()));
                sb.append(String.format("   Period: %s - %s\n",
                        rental.getRentalDate(), rental.getReturnDate()));

                if (rental.getReturnDate().isBefore(LocalDate.now())) {
                    long daysOverdue = ChronoUnit.DAYS.between(
                            rental.getReturnDate(), LocalDate.now());
                    sb.append(String.format("   ⚠️ Overdue by: %d days\n", daysOverdue));
                }
                sb.append("\n");
            } catch (Exception e) {
                sb.append(String.format("%d. Rental ID: %d (Error loading details)\n\n",
                        i + 1, rental.getId()));
            }
        }
        sb.append(String.format("Total: %d rentals", rentals.size()));
        return sb.toString();
    }

    private String formatPaymentsInfo(List<PaymentDto> payments) {
        if (payments.isEmpty()) {
            return "No pending payments";
        }

        StringBuilder sb = new StringBuilder("💰 Pending Payments:\n\n");
        for (int i = 0; i < payments.size(); i++) {
            PaymentDto payment = payments.get(i);
            try {
                List<RentalResponseDto> rental = rentalService.getAllRentals();
                UserResponseDto user = userService.findById(1L);

                sb.append(String.format("%d. %s %s\n",
                        i + 1, user.getFirstName(), user.getLastName()));
                sb.append(String.format("   Amount: $%.2f\n", payment.getAmountToPay()));
                sb.append(String.format("   Type: %s | Rental: #%d\n\n",
                        payment.getPaymentType(), payment.getRentalId()));
            } catch (Exception e) {
                sb.append(String.format("%d. Payment ID: %d (Error loading details)\n\n",
                        i + 1, payment.getId()));
            }
        }
        sb.append(String.format("Total: %d payments", payments.size()));
        return sb.toString();
    }

    @Override
    public long creatorId() {
        return 0;
    }
}
