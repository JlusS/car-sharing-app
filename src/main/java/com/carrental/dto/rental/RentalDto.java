package com.carrental.dto.rental;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;

@Data
public class RentalDto {
    private Long id;

    @NotNull(message = "Rental date cannot be null")
    private LocalDate rentalDate;

    @NotNull(message = "Return date cannot be null")
    private LocalDate returnDate;

    @NotNull(message = "Car ID cannot be null")
    private Long carId;

    @NotNull(message = "User ID cannot be null")
    private Long userId;
}
