package com.hapangama.medibackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportFilterRequest {
    private String hospital;
    private String department;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reportType; // VISITS, PAYMENTS, DEPARTMENT_BREAKDOWN, etc.
    private String granularity; // DAILY, WEEKLY, MONTHLY
}
