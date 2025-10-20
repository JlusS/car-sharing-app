package com.carrental.carrent.controller;

import com.carrental.carrent.dto.car.CarDto;
import com.carrental.carrent.service.CarService;
import com.carrental.carrent.service.telegram.TelegramNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cars")
@RequiredArgsConstructor
@Tag(name = "Car", description = "Car management APIs")
public class CarController {
    private final CarService carService;
    private final TelegramNotificationService telegramNotificationService;

    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new car", description =
            "Create a new car. Accessible by MANAGER role.")
    public CarDto createCar(@RequestBody @Valid CarDto carDto) {
        CarDto savedCarDto = carService.save(carDto);
        telegramNotificationService.sendNewCarNotification(savedCarDto);
        return savedCarDto;
    }

    @GetMapping
    @Operation(summary = "Get all cars", description =
            "Returns a paginated list of all cars. Accessible by CUSTOMER role.")
    public Iterable<CarDto> getAllCars(Pageable pageable) {
        return carService.findAll(pageable);
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'CUSTOMER')")
    @GetMapping("/{id}")
    @Operation(summary = "Get car by ID", description =
            "Returns a car by its ID. Accessible by CUSTOMER role.")
    public CarDto getCarById(@PathVariable Long id) {
        return carService.findById(id);
    }

    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    @PutMapping("/{id}")
    @Operation(summary = "Update a car", description =
            "Update an existing car by its ID. Accessible by MANAGER role.")
    public CarDto updateCar(@PathVariable Long id, @RequestBody @Valid CarDto carDto) {
        return carService.update(id, carDto);
    }

    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a car", description =
            "Delete a car by its ID. Accessible by MANAGER role.")
    public void deleteCar(@PathVariable Long id) {
        carService.deleteById(id);
    }
}
