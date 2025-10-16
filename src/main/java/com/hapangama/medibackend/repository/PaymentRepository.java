package com.hapangama.medibackend.repository;

import com.hapangama.medibackend.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    // Reporting queries
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'COMPLETED' " +
           "AND p.paymentDateTime BETWEEN :startDate AND :endDate")
    BigDecimal sumCompletedPaymentsByDateRange(@Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p JOIN p.appointment a JOIN a.provider pr " +
           "WHERE pr.specialty = :specialty AND p.status = 'COMPLETED' " +
           "AND p.paymentDateTime BETWEEN :startDate AND :endDate")
    BigDecimal sumCompletedPaymentsBySpecialtyAndDateRange(@Param("specialty") String specialty,
                                                             @Param("startDate") LocalDateTime startDate,
                                                             @Param("endDate") LocalDateTime endDate);
}
