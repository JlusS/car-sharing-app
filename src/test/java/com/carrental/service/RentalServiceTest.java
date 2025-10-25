package com.carrental.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.carrental.dto.rental.RentalDto;
import com.carrental.dto.rental.RentalResponseDto;
import com.carrental.dto.rental.RentalReturnRequestDto;
import com.carrental.mapper.RentalMapper;
import com.carrental.model.Car;
import com.carrental.model.Rental;
import com.carrental.model.User;
import com.carrental.repository.car.CarRepository;
import com.carrental.repository.rental.RentalRepository;
import com.carrental.repository.user.UserRepository;
import com.carrental.security.AuthenticationService;
import com.carrental.service.impl.RentalServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class RentalServiceTest {

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private RentalMapper rentalMapper;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private CarRepository carRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RentalServiceImpl rentalService;

    @Test
    @DisplayName("Create rental - should create rental and return DTO")
    void createRental_ValidRentalDto_ShouldCreateRental() {
        // Given
        RentalDto rentalDto = new RentalDto();
        rentalDto.setUserId(1L);
        rentalDto.setCarId(1L);
        rentalDto.setRentalDate(LocalDate.now());
        rentalDto.setReturnDate(LocalDate.now().plusDays(7));

        Car car = new Car();
        car.setId(1L);
        car.setInventory(5);
        car.setDailyFee(BigDecimal.valueOf(50.00));

        Rental rental = new Rental();
        rental.setId(1L);
        rental.setUserId(1L);
        rental.setCarId(1L);
        rental.setRentalDate(LocalDate.now());
        rental.setReturnDate(LocalDate.now().plusDays(7));

        RentalDto responseDto = new RentalDto();
        responseDto.setId(1L);
        responseDto.setUserId(1L);
        responseDto.setCarId(1L);

        when(userRepository.existsById(1L)).thenReturn(true);
        when(rentalRepository.existsByUserIdAndActualReturnDateIsNull(1L)).thenReturn(false);
        when(carRepository.findById(1L)).thenReturn(Optional.of(car));
        when(rentalMapper.toModel(rentalDto)).thenReturn(rental);
        when(rentalRepository.save(rental)).thenReturn(rental);
        when(rentalMapper.toDto(rental)).thenReturn(responseDto);

        // When
        RentalDto result = rentalService.createRental(rentalDto);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(userRepository).existsById(1L);
        verify(rentalRepository).existsByUserIdAndActualReturnDateIsNull(1L);
        verify(carRepository).findById(1L);
        verify(rentalMapper).toModel(rentalDto);
        verify(rentalRepository).save(rental);
        verify(rentalMapper).toDto(rental);
    }

    @Test
    @DisplayName("Create rental - should throw exception when user not exists")
    void createRental_NonExistingUser_ShouldThrowException() {
        // Given
        RentalDto rentalDto = new RentalDto();
        rentalDto.setUserId(999L);

        when(userRepository.existsById(999L)).thenReturn(false);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> rentalService.createRental(rentalDto));
        assertEquals("User with ID 999 does not exist", exception.getMessage());
        verify(userRepository).existsById(999L);
    }

    @Test
    @DisplayName("Create rental - should throw exception when user has active rental")
    void createRental_UserWithActiveRental_ShouldThrowException() {
        // Given
        RentalDto rentalDto = new RentalDto();
        rentalDto.setUserId(1L);

        when(userRepository.existsById(1L)).thenReturn(true);
        when(rentalRepository.existsByUserIdAndActualReturnDateIsNull(1L)).thenReturn(true);

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> rentalService.createRental(rentalDto));
        assertEquals("User with ID 1 already has an active rental", exception.getMessage());
        verify(userRepository).existsById(1L);
        verify(rentalRepository).existsByUserIdAndActualReturnDateIsNull(1L);
    }

    @Test
    @DisplayName("Create rental - should throw exception when car out of stock")
    void createRental_CarOutOfStock_ShouldThrowException() {
        // Given
        RentalDto rentalDto = new RentalDto();
        rentalDto.setUserId(1L);
        rentalDto.setCarId(1L);

        Car car = new Car();
        car.setId(1L);
        car.setInventory(0);

        when(userRepository.existsById(1L)).thenReturn(true);
        when(rentalRepository.existsByUserIdAndActualReturnDateIsNull(1L)).thenReturn(false);
        when(carRepository.findById(1L)).thenReturn(Optional.of(car));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> rentalService.createRental(rentalDto));
        assertEquals("Car with ID 1 is out of stock", exception.getMessage());
        verify(carRepository).findById(1L);
    }

    @Test
    @DisplayName("Get all rentals - should return list of active rental response DTOs")
    void getAllRentals_WithActiveRentals_ShouldReturnActiveRentals() {
        // Given
        Rental activeRental = new Rental();
        activeRental.setId(1L);
        activeRental.setActualReturnDate(null);

        Rental completedRental = new Rental();
        completedRental.setId(2L);
        completedRental.setActualReturnDate(LocalDate.now());

        RentalResponseDto responseDto = new RentalResponseDto();
        responseDto.setId(1L);

        when(rentalRepository.findAll()).thenReturn(List.of(activeRental, completedRental));
        when(rentalMapper.toResponseDto(activeRental)).thenReturn(responseDto);

        // When
        List<RentalResponseDto> result = rentalService.getAllRentals();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        verify(rentalRepository).findAll();
        verify(rentalMapper).toResponseDto(activeRental);
    }

    @Test
    @DisplayName("Get rentals by user and status - should return filtered rentals")
    void getRentalsByUserAndStatus_WithUserIdAndActive_ShouldReturnFilteredRentals() {
        // Given
        Long userId = 1L;

        Rental rental = new Rental();
        rental.setId(1L);
        rental.setUserId(userId);

        RentalResponseDto responseDto = new RentalResponseDto();
        responseDto.setId(1L);
        responseDto.setUserId(userId);

        when(rentalRepository.findAll(any(Specification.class))).thenReturn(List.of(rental));
        when(rentalMapper.toResponseDto(rental)).thenReturn(responseDto);

        // When
        Boolean isActive = true;
        List<RentalResponseDto> result = rentalService.getRentalsByUserAndStatus(userId, isActive);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(userId, result.get(0).getUserId());
        verify(rentalRepository).findAll(any(Specification.class));
        verify(rentalMapper).toResponseDto(rental);
    }

    @Test
    @DisplayName("Get specific rental - should return rental for authenticated user")
    void getSpecificRental_AuthenticatedUser_ShouldReturnRental() {
        // Given
        User user = new User();
        user.setId(1L);

        Rental rental = new Rental();
        rental.setId(1L);
        rental.setUserId(1L);

        RentalResponseDto responseDto = new RentalResponseDto();
        responseDto.setId(1L);
        responseDto.setUserId(1L);

        when(authenticationService.getAuthenticatedUser()).thenReturn(user);
        when(rentalRepository.findByUserId(1L)).thenReturn(Optional.of(rental));
        when(rentalMapper.toResponseDto(rental)).thenReturn(responseDto);

        // When
        RentalResponseDto result = rentalService.getSpecificRental();

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(authenticationService).getAuthenticatedUser();
        verify(rentalRepository).findByUserId(1L);
        verify(rentalMapper).toResponseDto(rental);
    }

    @Test
    @DisplayName("Return rental - should update rental with actual return date")
    void returnRentalDate_ValidRequest_ShouldUpdateRental() {
        // Given
        User user = new User();
        user.setId(1L);

        RentalReturnRequestDto requestDto = new RentalReturnRequestDto();
        requestDto.setActualReturnDate(LocalDate.now());

        Rental rental = new Rental();
        rental.setId(1L);
        rental.setUserId(1L);
        rental.setCarId(1L);
        rental.setActualReturnDate(null);

        Car car = new Car();
        car.setId(1L);
        car.setInventory(0);

        RentalResponseDto responseDto = new RentalResponseDto();
        responseDto.setId(1L);

        when(authenticationService.getAuthenticatedUser()).thenReturn(user);
        when(rentalRepository.findByUserIdAndActualReturnDateIsNull(1L))
                .thenReturn(Optional.of(rental));
        when(carRepository.findById(1L)).thenReturn(Optional.of(car));
        when(rentalRepository.save(rental)).thenReturn(rental);
        when(rentalMapper.toResponseDto(rental)).thenReturn(responseDto);

        // When
        RentalResponseDto result = rentalService.returnRentalDate(requestDto);

        // Then
        assertNotNull(result);
        assertEquals(LocalDate.now(), rental.getActualReturnDate());
        assertEquals(1, car.getInventory());
        verify(authenticationService).getAuthenticatedUser();
        verify(rentalRepository).findByUserIdAndActualReturnDateIsNull(1L);
        verify(carRepository).findById(1L);
        verify(rentalRepository).save(rental);
        verify(rentalMapper).toResponseDto(rental);
    }
}
