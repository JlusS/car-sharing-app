package com.carrental.carrent.repository.rental;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.carrental.carrent.model.Rental;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RentalRepositoryTest {

    @Autowired
    private RentalRepository rentalRepository;

    @Test
    @DisplayName("Find by user ID - should return rental when user has rentals")
    void findByUserId_ExistingUser_ShouldReturnRental() {
        // Given
        Rental rental = new Rental();
        rental.setUserId(1L);
        rental.setCarId(1L);
        rental.setRentalDate(LocalDate.now());
        rental.setReturnDate(LocalDate.now().plusDays(7));
        rental.setActualReturnDate(null);
        rentalRepository.save(rental);

        // When
        Optional<Rental> foundRental = rentalRepository.findByUserId(1L);

        // Then
        assertTrue(foundRental.isPresent());
        assertEquals(1L, foundRental.get().getUserId());
        assertEquals(1L, foundRental.get().getCarId());
        assertEquals(LocalDate.now(), foundRental.get().getRentalDate());
        assertNull(foundRental.get().getActualReturnDate());
    }

    @Test
    @DisplayName("Find by user ID - should return empty when user has no rentals")
    void findByUserId_NonExistingUser_ShouldReturnEmpty() {
        // When
        Optional<Rental> foundRental = rentalRepository.findByUserId(999L);

        // Then
        assertTrue(foundRental.isEmpty());
    }

    @Test
    @DisplayName("Find by user ID with null actual return date - should return active rental")
    void findByUserIdAndActualReturnDateIsNull_ActiveRental_ShouldReturnRental() {
        // Given
        Rental activeRental = new Rental();
        activeRental.setUserId(1L);
        activeRental.setCarId(1L);
        activeRental.setRentalDate(LocalDate.now());
        activeRental.setReturnDate(LocalDate.now().plusDays(7));
        activeRental.setActualReturnDate(null);

        Rental completedRental = new Rental();
        completedRental.setUserId(1L);
        completedRental.setCarId(2L);
        completedRental.setRentalDate(LocalDate.now().minusDays(14));
        completedRental.setReturnDate(LocalDate.now().minusDays(7));
        completedRental.setActualReturnDate(LocalDate.now().minusDays(7));

        rentalRepository.save(activeRental);
        rentalRepository.save(completedRental);

        // When
        Optional<Rental> foundRental = rentalRepository.findByUserIdAndActualReturnDateIsNull(1L);

        // Then
        assertTrue(foundRental.isPresent());
        assertNull(foundRental.get().getActualReturnDate());
        assertEquals(1L, foundRental.get().getCarId());
    }

    @Test
    @DisplayName("Find by user ID with null actual return date "
            + "- should return empty when no active rentals")
    void findByUserIdAndActualReturnDateIsNull_NoActiveRental_ShouldReturnEmpty() {
        // Given
        Rental completedRental = new Rental();
        completedRental.setUserId(1L);
        completedRental.setCarId(1L);
        completedRental.setRentalDate(LocalDate.now().minusDays(14));
        completedRental.setReturnDate(LocalDate.now().minusDays(7));
        completedRental.setActualReturnDate(LocalDate.now().minusDays(7));
        rentalRepository.save(completedRental);

        // When
        Optional<Rental> foundRental = rentalRepository.findByUserIdAndActualReturnDateIsNull(1L);

        // Then
        assertTrue(foundRental.isEmpty());
    }

    @Test
    @DisplayName("Exists by user ID and null actual return date "
            + "- should return true for active rental")
    void existsByUserIdAndActualReturnDateIsNull_ActiveRental_ShouldReturnTrue() {
        // Given
        Rental activeRental = new Rental();
        activeRental.setUserId(1L);
        activeRental.setCarId(1L);
        activeRental.setRentalDate(LocalDate.now());
        activeRental.setReturnDate(LocalDate.now().plusDays(7));
        activeRental.setActualReturnDate(null);
        rentalRepository.save(activeRental);

        // When
        boolean exists = rentalRepository.existsByUserIdAndActualReturnDateIsNull(1L);

        // Then
        assertTrue(exists);
    }

    @Test
    @DisplayName("Exists by user ID and null actual return date "
            + "- should return false when no active rental")
    void existsByUserIdAndActualReturnDateIsNull_NoActiveRental_ShouldReturnFalse() {
        // Given
        Rental completedRental = new Rental();
        completedRental.setUserId(1L);
        completedRental.setCarId(1L);
        completedRental.setRentalDate(LocalDate.now().minusDays(14));
        completedRental.setReturnDate(LocalDate.now().minusDays(7));
        completedRental.setActualReturnDate(LocalDate.now().minusDays(7));
        rentalRepository.save(completedRental);

        // When
        boolean exists = rentalRepository.existsByUserIdAndActualReturnDateIsNull(1L);

        // Then
        assertFalse(exists);
    }

    @Test
    @DisplayName("Find overdue active rentals - should return rentals with passed return date")
    void findOverdueActiveRentals_WithOverdueRentals_ShouldReturnOverdueRentals() {
        // Given
        LocalDate today = LocalDate.now();

        Rental overdueRental = new Rental();
        overdueRental.setUserId(1L);
        overdueRental.setCarId(1L);
        overdueRental.setRentalDate(today.minusDays(10));
        overdueRental.setReturnDate(today.minusDays(3));
        overdueRental.setActualReturnDate(null);

        Rental activeRental = new Rental();
        activeRental.setUserId(2L);
        activeRental.setCarId(2L);
        activeRental.setRentalDate(today.minusDays(2));
        activeRental.setReturnDate(today.plusDays(5));
        activeRental.setActualReturnDate(null);

        Rental completedRental = new Rental();
        completedRental.setUserId(3L);
        completedRental.setCarId(3L);
        completedRental.setRentalDate(today.minusDays(10));
        completedRental.setReturnDate(today.minusDays(3));
        completedRental.setActualReturnDate(today.minusDays(2));

        rentalRepository.save(overdueRental);
        rentalRepository.save(activeRental);
        rentalRepository.save(completedRental);

        // When
        List<Rental> overdueRentals = rentalRepository.findOverdueActiveRentals(today);

        // Then
        assertEquals(1, overdueRentals.size());
        assertEquals(1L, overdueRentals.get(0).getUserId());
        assertNull(overdueRentals.get(0).getActualReturnDate());
        assertTrue(overdueRentals.get(0).getReturnDate().isBefore(today));
    }

    @Test
    @DisplayName("Find overdue active rentals - should return empty when no overdue rentals")
    void findOverdueActiveRentals_NoOverdueRentals_ShouldReturnEmpty() {
        // Given
        LocalDate today = LocalDate.now();

        Rental activeRental = new Rental();
        activeRental.setUserId(1L);
        activeRental.setCarId(1L);
        activeRental.setRentalDate(today.minusDays(2));
        activeRental.setReturnDate(today.plusDays(5));
        activeRental.setActualReturnDate(null);

        rentalRepository.save(activeRental);

        // When
        List<Rental> overdueRentals = rentalRepository.findOverdueActiveRentals(today);

        // Then
        assertTrue(overdueRentals.isEmpty());
    }

    @Test
    @DisplayName("Save rental - should save and return rental with generated ID")
    void save_ValidRental_ShouldSaveRental() {
        // Given
        Rental rental = new Rental();
        rental.setUserId(1L);
        rental.setCarId(1L);
        rental.setRentalDate(LocalDate.now());
        rental.setReturnDate(LocalDate.now().plusDays(7));
        rental.setActualReturnDate(null);

        // When
        Rental savedRental = rentalRepository.save(rental);

        // Then
        assertNotNull(savedRental);
        assertNotNull(savedRental.getId());
        assertEquals(1L, savedRental.getUserId());
        assertEquals(1L, savedRental.getCarId());
        assertEquals(LocalDate.now(), savedRental.getRentalDate());
        assertEquals(LocalDate.now().plusDays(7), savedRental.getReturnDate());
        assertNull(savedRental.getActualReturnDate());
    }

    @Test
    @DisplayName("Update rental with actual return date - should mark rental as completed")
    void updateRental_WithActualReturnDate_ShouldCompleteRental() {
        // Given
        Rental rental = new Rental();
        rental.setUserId(1L);
        rental.setCarId(1L);
        rental.setRentalDate(LocalDate.now().minusDays(5));
        rental.setReturnDate(LocalDate.now().plusDays(2));
        rental.setActualReturnDate(null);
        Rental savedRental = rentalRepository.save(rental);

        // When
        savedRental.setActualReturnDate(LocalDate.now());
        Rental updatedRental = rentalRepository.save(savedRental);

        // Then
        assertNotNull(updatedRental.getActualReturnDate());
        assertEquals(LocalDate.now(), updatedRental.getActualReturnDate());
    }

    @Test
    @DisplayName("Find all rentals - should return all saved rentals")
    void findAll_WithSavedRentals_ShouldReturnAllRentals() {
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

        // When
        List<Rental> allRentals = rentalRepository.findAll();

        // Then
        assertEquals(2, allRentals.size());
    }
}
