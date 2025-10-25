package com.carrental.mapper;

import com.carrental.config.MapperConfig;
import com.carrental.dto.rental.RentalDto;
import com.carrental.dto.rental.RentalResponseDto;
import com.carrental.model.Rental;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfig.class)
public interface RentalMapper {
    RentalDto toDto(Rental rental);

    RentalResponseDto toResponseDto(Rental rental);

    Rental toModel(RentalDto rentalDto);
}
