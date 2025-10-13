package com.carrental.carrent.mapper;

import com.carrental.carrent.config.MapperConfig;
import com.carrental.carrent.dto.rental.RentalDto;
import com.carrental.carrent.dto.rental.RentalResponseDto;
import com.carrental.carrent.model.Rental;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfig.class)
public interface RentalMapper {
    RentalDto toDto(Rental rental);

    RentalResponseDto toResponseDto(Rental rental);

    Rental toModel(RentalDto rentalDto);
}
