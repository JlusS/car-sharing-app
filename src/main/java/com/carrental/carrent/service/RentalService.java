package com.carrental.carrent.service;

import com.carrental.carrent.dto.rental.RentalDto;
import com.carrental.carrent.dto.rental.RentalResponseDto;
import com.carrental.carrent.dto.rental.RentalReturnRequestDto;
import java.util.List;

public interface RentalService {
    RentalDto createRental(RentalDto rentalDto);

    List<RentalResponseDto> getAllRentals();

    List<RentalResponseDto> getRentalsByUserAndStatus(Long userId, Boolean isActive);

    RentalResponseDto getSpecificRental();

    RentalResponseDto returnRentalDate(RentalReturnRequestDto requestDto);
}
