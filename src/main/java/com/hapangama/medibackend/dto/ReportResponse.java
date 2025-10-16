package com.hapangama.medibackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportResponse {
    private ReportKPIs kpis;
    private List<DailyVisit> dailyVisits;
    private List<DepartmentBreakdown> departmentBreakdowns;
    private ReportFilterRequest filters;
    private LocalDateTime generatedAt;
    private String message; // For "No data" scenarios
}
