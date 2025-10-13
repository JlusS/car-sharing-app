package com.carrental.carrent.dto.rental;

import java.time.LocalDate;
import lombok.Data;

@Data
public class RentalDto {
    private Long id;
    private LocalDate rentalDate;
    private LocalDate returnDate;
    private Long carId;
    private Long userId;
}
