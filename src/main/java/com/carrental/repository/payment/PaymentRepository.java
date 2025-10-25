package com.carrental.repository.payment;

import com.carrental.model.Payment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findBySessionId(String sessionId);

    List<Payment> findByRentalId(Long rentalId);

    @Query("SELECT p FROM Payment p WHERE p.status = 'PENDING'")
    List<Payment> findExpiredPendingPayments();
}
