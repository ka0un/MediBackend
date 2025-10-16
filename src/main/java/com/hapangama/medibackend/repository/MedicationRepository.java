package com.hapangama.medibackend.repository;

import com.hapangama.medibackend.model.Medication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicationRepository extends JpaRepository<Medication, Long> {
    List<Medication> findByPatientIdAndActiveTrue(Long patientId);
    List<Medication> findByPatientId(Long patientId);
}
