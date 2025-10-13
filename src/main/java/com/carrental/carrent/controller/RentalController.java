package com.carrental.carrent.controller;

import com.carrental.carrent.dto.rental.RentalDto;
import com.carrental.carrent.dto.rental.RentalResponseDto;
import com.carrental.carrent.dto.rental.RentalReturnRequestDto;
import com.carrental.carrent.service.CarService;
import com.carrental.carrent.service.RentalService;
import com.carrental.carrent.service.UserService;
import com.carrental.carrent.service.telegram.TelegramNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rentals")
@RequiredArgsConstructor
@Tag(name = "Rentals", description = "Managing users' car rentals")
public class RentalController {

    private final RentalService rentalService;
    private final TelegramNotificationService telegramService;
    private final UserService userService;
    private final CarService carService;

    @PostMapping
    @Operation(summary = "Create a new rental",
            description = "Adds a new rental and decreases car inventory by 1")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    public RentalDto createRental(@RequestBody @Valid RentalDto rentalDto) {
        telegramService.sendNewRentalNotification(rentalDto,
                userService.findById(rentalDto.getUserId()),
                carService.findById(rentalDto.getCarId()));
        return rentalService.createRental(rentalDto);
    }

    @GetMapping(params = "user_id")
    @Operation(summary = "Get rentals by user and status",
            description = "Returns rentals filtered by user ID and active status")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    public List<RentalResponseDto> getRentalsByUserAndStatus(
            @RequestParam("user_id") Long userId,
            @RequestParam(value = "is_active", required = false) Boolean isActive
    ) {
        return rentalService.getRentalsByUserAndStatus(userId, isActive);
    }

    @GetMapping
    @Operation(summary = "Get current rental",
            description = "Returns the current rental for the authenticated user")
    @PreAuthorize("hasAnyRole('MANAGER', 'CUSTOMER')")
    public RentalResponseDto getCurrentRental() {
        return rentalService.getSpecificRental();
    }

    @PostMapping("/return")
    @Operation(summary = "Return rental",
            description = "Sets actual return date and increases car inventory by 1")
    @PreAuthorize("hasAnyRole('MANAGER', 'CUSTOMER')")
    public RentalResponseDto returnRental(
            @RequestBody @Valid RentalReturnRequestDto requestDto) {
        return rentalService.returnRentalDate(requestDto);
    }
}
