package com.carrental.carrent.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.carrental.carrent.dto.car.CarDto;
import com.carrental.carrent.mapper.CarMapper;
import com.carrental.carrent.model.Car;
import com.carrental.carrent.model.CarType;
import com.carrental.carrent.repository.car.CarRepository;
import com.carrental.carrent.service.impl.CarServiceImpl;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class CarServiceTest {

    @Mock
    private CarRepository carRepository;

    @Mock
    private CarMapper carMapper;

    @InjectMocks
    private CarServiceImpl carService;

    @Test
    @DisplayName("Save car - should save and return car DTO")
    void save_ValidCarDto_ShouldSaveAndReturnCarDto() {
        // Given
        CarDto requestDto = createValidCarDto();
        Car car = createCarFromDto(requestDto);
        CarDto expectedResponse = createExpectedCarDto();

        when(carMapper.toEntity(requestDto)).thenReturn(car);
        when(carRepository.save(car)).thenReturn(car);
        when(carMapper.toDto(any(Car.class))).thenReturn(expectedResponse);

        // When
        CarDto actual = carService.save(requestDto);

        // Then
        assertNotNull(actual);
        assertEquals(expectedResponse.getId(), actual.getId());
        assertEquals(expectedResponse.getModel(), actual.getModel());
        assertEquals(expectedResponse.getBrand(), actual.getBrand());
        assertEquals(expectedResponse.getCarType(), actual.getCarType());
        assertEquals(expectedResponse.getInventory(), actual.getInventory());
        assertEquals(expectedResponse.getDailyFee(), actual.getDailyFee());

        verify(carRepository, times(1)).save(car);
        verify(carMapper, times(1)).toDto(any(Car.class));
    }

    private CarDto createValidCarDto() {
        CarDto dto = new CarDto();
        dto.setModel("Model X");
        dto.setBrand("Tesla");
        dto.setCarType(CarType.SEDAN);
        dto.setInventory(5);
        dto.setDailyFee(BigDecimal.valueOf(50.0));
        return dto;
    }

    private Car createCarFromDto(CarDto dto) {
        Car car = new Car();
        car.setId(1L);
        car.setModel(dto.getModel());
        car.setBrand(dto.getBrand());
        car.setCarType(dto.getCarType());
        car.setInventory(dto.getInventory());
        car.setDailyFee(dto.getDailyFee());
        return car;
    }

    private CarDto createExpectedCarDto() {
        CarDto dto = new CarDto();
        dto.setId(1L);
        dto.setModel("Model X");
        dto.setBrand("Tesla");
        dto.setCarType(CarType.SEDAN);
        dto.setInventory(5);
        dto.setDailyFee(BigDecimal.valueOf(50.0));
        return dto;
    }

    @Test
    @DisplayName("Find all cars - should return page of car DTOs")
    void findAll_WithPageable_ShouldReturnPageOfCarDtos() {
        // Given

        Car car = new Car();
        car.setId(1L);
        car.setModel("Camry");
        car.setBrand("Toyota");
        car.setCarType(CarType.SEDAN);
        car.setInventory(5);
        car.setDailyFee(BigDecimal.valueOf(50.00));

        CarDto carDto = new CarDto();
        carDto.setId(1L);
        carDto.setModel("Camry");
        carDto.setBrand("Toyota");
        carDto.setCarType(CarType.SEDAN);
        carDto.setInventory(5);
        carDto.setDailyFee(BigDecimal.valueOf(50.00));

        Pageable pageable = PageRequest.of(0, 10);

        Page<Car> carPage = new PageImpl<>(java.util.List.of(car), pageable, 1);

        when(carRepository.findAll(pageable)).thenReturn(carPage);
        when(carMapper.toDto(car)).thenReturn(carDto);

        // When
        Page<CarDto> result = carService.findAll(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Camry", result.getContent().get(0).getModel());
        verify(carRepository).findAll(pageable);
        verify(carMapper).toDto(car);
    }

    @Test
    @DisplayName("Find car by ID - should return car DTO when exists")
    void findById_ExistingId_ShouldReturnCarDto() {
        // Given
        Long carId = 1L;

        Car car = new Car();
        car.setId(carId);
        car.setModel("Camry");
        car.setBrand("Toyota");
        car.setCarType(CarType.SEDAN);
        car.setInventory(5);
        car.setDailyFee(BigDecimal.valueOf(50.00));

        CarDto carDto = new CarDto();
        carDto.setId(carId);
        carDto.setModel("Camry");
        carDto.setBrand("Toyota");
        carDto.setCarType(CarType.SEDAN);
        carDto.setInventory(5);
        carDto.setDailyFee(BigDecimal.valueOf(50.00));

        when(carRepository.findById(carId)).thenReturn(Optional.of(car));
        when(carMapper.toDto(car)).thenReturn(carDto);

        // When
        CarDto result = carService.findById(carId);

        // Then
        assertNotNull(result);
        assertEquals(carId, result.getId());
        assertEquals("Camry", result.getModel());
        verify(carRepository).findById(carId);
        verify(carMapper).toDto(car);
    }

    @Test
    @DisplayName("Find car by ID - should throw exception when not exists")
    void findById_NonExistingId_ShouldThrowException() {
        // Given
        Long carId = 999L;
        when(carRepository.findById(carId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> carService.findById(carId));
        assertEquals("Car not found", exception.getMessage());
        verify(carRepository).findById(carId);
    }

    @Test
    @DisplayName("Update car - should update and return car DTO")
    void update_ExistingCar_ShouldUpdateAndReturnCarDto() {
        // Given

        CarDto carDto = new CarDto();
        carDto.setModel("Updated Camry");
        carDto.setBrand("Toyota");
        carDto.setCarType(CarType.SEDAN);
        carDto.setInventory(3);
        carDto.setDailyFee(BigDecimal.valueOf(55.00));

        Long carId = 1L;
        Car existingCar = new Car();
        existingCar.setId(carId);
        existingCar.setModel("Camry");
        existingCar.setBrand("Toyota");
        existingCar.setCarType(CarType.SEDAN);
        existingCar.setInventory(5);
        existingCar.setDailyFee(BigDecimal.valueOf(50.00));

        Car updatedCar = new Car();
        updatedCar.setId(carId);
        updatedCar.setModel("Updated Camry");
        updatedCar.setBrand("Toyota");
        updatedCar.setCarType(CarType.SEDAN);
        updatedCar.setInventory(3);
        updatedCar.setDailyFee(BigDecimal.valueOf(55.00));

        when(carRepository.findById(carId)).thenReturn(Optional.of(existingCar));
        when(carRepository.save(existingCar)).thenReturn(updatedCar);

        // When
        CarDto result = carService.update(carId, carDto);

        // Then
        verify(carRepository).findById(carId);
        verify(carMapper).updateModelFromDto(existingCar, carDto);
        verify(carRepository).save(existingCar);
    }

    @Test
    @DisplayName("Update car - should throw exception when car not exists")
    void update_NonExistingCar_ShouldThrowException() {
        // Given
        Long carId = 999L;
        CarDto carDto = new CarDto();

        when(carRepository.findById(carId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> carService.update(carId, carDto));
        assertEquals("Car not found", exception.getMessage());
        verify(carRepository).findById(carId);
    }

    @Test
    @DisplayName("Delete car by ID - should delete car")
    void deleteById_ExistingCar_ShouldDeleteCar() {
        // Given
        Long carId = 1L;

        // When
        carService.deleteById(carId);

        // Then
        verify(carRepository).deleteById(carId);
    }
}
