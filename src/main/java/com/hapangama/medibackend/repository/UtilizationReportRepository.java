package com.hapangama.medibackend.repository;

import com.hapangama.medibackend.model.UtilizationReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface UtilizationReportRepository extends JpaRepository<UtilizationReport, Long> {
    
    List<UtilizationReport> findByDepartment(String department);
    
    List<UtilizationReport> findByDoctor(String doctor);
    
    List<UtilizationReport> findByServiceCategory(String serviceCategory);
    
    @Query("SELECT u FROM UtilizationReport u WHERE u.startDate >= :startDate AND u.endDate <= :endDate")
    List<UtilizationReport> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    List<UtilizationReport> findAllByOrderByCreatedAtDesc();
}
