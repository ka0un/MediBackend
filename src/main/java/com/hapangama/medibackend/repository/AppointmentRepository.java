package com.hapangama.medibackend.repository;

import com.hapangama.medibackend.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatientId(Long patientId);
    List<Appointment> findByProviderId(Long providerId);
    Optional<Appointment> findByConfirmationNumber(String confirmationNumber);
}
