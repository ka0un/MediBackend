package com.hapangama.medibackend.repository;

import com.hapangama.medibackend.model.HealthcareProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HealthcareProviderRepository extends JpaRepository<HealthcareProvider, Long> {
    List<HealthcareProvider> findBySpecialty(String specialty);
}
