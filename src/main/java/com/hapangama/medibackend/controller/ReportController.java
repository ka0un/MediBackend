package com.hapangama.medibackend.controller;

import com.hapangama.medibackend.dto.ExportRequest;
import com.hapangama.medibackend.dto.ReportFilterRequest;
import com.hapangama.medibackend.dto.ReportResponse;
import com.hapangama.medibackend.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReportController {

    private final ReportService reportService;

    @GetMapping
    public ResponseEntity<ReportResponse> generateReport(
            @RequestParam(required = false) String hospital,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String reportType,
            @RequestParam(required = false) String granularity) {
        
        ReportFilterRequest filters = new ReportFilterRequest();
        filters.setHospital(hospital);
        filters.setDepartment(department);
        filters.setStartDate(startDate);
        filters.setEndDate(endDate);
        filters.setReportType(reportType);
        filters.setGranularity(granularity);

        ReportResponse report = reportService.generateReport(filters);
        return ResponseEntity.ok(report);
    }

    @PostMapping("/export")
    public ResponseEntity<?> exportReport(@RequestBody ExportRequest request) {
        String format = request.getFormat() != null ? request.getFormat().toUpperCase() : "PDF";

        if ("PDF".equals(format)) {
            byte[] pdfBytes = reportService.exportReportAsPdf(request.getFilters());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "report_" + System.currentTimeMillis() + ".pdf");
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } else if ("CSV".equals(format)) {
            String csv = reportService.exportReportAsCsv(request.getFilters());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", "report_" + System.currentTimeMillis() + ".csv");
            
            return new ResponseEntity<>(csv.getBytes(), headers, HttpStatus.OK);
        } else {
            return ResponseEntity.badRequest().body("Unsupported format. Use PDF or CSV.");
        }
    }
}
