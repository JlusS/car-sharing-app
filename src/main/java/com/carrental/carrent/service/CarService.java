package com.carrental.carrent.service;

import com.carrental.carrent.dto.car.CarDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CarService {
    CarDto save(CarDto carDto);

    Page<CarDto> findAll(Pageable pageable);

    CarDto findById(Long id);

    CarDto update(Long id, CarDto carDto);

    void deleteById(Long id);

}
