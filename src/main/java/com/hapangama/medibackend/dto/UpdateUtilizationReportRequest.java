package com.hapangama.medibackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUtilizationReportRequest {
    private String reportName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String department;
    private String doctor;
    private String serviceCategory;
    private String notes;
}
