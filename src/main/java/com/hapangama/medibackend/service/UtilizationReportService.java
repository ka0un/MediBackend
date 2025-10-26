package com.hapangama.medibackend.service;

import com.hapangama.medibackend.dto.CreateUtilizationReportRequest;
import com.hapangama.medibackend.dto.UpdateUtilizationReportRequest;
import com.hapangama.medibackend.dto.UtilizationReportResponse;
import com.hapangama.medibackend.exception.NotFoundException;
import com.hapangama.medibackend.model.Appointment;
import com.hapangama.medibackend.model.UtilizationReport;
import com.hapangama.medibackend.repository.AppointmentRepository;
import com.hapangama.medibackend.repository.UtilizationReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service class for managing Utilization Reports
 * Follows Single Responsibility Principle - handles only utilization report business logic
 */
@Service
@RequiredArgsConstructor
public class UtilizationReportService {

    private final UtilizationReportRepository utilizationReportRepository;
    private final AppointmentRepository appointmentRepository;
    private final UtilizationReportCalculator calculator;
    private final UtilizationReportValidator validator;
    private final AuditService auditService;

    /**
     * Create a new utilization report based on filters
     * Follows Open/Closed Principle - extensible through calculator strategies
     */
    @Transactional
    public UtilizationReportResponse createReport(CreateUtilizationReportRequest request) {
        validator.validateCreateRequest(request);
        
        UtilizationReport report = new UtilizationReport();
        report.setReportName(request.getReportName());
        report.setStartDate(request.getStartDate());
        report.setEndDate(request.getEndDate());
        report.setDepartment(request.getDepartment());
        report.setDoctor(request.getDoctor());
        report.setServiceCategory(request.getServiceCategory());
        
        // Calculate metrics based on appointments data
        List<Appointment> appointments = fetchAppointmentsForReport(request);
        UtilizationMetrics metrics = calculator.calculateMetrics(appointments, request);
        
        report.setTotalServices(metrics.getTotalServices());
        report.setTotalPatients(metrics.getTotalPatients());
        report.setAverageUtilization(metrics.getAverageUtilization());
        report.setPeakHours(metrics.getPeakHours());
        
        UtilizationReport savedReport = utilizationReportRepository.save(report);

        // Audit report creation
        auditService.logAsync(AuditService.builder()
            .action("UTILIZATION_REPORT_CREATED")
            .entityType("UtilizationReport")
            .entityId(String.valueOf(savedReport.getId()))
            .details(String.format("Utilization report created: %s (Period: %s to %s, Department: %s)", 
                savedReport.getReportName(), savedReport.getStartDate(), savedReport.getEndDate(), 
                savedReport.getDepartment() != null ? savedReport.getDepartment() : "All"))
            .metadata(String.format("{\"reportId\":%d,\"reportName\":\"%s\",\"startDate\":\"%s\",\"endDate\":\"%s\",\"department\":\"%s\",\"totalServices\":%d}", 
                savedReport.getId(), savedReport.getReportName(), savedReport.getStartDate(), 
                savedReport.getEndDate(), savedReport.getDepartment() != null ? savedReport.getDepartment() : "All", 
                savedReport.getTotalServices())));

        return UtilizationReportResponse.fromEntity(savedReport);
    }

