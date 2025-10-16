package com.hapangama.medibackend.repository;

import com.hapangama.medibackend.model.TestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestResultRepository extends JpaRepository<TestResult, Long> {
    List<TestResult> findByPatientIdOrderByTestDateDesc(Long patientId);
}
