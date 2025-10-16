package com.hapangama.medibackend.repository;

import com.hapangama.medibackend.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByPatientIdOrderByTimestampDesc(Long patientId);
}
