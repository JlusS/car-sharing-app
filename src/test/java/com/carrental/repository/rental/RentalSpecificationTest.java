package com.carrental.repository.rental;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.carrental.model.Rental;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RentalSpecificationTest {

    @Autowired
    private RentalRepository rentalRepository;

    @Test
    @DisplayName("By user ID - should create specification for user ID filter")
    void byUserId_WithValidUserId_ShouldCreateSpecification() {
        // Given
        Long userId = 1L;

        // When
        Specification<Rental> spec = RentalSpecification.byUserId(userId);

        // Then
        assertNotNull(spec);
    }

    @Test
    @DisplayName("By user ID - should filter rentals by specific user ID")
    void byUserId_WithValidUserId_ShouldFilterRentals() {
        // Given
        Rental rental1 = new Rental();
        rental1.setUserId(1L);
        rental1.setCarId(1L);
        rental1.setRentalDate(LocalDate.now());
        rental1.setReturnDate(LocalDate.now().plusDays(7));

        Rental rental2 = new Rental();
        rental2.setUserId(2L);
        rental2.setCarId(2L);
        rental2.setRentalDate(LocalDate.now());
        rental2.setReturnDate(LocalDate.now().plusDays(5));

        rentalRepository.save(rental1);
        rentalRepository.save(rental2);

        Specification<Rental> spec = RentalSpecification.byUserId(1L);

        // When
        List<Rental> result = rentalRepository.findAll(spec);

        // Then
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getUserId());
        assertEquals(1L, result.get(0).getCarId());
    }

    @Test
    @DisplayName("By user ID - should return empty list for non-existing user ID")
    void byUserId_WithNonExistingUserId_ShouldReturnEmptyList() {
        // Given
        Rental rental = new Rental();
        rental.setUserId(1L);
        rental.setCarId(1L);
        rental.setRentalDate(LocalDate.now());
        rental.setReturnDate(LocalDate.now().plusDays(7));
        rentalRepository.save(rental);

        Specification<Rental> spec = RentalSpecification.byUserId(999L);

        // When
        List<Rental> result = rentalRepository.findAll(spec);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Is active - should create specification for active rentals when true")
    void isActive_WithTrue_ShouldCreateActiveSpecification() {
        // Given
        Boolean active = true;

        // When
        Specification<Rental> spec = RentalSpecification.isActive(active);

        // Then
        assertNotNull(spec);
    }

    @Test
    @DisplayName("Is active - should filter only active rentals when true")
    void isActive_WithTrue_ShouldFilterActiveRentals() {
        // Given
        Rental activeRental = new Rental();
        activeRental.setUserId(1L);
        activeRental.setCarId(1L);
        activeRental.setRentalDate(LocalDate.now());
        activeRental.setReturnDate(LocalDate.now().plusDays(7));
        activeRental.setActualReturnDate(null);

        Rental completedRental = new Rental();
        completedRental.setUserId(2L);
        completedRental.setCarId(2L);
        completedRental.setRentalDate(LocalDate.now().minusDays(10));
        completedRental.setReturnDate(LocalDate.now().minusDays(3));
        completedRental.setActualReturnDate(LocalDate.now().minusDays(2));

        rentalRepository.save(activeRental);
        rentalRepository.save(completedRental);

        Specification<Rental> spec = RentalSpecification.isActive(true);

        // When
        List<Rental> result = rentalRepository.findAll(spec);

        // Then
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getUserId());
        assertNull(result.get(0).getActualReturnDate());
    }

    @Test
    @DisplayName("Is active - should create specification for inactive rentals when false")
    void isActive_WithFalse_ShouldCreateInactiveSpecification() {
        // Given
        Boolean active = false;

        // When
        Specification<Rental> spec = RentalSpecification.isActive(active);

        // Then
        assertNotNull(spec);
    }

    @Test
    @DisplayName("Is active - should filter only completed rentals when false")
    void isActive_WithFalse_ShouldFilterInactiveRentals() {
        // Given
        Rental activeRental = new Rental();
        activeRental.setUserId(1L);
        activeRental.setCarId(1L);
        activeRental.setRentalDate(LocalDate.now());
        activeRental.setReturnDate(LocalDate.now().plusDays(7));
        activeRental.setActualReturnDate(null);

        Rental completedRental = new Rental();
        completedRental.setUserId(2L);
        completedRental.setCarId(2L);
        completedRental.setRentalDate(LocalDate.now().minusDays(10));
        completedRental.setReturnDate(LocalDate.now().minusDays(3));
        completedRental.setActualReturnDate(LocalDate.now().minusDays(2));

        rentalRepository.save(activeRental);
        rentalRepository.save(completedRental);

        Specification<Rental> spec = RentalSpecification.isActive(false);

        // When
        List<Rental> result = rentalRepository.findAll(spec);

        // Then
        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getUserId());
        assertNotNull(result.get(0).getActualReturnDate());
    }

    @Test
    @DisplayName("Is active - should return null when active is null")
    void isActive_WithNull_ShouldReturnNull() {
        // Given
        Boolean active = null;

        // When
        Specification<Rental> spec = RentalSpecification.isActive(active);

        // Then
        assertNull(spec);
    }

    @Test
    @DisplayName("Combine specifications - should filter by user ID and active status")
    void combineSpecifications_WithUserIdAndActive_ShouldFilterCorrectly() {
        // Given
        Rental activeRentalUser1 = new Rental();
        activeRentalUser1.setUserId(1L);
        activeRentalUser1.setCarId(1L);
        activeRentalUser1.setRentalDate(LocalDate.now());
        activeRentalUser1.setReturnDate(LocalDate.now().plusDays(7));
        activeRentalUser1.setActualReturnDate(null);

        Rental completedRentalUser1 = new Rental();
        completedRentalUser1.setUserId(1L);
        completedRentalUser1.setCarId(2L);
        completedRentalUser1.setRentalDate(LocalDate.now().minusDays(10));
        completedRentalUser1.setReturnDate(LocalDate.now().minusDays(3));
        completedRentalUser1.setActualReturnDate(LocalDate.now().minusDays(2));

        Rental activeRentalUser2 = new Rental();
        activeRentalUser2.setUserId(2L);
        activeRentalUser2.setCarId(3L);
        activeRentalUser2.setRentalDate(LocalDate.now());
        activeRentalUser2.setReturnDate(LocalDate.now().plusDays(5));
        activeRentalUser2.setActualReturnDate(null);

        rentalRepository.save(activeRentalUser1);
        rentalRepository.save(completedRentalUser1);
        rentalRepository.save(activeRentalUser2);

        Specification<Rental> userSpec = RentalSpecification.byUserId(1L);
        Specification<Rental> activeSpec = RentalSpecification.isActive(true);
        Specification<Rental> combinedSpec = userSpec.and(activeSpec);

        // When
        List<Rental> result = rentalRepository.findAll(combinedSpec);

        // Then
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getUserId());
        assertEquals(1L, result.get(0).getCarId());
        assertNull(result.get(0).getActualReturnDate());
    }

    @Test
    @DisplayName("Combine specifications - should filter by user ID and inactive status")
    void combineSpecifications_WithUserIdAndInactive_ShouldFilterCorrectly() {
        // Given
        Rental activeRentalUser1 = new Rental();
        activeRentalUser1.setUserId(1L);
        activeRentalUser1.setCarId(1L);
        activeRentalUser1.setRentalDate(LocalDate.now());
        activeRentalUser1.setReturnDate(LocalDate.now().plusDays(7));
        activeRentalUser1.setActualReturnDate(null);

        Rental completedRentalUser1 = new Rental();
        completedRentalUser1.setUserId(1L);
        completedRentalUser1.setCarId(2L);
        completedRentalUser1.setRentalDate(LocalDate.now().minusDays(10));
        completedRentalUser1.setReturnDate(LocalDate.now().minusDays(3));
        completedRentalUser1.setActualReturnDate(LocalDate.now().minusDays(2));

        rentalRepository.save(activeRentalUser1);
        rentalRepository.save(completedRentalUser1);

        Specification<Rental> userSpec = RentalSpecification.byUserId(1L);
        Specification<Rental> inactiveSpec = RentalSpecification.isActive(false);
        Specification<Rental> combinedSpec = userSpec.and(inactiveSpec);

        // When
        List<Rental> result = rentalRepository.findAll(combinedSpec);

        // Then
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getUserId());
        assertEquals(2L, result.get(0).getCarId());
        assertNotNull(result.get(0).getActualReturnDate());
    }

    @Test
    @DisplayName("Multiple active rentals for same user - should return all active rentals")
    void isActive_WithMultipleActiveRentals_ShouldReturnAllActive() {
        // Given
        Rental activeRental1 = new Rental();
        activeRental1.setUserId(1L);
        activeRental1.setCarId(1L);
        activeRental1.setRentalDate(LocalDate.now());
        activeRental1.setReturnDate(LocalDate.now().plusDays(7));
        activeRental1.setActualReturnDate(null);

        Rental activeRental2 = new Rental();
        activeRental2.setUserId(1L);
        activeRental2.setCarId(2L);
        activeRental2.setRentalDate(LocalDate.now());
        activeRental2.setReturnDate(LocalDate.now().plusDays(5));
        activeRental2.setActualReturnDate(null);

        Rental completedRental = new Rental();
        completedRental.setUserId(1L);
        completedRental.setCarId(3L);
        completedRental.setRentalDate(LocalDate.now().minusDays(10));
        completedRental.setReturnDate(LocalDate.now().minusDays(3));
        completedRental.setActualReturnDate(LocalDate.now().minusDays(2));

        rentalRepository.save(activeRental1);
        rentalRepository.save(activeRental2);
        rentalRepository.save(completedRental);

        Specification<Rental> spec = RentalSpecification.isActive(true);

        // When
        List<Rental> result = rentalRepository.findAll(spec);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(rental -> rental.getActualReturnDate() == null));
        assertTrue(result.stream().anyMatch(rental -> rental.getCarId().equals(1L)));
        assertTrue(result.stream().anyMatch(rental -> rental.getCarId().equals(2L)));
    }

    @Test
    @DisplayName("Empty database - should return empty list for any specification")
    void anySpecification_WithEmptyDatabase_ShouldReturnEmptyList() {
        // Given
        Specification<Rental> userSpec = RentalSpecification.byUserId(1L);
        Specification<Rental> activeSpec = RentalSpecification.isActive(true);

        // When
        List<Rental> userResult = rentalRepository.findAll(userSpec);
        List<Rental> activeResult = rentalRepository.findAll(activeSpec);

        // Then
        assertTrue(userResult.isEmpty());
        assertTrue(activeResult.isEmpty());
    }
}
