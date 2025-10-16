package com.hapangama.medibackend.repository;

import com.hapangama.medibackend.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatientId(Long patientId);
    List<Appointment> findByProviderId(Long providerId);
    Optional<Appointment> findByConfirmationNumber(String confirmationNumber);
    
    // Reporting queries
    @Query("SELECT a FROM Appointment a WHERE a.bookingDateTime BETWEEN :startDate AND :endDate")
    List<Appointment> findByBookingDateTimeBetween(@Param("startDate") LocalDateTime startDate, 
                                                     @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT a FROM Appointment a JOIN a.provider p WHERE p.hospitalName = :hospital " +
           "AND a.bookingDateTime BETWEEN :startDate AND :endDate")
    List<Appointment> findByHospitalAndDateRange(@Param("hospital") String hospital,
                                                   @Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT a FROM Appointment a JOIN a.provider p WHERE p.specialty = :specialty " +
           "AND a.bookingDateTime BETWEEN :startDate AND :endDate")
    List<Appointment> findBySpecialtyAndDateRange(@Param("specialty") String specialty,
                                                    @Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT a FROM Appointment a JOIN a.provider p WHERE p.hospitalName = :hospital " +
           "AND p.specialty = :specialty AND a.bookingDateTime BETWEEN :startDate AND :endDate")
    List<Appointment> findByHospitalSpecialtyAndDateRange(@Param("hospital") String hospital,
                                                            @Param("specialty") String specialty,
                                                            @Param("startDate") LocalDateTime startDate,
                                                            @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.status = :status " +
           "AND a.bookingDateTime BETWEEN :startDate AND :endDate")
    Long countByStatusAndDateRange(@Param("status") Appointment.AppointmentStatus status,
                                    @Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);
}
