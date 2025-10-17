package com.hapangama.medibackend.dto;

import com.hapangama.medibackend.model.UtilizationReport;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UtilizationReportResponse {
    private Long id;
    private String reportName;
    private LocalDate reportDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private String department;
    private String doctor;
    private String serviceCategory;
    private Integer totalServices;
    private Integer totalPatients;
    private Double averageUtilization;
    private String peakHours;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UtilizationReportResponse fromEntity(UtilizationReport report) {
        return new UtilizationReportResponse(
            report.getId(),
            report.getReportName(),
            report.getReportDate(),
            report.getStartDate(),
            report.getEndDate(),
            report.getDepartment(),
            report.getDoctor(),
            report.getServiceCategory(),
            report.getTotalServices(),
            report.getTotalPatients(),
            report.getAverageUtilization(),
            report.getPeakHours(),
            report.getNotes(),
            report.getCreatedAt(),
            report.getUpdatedAt()
        );
    }
}
