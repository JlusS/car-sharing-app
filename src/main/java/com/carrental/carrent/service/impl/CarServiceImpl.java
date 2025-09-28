package com.carrental.carrent.service.impl;

import com.carrental.carrent.dto.car.CarDto;
import com.carrental.carrent.mapper.CarMapper;
import com.carrental.carrent.model.Car;
import com.carrental.carrent.repository.car.CarRepository;
import com.carrental.carrent.service.CarService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class CarServiceImpl implements CarService {
    private final CarRepository carRepository;
    private final CarMapper carMapper;

    @Override
    public CarDto save(CarDto carDto) {
        Car car = carMapper.toEntity(carDto);
        carRepository.save(car);
        return carMapper.toDto(car);
    }

    @Override
    public Page<CarDto> findAll(Pageable pageable) {
        return carRepository.findAll(pageable)
                .map(carMapper::toDto);
    }

    @Override
    public CarDto findById(Long id) {
        return carMapper.toDto(carRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Car not found")
                ));
    }

    @Override
    public CarDto update(Long id, CarDto carDto) {
        Car car = carRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Car not found")
        );

        carMapper.updateModelFromDto(car, carDto);
        carRepository.save(car);
        return null;
    }

    @Override
    public void deleteById(Long id) {
        carRepository.deleteById(id);
    }
}
