package com.hapangama.medibackend.controller;

import com.hapangama.medibackend.dto.CreateUtilizationReportRequest;
import com.hapangama.medibackend.dto.UpdateUtilizationReportRequest;
import com.hapangama.medibackend.dto.UtilizationReportResponse;
import com.hapangama.medibackend.service.UtilizationReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Utilization Report CRUD operations
 * Follows Single Responsibility Principle - handles only HTTP requests/responses
 */
@RestController
@RequestMapping("/api/analytics/utilization-reports")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UtilizationReportController {

    private final UtilizationReportService utilizationReportService;

    /**
     * CREATE - Generate a new utilization report
     * POST /api/analytics/utilization-reports
     */
    @PostMapping
    public ResponseEntity<UtilizationReportResponse> createReport(
            @RequestBody CreateUtilizationReportRequest request) {
        UtilizationReportResponse response = utilizationReportService.createReport(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * READ - Get all utilization reports
     * GET /api/analytics/utilization-reports
     */
    @GetMapping
    public ResponseEntity<List<UtilizationReportResponse>> getAllReports() {
        List<UtilizationReportResponse> reports = utilizationReportService.getAllReports();
        return ResponseEntity.ok(reports);
    }

    /**
     * READ - Get a specific utilization report by ID
     * GET /api/analytics/utilization-reports/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<UtilizationReportResponse> getReportById(@PathVariable Long id) {
        UtilizationReportResponse report = utilizationReportService.getReportById(id);
        return ResponseEntity.ok(report);
    }

    /**
     * UPDATE - Modify an existing utilization report
     * PUT /api/analytics/utilization-reports/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<UtilizationReportResponse> updateReport(
            @PathVariable Long id,
            @RequestBody UpdateUtilizationReportRequest request) {
        UtilizationReportResponse response = utilizationReportService.updateReport(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE - Remove a utilization report
     * DELETE /api/analytics/utilization-reports/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteReport(@PathVariable Long id) {
        utilizationReportService.deleteReport(id);
        return ResponseEntity.ok(Map.of("message", "Utilization report deleted successfully"));
    }

    /**
     * READ - Filter reports by department
     * GET /api/analytics/utilization-reports/filter/department/{department}
     */
    @GetMapping("/filter/department/{department}")
    public ResponseEntity<List<UtilizationReportResponse>> getReportsByDepartment(
            @PathVariable String department) {
        List<UtilizationReportResponse> reports = utilizationReportService.getReportsByDepartment(department);
        return ResponseEntity.ok(reports);
    }

    /**
     * READ - Filter reports by doctor
     * GET /api/analytics/utilization-reports/filter/doctor/{doctor}
     */
    @GetMapping("/filter/doctor/{doctor}")
    public ResponseEntity<List<UtilizationReportResponse>> getReportsByDoctor(
            @PathVariable String doctor) {
        List<UtilizationReportResponse> reports = utilizationReportService.getReportsByDoctor(doctor);
        return ResponseEntity.ok(reports);
    }
}
