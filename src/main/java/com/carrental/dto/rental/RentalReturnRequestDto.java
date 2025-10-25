package com.carrental.dto.rental;

import java.time.LocalDate;
import lombok.Data;

@Data
public class RentalReturnRequestDto {
    private LocalDate actualReturnDate;
}
