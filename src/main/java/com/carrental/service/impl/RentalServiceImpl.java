package com.carrental.service.impl;

import com.carrental.dto.rental.RentalDto;
import com.carrental.dto.rental.RentalResponseDto;
import com.carrental.dto.rental.RentalReturnRequestDto;
import com.carrental.exception.RentalNotFoundException;
import com.carrental.mapper.RentalMapper;
import com.carrental.model.Car;
import com.carrental.model.Rental;
import com.carrental.model.User;
import com.carrental.repository.car.CarRepository;
import com.carrental.repository.rental.RentalRepository;
import com.carrental.repository.rental.RentalSpecification;
import com.carrental.repository.user.UserRepository;
import com.carrental.security.AuthenticationService;
import com.carrental.service.RentalService;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class RentalServiceImpl implements RentalService {
    private final RentalRepository rentalRepository;
    private final RentalMapper rentalMapper;
    private final AuthenticationService authenticationService;
    private final CarRepository carRepository;
    private final UserRepository userRepository;

    @Override
    public RentalDto createRental(RentalDto rentalDto) {
        if (!userRepository.existsById(rentalDto.getUserId())) {
            throw new IllegalArgumentException("User with ID "
                    + rentalDto.getUserId() + " does not exist");
        }

        boolean hasActiveRental = rentalRepository.existsByUserIdAndActualReturnDateIsNull(
                rentalDto.getUserId());
        if (hasActiveRental) {
            throw new IllegalStateException("User with ID "
                    + rentalDto.getUserId()
                    + " already has an active rental");
        }

        Car car = carRepository.findById(rentalDto.getCarId())
                .orElseThrow(() -> new IllegalArgumentException("Car with ID "
                        + rentalDto.getCarId() + " not found"));

        if (car.getInventory() <= 0) {
            throw new IllegalStateException("Car with ID "
                    + car.getId() + " is out of stock");
        }

        if (rentalDto.getReturnDate().isBefore(rentalDto.getRentalDate())) {
            throw new IllegalArgumentException("Return date cannot be before rental date");
        }

        car.setInventory(car.getInventory() - 1);
        carRepository.save(car);

        Rental rental = rentalMapper.toModel(rentalDto);
        Rental savedRental = rentalRepository.save(rental);

        return rentalMapper.toDto(savedRental);
    }

    @Override
    public List<RentalResponseDto> getAllRentals() {
        return rentalRepository.findAll().stream()
                .filter(rental -> rental.getActualReturnDate() == null)
                .map(rentalMapper::toResponseDto)
                .toList();
    }

    @Override
    public List<RentalResponseDto> getRentalsByUserAndStatus(Long userId, Boolean isActive) {
        Specification<Rental> spec = RentalSpecification.byUserId(userId);

        if (isActive != null) {
            spec = spec.and(RentalSpecification.isActive(isActive));
        }

        return rentalRepository.findAll(spec).stream()
                .map(rentalMapper::toResponseDto)
                .toList();
    }

    @Override
    public RentalResponseDto getSpecificRental() {
        User authenticatedUser = authenticationService.getAuthenticatedUser();
        Rental rental = rentalRepository.findByUserId(authenticatedUser.getId())
                .orElseThrow(() -> new RentalNotFoundException(
                        "Rental not found for user id: "
                                + authenticatedUser.getId()));;
        return rentalMapper.toResponseDto(rental);
    }

    @Override
    public RentalResponseDto returnRentalDate(RentalReturnRequestDto requestDto) {
        User authenticatedUser = authenticationService.getAuthenticatedUser();

        Rental rental = rentalRepository
                .findByUserIdAndActualReturnDateIsNull(authenticatedUser.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No active rental found for user ID: " + authenticatedUser.getId()));

        Car car = carRepository.findById(rental.getCarId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Car not found with ID: " + rental.getCarId()));

        car.setInventory(car.getInventory() + 1);
        carRepository.save(car);

        rental.setActualReturnDate(requestDto.getActualReturnDate());
        rentalRepository.save(rental);

        return rentalMapper.toResponseDto(rental);
    }

}
