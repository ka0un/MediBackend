package com.hapangama.medibackend.repository;

import com.hapangama.medibackend.model.Vaccination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VaccinationRepository extends JpaRepository<Vaccination, Long> {
    List<Vaccination> findByPatientIdOrderByVaccinationDateDesc(Long patientId);
}
