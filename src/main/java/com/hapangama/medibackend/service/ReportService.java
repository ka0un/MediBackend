package com.hapangama.medibackend.service;

import com.hapangama.medibackend.dto.*;
import com.hapangama.medibackend.model.Appointment;
import com.hapangama.medibackend.model.Payment;
import com.hapangama.medibackend.model.ReportExport;
import com.hapangama.medibackend.repository.AppointmentRepository;
import com.hapangama.medibackend.repository.PaymentRepository;
import com.hapangama.medibackend.repository.ReportExportRepository;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final AppointmentRepository appointmentRepository;
    private final PaymentRepository paymentRepository;
    private final ReportExportRepository reportExportRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public ReportResponse generateReport(ReportFilterRequest filters) {
        // Validate and set default date range if not provided
        LocalDateTime startDate = filters.getStartDate() != null 
            ? filters.getStartDate().atStartOfDay() 
            : LocalDateTime.now().minusMonths(1);
        LocalDateTime endDate = filters.getEndDate() != null 
            ? filters.getEndDate().atTime(LocalTime.MAX) 
            : LocalDateTime.now();

        // Fetch appointments based on filters
        List<Appointment> appointments = fetchFilteredAppointments(filters, startDate, endDate);

        if (appointments.isEmpty()) {
            return ReportResponse.builder()
                .filters(filters)
                .generatedAt(LocalDateTime.now())
                .message("No data available for the selected filters and date range")
                .kpis(createEmptyKPIs())
                .dailyVisits(new ArrayList<>())
                .departmentBreakdowns(new ArrayList<>())
                .build();
        }

        // Calculate KPIs
        ReportKPIs kpis = calculateKPIs(appointments, startDate, endDate);

        // Calculate daily visits
        List<DailyVisit> dailyVisits = calculateDailyVisits(appointments);

        // Calculate department breakdown
        List<DepartmentBreakdown> departmentBreakdowns = calculateDepartmentBreakdown(appointments);

        // Audit report generation
        auditService.logAsync(AuditService.builder()
            .action("REPORT_GENERATED")
            .entityType("Report")
            .details(String.format("Statistical report generated (Period: %s to %s, Hospital: %s, Department: %s, Total Visits: %d)", 
                filters.getStartDate(), filters.getEndDate(), 
                filters.getHospital() != null ? filters.getHospital() : "All",
                filters.getDepartment() != null ? filters.getDepartment() : "All",
                kpis.getTotalVisits()))
            .metadata(String.format("{\"startDate\":\"%s\",\"endDate\":\"%s\",\"hospital\":\"%s\",\"department\":\"%s\",\"totalVisits\":%d}", 
                filters.getStartDate(), filters.getEndDate(),
                filters.getHospital() != null ? filters.getHospital() : "All",
                filters.getDepartment() != null ? filters.getDepartment() : "All",
                kpis.getTotalVisits())));

        return ReportResponse.builder()
            .kpis(kpis)
            .dailyVisits(dailyVisits)
            .departmentBreakdowns(departmentBreakdowns)
            .filters(filters)
            .generatedAt(LocalDateTime.now())
            .build();
    }

    private List<Appointment> fetchFilteredAppointments(ReportFilterRequest filters, 
                                                         LocalDateTime startDate, 
                                                         LocalDateTime endDate) {
        if (filters.getHospital() != null && filters.getDepartment() != null) {
            return appointmentRepository.findByHospitalSpecialtyAndDateRange(
                filters.getHospital(), filters.getDepartment(), startDate, endDate);
        } else if (filters.getHospital() != null) {
            return appointmentRepository.findByHospitalAndDateRange(
                filters.getHospital(), startDate, endDate);
        } else if (filters.getDepartment() != null) {
            return appointmentRepository.findBySpecialtyAndDateRange(
                filters.getDepartment(), startDate, endDate);
        } else {
            return appointmentRepository.findByBookingDateTimeBetween(startDate, endDate);
        }
    }

    private ReportKPIs calculateKPIs(List<Appointment> appointments, 
                                      LocalDateTime startDate, 
                                      LocalDateTime endDate) {
        long totalVisits = appointments.size();
        long confirmedAppointments = appointments.stream()
            .filter(a -> a.getStatus() == Appointment.AppointmentStatus.CONFIRMED)
            .count();
        long pendingPayments = appointments.stream()
            .filter(a -> a.getStatus() == Appointment.AppointmentStatus.PENDING_PAYMENT)
            .count();
        long cancelledAppointments = appointments.stream()
            .filter(a -> a.getStatus() == Appointment.AppointmentStatus.CANCELLED)
            .count();

        // Calculate total revenue
        BigDecimal totalRevenue = paymentRepository.sumCompletedPaymentsByDateRange(startDate, endDate);

        // Calculate average wait time (simplified - time between booking and appointment time)
        BigDecimal averageWaitTime = appointments.stream()
            .filter(a -> a.getTimeSlot() != null)
            .map(a -> {
                long hours = java.time.Duration.between(
                    a.getBookingDateTime(), 
                    a.getTimeSlot().getStartTime()
                ).toHours();
                return BigDecimal.valueOf(hours);
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(Math.max(1, totalVisits)), 2, RoundingMode.HALF_UP);

        // Calculate completion rate
        double completionRate = totalVisits > 0 
            ? (confirmedAppointments * 100.0 / totalVisits) 
            : 0.0;

        return ReportKPIs.builder()
            .totalVisits(totalVisits)
            .confirmedAppointments(confirmedAppointments)
            .pendingPayments(pendingPayments)
            .cancelledAppointments(cancelledAppointments)
            .totalRevenue(totalRevenue)
            .averageWaitTime(averageWaitTime)
            .appointmentCompletionRate(Math.round(completionRate * 100.0) / 100.0)
            .build();
    }

    private List<DailyVisit> calculateDailyVisits(List<Appointment> appointments) {
        Map<LocalDate, List<Appointment>> groupedByDate = appointments.stream()
            .collect(Collectors.groupingBy(a -> a.getBookingDateTime().toLocalDate()));

        return groupedByDate.entrySet().stream()
            .map(entry -> {
                LocalDate date = entry.getKey();
                List<Appointment> dayAppointments = entry.getValue();
                
                long visitCount = dayAppointments.size();
                long confirmedCount = dayAppointments.stream()
                    .filter(a -> a.getStatus() == Appointment.AppointmentStatus.CONFIRMED)
                    .count();
                long cancelledCount = dayAppointments.stream()
                    .filter(a -> a.getStatus() == Appointment.AppointmentStatus.CANCELLED)
                    .count();

                return DailyVisit.builder()
                    .date(date)
                    .visitCount(visitCount)
                    .confirmedCount(confirmedCount)
                    .cancelledCount(cancelledCount)
                    .build();
            })
            .sorted(Comparator.comparing(DailyVisit::getDate))
            .collect(Collectors.toList());
    }

    private List<DepartmentBreakdown> calculateDepartmentBreakdown(List<Appointment> appointments) {
        Map<String, List<Appointment>> groupedByDepartment = appointments.stream()
            .collect(Collectors.groupingBy(a -> a.getProvider().getSpecialty()));

        return groupedByDepartment.entrySet().stream()
            .map(entry -> {
                String department = entry.getKey();
                List<Appointment> deptAppointments = entry.getValue();
                
                long totalAppointments = deptAppointments.size();
                long confirmedAppointments = deptAppointments.stream()
                    .filter(a -> a.getStatus() == Appointment.AppointmentStatus.CONFIRMED)
                    .count();
                
                // Calculate revenue for this department
                BigDecimal revenue = deptAppointments.stream()
                    .filter(a -> a.getPayment() != null && 
                                 a.getPayment().getStatus() == Payment.PaymentStatus.COMPLETED)
                    .map(a -> a.getPayment().getAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                double completionRate = totalAppointments > 0 
                    ? (confirmedAppointments * 100.0 / totalAppointments) 
                    : 0.0;

                return DepartmentBreakdown.builder()
                    .department(department)
                    .totalAppointments(totalAppointments)
                    .confirmedAppointments(confirmedAppointments)
                    .revenue(revenue)
                    .completionRate(Math.round(completionRate * 100.0) / 100.0)
                    .build();
            })
            .sorted(Comparator.comparing(DepartmentBreakdown::getTotalAppointments).reversed())
            .collect(Collectors.toList());
    }

    private ReportKPIs createEmptyKPIs() {
        return ReportKPIs.builder()
            .totalVisits(0L)
            .confirmedAppointments(0L)
            .pendingPayments(0L)
            .cancelledAppointments(0L)
            .totalRevenue(BigDecimal.ZERO)
            .averageWaitTime(BigDecimal.ZERO)
            .appointmentCompletionRate(0.0)
            .build();
    }

    @Transactional
    public byte[] exportReportAsPdf(ReportFilterRequest filters) {
        try {
            ReportResponse report = generateReport(filters);
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Add title
            document.add(new Paragraph("Healthcare Statistical Report")
                .setFontSize(20)
                .setBold());
            
            document.add(new Paragraph("Generated: " + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .setFontSize(10));
            
            document.add(new Paragraph("\n"));

            // Add filters
            if (filters.getHospital() != null || filters.getDepartment() != null) {
                document.add(new Paragraph("Filters Applied:").setBold());
                if (filters.getHospital() != null) {
                    document.add(new Paragraph("Hospital: " + filters.getHospital()));
                }
                if (filters.getDepartment() != null) {
                    document.add(new Paragraph("Department: " + filters.getDepartment()));
                }
                if (filters.getStartDate() != null && filters.getEndDate() != null) {
                    document.add(new Paragraph("Date Range: " + filters.getStartDate() + 
                        " to " + filters.getEndDate()));
                }
                document.add(new Paragraph("\n"));
            }

            // Add KPIs
            document.add(new Paragraph("Key Performance Indicators").setBold().setFontSize(16));
            document.add(new Paragraph("Total Visits: " + report.getKpis().getTotalVisits()));
            document.add(new Paragraph("Confirmed Appointments: " + report.getKpis().getConfirmedAppointments()));
            document.add(new Paragraph("Pending Payments: " + report.getKpis().getPendingPayments()));
            document.add(new Paragraph("Cancelled Appointments: " + report.getKpis().getCancelledAppointments()));
            document.add(new Paragraph("Total Revenue: $" + report.getKpis().getTotalRevenue()));
            document.add(new Paragraph("Average Wait Time: " + report.getKpis().getAverageWaitTime() + " hours"));
            document.add(new Paragraph("Completion Rate: " + report.getKpis().getAppointmentCompletionRate() + "%"));
            document.add(new Paragraph("\n"));

            // Add Department Breakdown table
            if (!report.getDepartmentBreakdowns().isEmpty()) {
                document.add(new Paragraph("Department Breakdown").setBold().setFontSize(16));
                
                float[] columnWidths = {3, 2, 2, 2, 2};
                Table table = new Table(columnWidths);
                table.addHeaderCell("Department");
                table.addHeaderCell("Total");
                table.addHeaderCell("Confirmed");
                table.addHeaderCell("Revenue");
                table.addHeaderCell("Rate %");
                
                for (DepartmentBreakdown dept : report.getDepartmentBreakdowns()) {
                    table.addCell(dept.getDepartment());
                    table.addCell(String.valueOf(dept.getTotalAppointments()));
                    table.addCell(String.valueOf(dept.getConfirmedAppointments()));
                    table.addCell("$" + dept.getRevenue());
                    table.addCell(String.valueOf(dept.getCompletionRate()));
                }
                
                document.add(table);
            }

            document.close();

            // Record export
            ReportExport export = new ReportExport();
            export.setFormat("PDF");
            export.setExportedAt(LocalDateTime.now());
            export.setFilterParameters(filters.toString());
            export.setFileName("report_" + System.currentTimeMillis() + ".pdf");
            export.setStatus(ReportExport.ExportStatus.COMPLETED);
            reportExportRepository.save(export);

            // Audit PDF export
            auditService.logAsync(AuditService.builder()
                .action("REPORT_EXPORTED_PDF")
                .entityType("ReportExport")
                .entityId(String.valueOf(export.getId()))
                .details(String.format("Report exported as PDF: %s", export.getFileName()))
                .metadata(String.format("{\"exportId\":%d,\"format\":\"PDF\",\"fileName\":\"%s\"}", 
                    export.getId(), export.getFileName())));

            return baos.toByteArray();
        } catch (Exception e) {
            // Record failed export
            ReportExport export = new ReportExport();
            export.setFormat("PDF");
            export.setExportedAt(LocalDateTime.now());
            export.setFilterParameters(filters.toString());
            export.setStatus(ReportExport.ExportStatus.FAILED);
            reportExportRepository.save(export);
            
            throw new RuntimeException("Failed to generate PDF report: " + e.getMessage(), e);
        }
    }

    @Transactional
    public String exportReportAsCsv(ReportFilterRequest filters) {
        try {
            ReportResponse report = generateReport(filters);
            
            StringBuilder csv = new StringBuilder();
            
            // Add header
            csv.append("Healthcare Statistical Report\n");
            csv.append("Generated: ").append(LocalDateTime.now()).append("\n\n");
            
            // Add KPIs
            csv.append("Key Performance Indicators\n");
            csv.append("Total Visits,").append(report.getKpis().getTotalVisits()).append("\n");
            csv.append("Confirmed Appointments,").append(report.getKpis().getConfirmedAppointments()).append("\n");
            csv.append("Pending Payments,").append(report.getKpis().getPendingPayments()).append("\n");
            csv.append("Cancelled Appointments,").append(report.getKpis().getCancelledAppointments()).append("\n");
            csv.append("Total Revenue,").append(report.getKpis().getTotalRevenue()).append("\n");
            csv.append("Average Wait Time (hours),").append(report.getKpis().getAverageWaitTime()).append("\n");
            csv.append("Completion Rate (%),").append(report.getKpis().getAppointmentCompletionRate()).append("\n\n");
            
            // Add Department Breakdown
            if (!report.getDepartmentBreakdowns().isEmpty()) {
                csv.append("Department Breakdown\n");
                csv.append("Department,Total Appointments,Confirmed Appointments,Revenue,Completion Rate (%)\n");
                for (DepartmentBreakdown dept : report.getDepartmentBreakdowns()) {
                    csv.append(dept.getDepartment()).append(",")
                       .append(dept.getTotalAppointments()).append(",")
                       .append(dept.getConfirmedAppointments()).append(",")
                       .append(dept.getRevenue()).append(",")
                       .append(dept.getCompletionRate()).append("\n");
                }
                csv.append("\n");
            }
            
            // Add Daily Visits
            if (!report.getDailyVisits().isEmpty()) {
                csv.append("Daily Visits\n");
                csv.append("Date,Total Visits,Confirmed,Cancelled\n");
                for (DailyVisit visit : report.getDailyVisits()) {
                    csv.append(visit.getDate()).append(",")
                       .append(visit.getVisitCount()).append(",")
                       .append(visit.getConfirmedCount()).append(",")
                       .append(visit.getCancelledCount()).append("\n");
                }
            }

            // Record export
            ReportExport export = new ReportExport();
            export.setFormat("CSV");
            export.setExportedAt(LocalDateTime.now());
            export.setFilterParameters(filters.toString());
            export.setFileName("report_" + System.currentTimeMillis() + ".csv");
            export.setStatus(ReportExport.ExportStatus.COMPLETED);
            reportExportRepository.save(export);

            // Audit CSV export
            auditService.logAsync(AuditService.builder()
                .action("REPORT_EXPORTED_CSV")
                .entityType("ReportExport")
                .entityId(String.valueOf(export.getId()))
                .details(String.format("Report exported as CSV: %s", export.getFileName()))
                .metadata(String.format("{\"exportId\":%d,\"format\":\"CSV\",\"fileName\":\"%s\"}", 
                    export.getId(), export.getFileName())));

            return csv.toString();
        } catch (Exception e) {
            // Record failed export
            ReportExport export = new ReportExport();
            export.setFormat("CSV");
            export.setExportedAt(LocalDateTime.now());
            export.setFilterParameters(filters.toString());
            export.setStatus(ReportExport.ExportStatus.FAILED);
            reportExportRepository.save(export);
            
            throw new RuntimeException("Failed to generate CSV report: " + e.getMessage(), e);
        }
    }
}
