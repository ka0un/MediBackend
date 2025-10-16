package com.hapangama.medibackend.repository;

import com.hapangama.medibackend.model.MedicalRecordAccessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalRecordAccessLogRepository extends JpaRepository<MedicalRecordAccessLog, Long> {
    List<MedicalRecordAccessLog> findByPatientIdOrderByAccessTimestampDesc(Long patientId);
    List<MedicalRecordAccessLog> findByStaffIdOrderByAccessTimestampDesc(String staffId);
}
