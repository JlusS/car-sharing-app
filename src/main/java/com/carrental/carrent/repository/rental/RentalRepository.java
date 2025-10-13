package com.carrental.carrent.repository.rental;

import com.carrental.carrent.model.Rental;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RentalRepository extends
        JpaRepository<Rental, Long>, JpaSpecificationExecutor<Rental> {
    Optional<Rental> findByUserId(Long userId);

    Optional<Rental> findByUserIdAndActualReturnDateIsNull(Long userId);

    boolean existsByUserIdAndActualReturnDateIsNull(Long userId);

    @Query("SELECT r FROM Rental r WHERE r.actualReturnDate IS "
            + "NULL AND r.returnDate < :currentDate")
    List<Rental> findOverdueActiveRentals(@Param("currentDate") LocalDate now);
}
