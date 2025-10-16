package com.hapangama.medibackend.repository;

import com.hapangama.medibackend.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByDigitalHealthCardNumber(String digitalHealthCardNumber);
    Optional<Patient> findByEmail(String email);
    Optional<Patient> findByUser(com.hapangama.medibackend.model.User user);
    boolean existsByEmail(String email);
    boolean existsByDigitalHealthCardNumber(String digitalHealthCardNumber);
}
