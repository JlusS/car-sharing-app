package com.carrental.carrent.mapper;

import com.carrental.carrent.config.MapperConfig;
import com.carrental.carrent.dto.car.CarDto;
import com.carrental.carrent.model.Car;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class)
public interface CarMapper {
    CarDto toDto(Car car);

    @Mapping(target = "id", ignore = true)
    Car toEntity(CarDto carDto);

    @Mapping(target = "id", ignore = true)
    void updateModelFromDto(@MappingTarget Car car, CarDto carDto);
}