    /**
     * Read/View all utilization reports
     */
    @Transactional(readOnly = true)
    public List<UtilizationReportResponse> getAllReports() {
        return utilizationReportRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(UtilizationReportResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Read/View a specific utilization report by ID
     */
    @Transactional(readOnly = true)
    public UtilizationReportResponse getReportById(Long id) {
        UtilizationReport report = utilizationReportRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Utilization report not found with id: " + id));
        return UtilizationReportResponse.fromEntity(report);
    }

    /**
     * Update an existing utilization report
     * Follows Open/Closed Principle - can regenerate with new parameters
     */
    @Transactional
    public UtilizationReportResponse updateReport(Long id, UpdateUtilizationReportRequest request) {
        UtilizationReport report = utilizationReportRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Utilization report not found with id: " + id));
        
        validator.validateUpdateRequest(request);
        
        // Update basic fields
        if (request.getReportName() != null) {
            report.setReportName(request.getReportName());
        }
        if (request.getNotes() != null) {
            report.setNotes(request.getNotes());
        }
        
        // If date range or filters changed, recalculate metrics
        boolean needsRecalculation = false;
        if (request.getStartDate() != null && !request.getStartDate().equals(report.getStartDate())) {
            report.setStartDate(request.getStartDate());
            needsRecalculation = true;
        }
        if (request.getEndDate() != null && !request.getEndDate().equals(report.getEndDate())) {
            report.setEndDate(request.getEndDate());
            needsRecalculation = true;
        }
        if (request.getDepartment() != null) {
            report.setDepartment(request.getDepartment());
            needsRecalculation = true;
        }
        if (request.getDoctor() != null) {
            report.setDoctor(request.getDoctor());
            needsRecalculation = true;
        }
        if (request.getServiceCategory() != null) {
            report.setServiceCategory(request.getServiceCategory());
            needsRecalculation = true;
        }
        
        if (needsRecalculation) {
            CreateUtilizationReportRequest recalcRequest = new CreateUtilizationReportRequest(
                report.getReportName(),
                report.getStartDate(),
                report.getEndDate(),
                report.getDepartment(),
                report.getDoctor(),
                report.getServiceCategory()
            );
            List<Appointment> appointments = fetchAppointmentsForReport(recalcRequest);
            UtilizationMetrics metrics = calculator.calculateMetrics(appointments, recalcRequest);
            
            report.setTotalServices(metrics.getTotalServices());
            report.setTotalPatients(metrics.getTotalPatients());
            report.setAverageUtilization(metrics.getAverageUtilization());
            report.setPeakHours(metrics.getPeakHours());
        }
        
        UtilizationReport updatedReport = utilizationReportRepository.save(report);

        // Audit report update
        auditService.logAsync(AuditService.builder()
            .action("UTILIZATION_REPORT_UPDATED")
            .entityType("UtilizationReport")
            .entityId(String.valueOf(id))
            .details(String.format("Utilization report updated: %s (Recalculated: %b)", 
                updatedReport.getReportName(), needsRecalculation))
            .metadata(String.format("{\"reportId\":%d,\"recalculated\":%b}", id, needsRecalculation)));

        return UtilizationReportResponse.fromEntity(updatedReport);
    }

    /**
     * Delete a utilization report
     */
    @Transactional
    public void deleteReport(Long id) {
        if (!utilizationReportRepository.existsById(id)) {
            throw new NotFoundException("Utilization report not found with id: " + id);
        }

        // Audit report deletion
        auditService.logAsync(AuditService.builder()
            .action("UTILIZATION_REPORT_DELETED")
            .entityType("UtilizationReport")
            .entityId(String.valueOf(id))
            .details(String.format("Utilization report deleted: ID %d", id))
            .metadata(String.format("{\"reportId\":%d}", id)));

        utilizationReportRepository.deleteById(id);
    }

    /**
     * Filter reports by department
     */
    @Transactional(readOnly = true)
    public List<UtilizationReportResponse> getReportsByDepartment(String department) {
        return utilizationReportRepository.findByDepartment(department)
                .stream()
                .map(UtilizationReportResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Filter reports by doctor
     */
    @Transactional(readOnly = true)
    public List<UtilizationReportResponse> getReportsByDoctor(String doctor) {
        return utilizationReportRepository.findByDoctor(doctor)
                .stream()
                .map(UtilizationReportResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Helper method to fetch appointments based on report filters
     * Follows Dependency Inversion Principle - depends on repository abstraction
     */
    private List<Appointment> fetchAppointmentsForReport(CreateUtilizationReportRequest request) {
        List<Appointment> appointments = appointmentRepository.findAll();
        
        // Apply filters
        return appointments.stream()
                .filter(apt -> {
                    LocalDate aptDate = apt.getTimeSlot().getStartTime().toLocalDate();
                    return !aptDate.isBefore(request.getStartDate()) && !aptDate.isAfter(request.getEndDate());
                })
                .filter(apt -> request.getDepartment() == null || 
                        request.getDepartment().isEmpty() || 
                        apt.getProvider().getSpecialty().equalsIgnoreCase(request.getDepartment()))
                .filter(apt -> request.getDoctor() == null || 
                        request.getDoctor().isEmpty() || 
                        apt.getProvider().getName().equalsIgnoreCase(request.getDoctor()))
                .collect(Collectors.toList());
    }
}

/**
 * Separate class for calculation logic
 * Follows Single Responsibility Principle - only calculates metrics
 */
@Service
class UtilizationReportCalculator {
    
    public UtilizationMetrics calculateMetrics(List<Appointment> appointments, CreateUtilizationReportRequest request) {
        int totalServices = appointments.size();
        long totalPatients = appointments.stream()
                .map(apt -> apt.getPatient().getId())
                .distinct()
                .count();
        
        // Calculate average utilization (percentage of confirmed appointments)
        long confirmedCount = appointments.stream()
                .filter(apt -> apt.getStatus() == Appointment.AppointmentStatus.CONFIRMED || 
                              apt.getStatus() == Appointment.AppointmentStatus.COMPLETED)
                .count();
        double averageUtilization = totalServices > 0 ? (confirmedCount * 100.0 / totalServices) : 0.0;
        
        // Calculate peak hours
        String peakHours = calculatePeakHours(appointments);
        
        return new UtilizationMetrics(totalServices, (int) totalPatients, averageUtilization, peakHours);
    }
    
    private String calculatePeakHours(List<Appointment> appointments) {
        Map<Integer, Long> hourCounts = new HashMap<>();
        
        for (Appointment apt : appointments) {
            int hour = apt.getTimeSlot().getStartTime().toLocalTime().getHour();
            hourCounts.put(hour, hourCounts.getOrDefault(hour, 0L) + 1);
        }
        
        if (hourCounts.isEmpty()) {
            return "N/A";
        }
        
        int peakHour = hourCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(0);
        
        return String.format("%02d:00 - %02d:00", peakHour, peakHour + 1);
    }
}

/**
 * Separate class for validation logic
 * Follows Single Responsibility Principle - only validates requests
 */
@Service
class UtilizationReportValidator {
    
    public void validateCreateRequest(CreateUtilizationReportRequest request) {
        if (request.getReportName() == null || request.getReportName().trim().isEmpty()) {
            throw new IllegalArgumentException("Report name is required");
        }
        if (request.getStartDate() == null) {
            throw new IllegalArgumentException("Start date is required");
        }
        if (request.getEndDate() == null) {
            throw new IllegalArgumentException("End date is required");
        }
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }
        if (request.getEndDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("End date cannot be in the future");
        }
    }
    
    public void validateUpdateRequest(UpdateUtilizationReportRequest request) {
        if (request.getStartDate() != null && request.getEndDate() != null) {
            if (request.getStartDate().isAfter(request.getEndDate())) {
                throw new IllegalArgumentException("Start date must be before or equal to end date");
            }
        }
    }
}

/**
 * Data class for metrics
 * Follows Interface Segregation Principle - contains only necessary data
 */
@lombok.Data
@lombok.AllArgsConstructor
class UtilizationMetrics {
    private int totalServices;
    private int totalPatients;
    private double averageUtilization;
    private String peakHours;
}
