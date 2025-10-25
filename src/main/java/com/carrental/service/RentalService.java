package com.carrental.service;

import com.carrental.dto.rental.RentalDto;
import com.carrental.dto.rental.RentalResponseDto;
import com.carrental.dto.rental.RentalReturnRequestDto;
import java.util.List;

public interface RentalService {
    RentalDto createRental(RentalDto rentalDto);

    List<RentalResponseDto> getAllRentals();

    List<RentalResponseDto> getRentalsByUserAndStatus(Long userId, Boolean isActive);

    RentalResponseDto getSpecificRental();

    RentalResponseDto returnRentalDate(RentalReturnRequestDto requestDto);
}
