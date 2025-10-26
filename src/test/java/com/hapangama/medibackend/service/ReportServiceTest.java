package com.hapangama.medibackend.service;

import com.hapangama.medibackend.dto.*;
import com.hapangama.medibackend.model.Appointment;
import com.hapangama.medibackend.model.HealthcareProvider;
import com.hapangama.medibackend.model.Payment;
import com.hapangama.medibackend.model.Patient;
import com.hapangama.medibackend.model.TimeSlot;
import com.hapangama.medibackend.repository.AppointmentRepository;
import com.hapangama.medibackend.repository.PaymentRepository;
import com.hapangama.medibackend.repository.ReportExportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ReportExportRepository reportExportRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private ReportService reportService;

    private List<Appointment> testAppointments;
    private HealthcareProvider provider1;
    private HealthcareProvider provider2;
    private Patient patient;

    @BeforeEach
    void setUp() {
        // Create test providers
        provider1 = new HealthcareProvider();
        provider1.setId(1L);
        provider1.setName("Dr. Smith");
        provider1.setSpecialty("Cardiology");
        provider1.setHospitalName("City Hospital");
        provider1.setHospitalType(HealthcareProvider.HospitalType.GOVERNMENT);

        provider2 = new HealthcareProvider();
        provider2.setId(2L);
        provider2.setName("Dr. Johnson");
        provider2.setSpecialty("Dermatology");
        provider2.setHospitalName("Private Clinic");
        provider2.setHospitalType(HealthcareProvider.HospitalType.PRIVATE);

        // Create test patient
        patient = new Patient();
        patient.setId(1L);
        patient.setName("John Doe");
        patient.setEmail("john@example.com");
        patient.setPhone("1234567890");
        patient.setDigitalHealthCardNumber("DHC123");

        // Create test appointments
        testAppointments = createTestAppointments();
    }

    private List<Appointment> createTestAppointments() {
        List<Appointment> appointments = new ArrayList<>();

        // Appointment 1 - Confirmed Cardiology
        Appointment apt1 = new Appointment();
        apt1.setId(1L);
        apt1.setPatient(patient);
        apt1.setProvider(provider1);
        apt1.setStatus(Appointment.AppointmentStatus.CONFIRMED);
        apt1.setBookingDateTime(LocalDateTime.now().minusDays(5));
        apt1.setPaymentRequired(false);
        
        TimeSlot ts1 = new TimeSlot();
        ts1.setStartTime(LocalDateTime.now().plusDays(1));
        ts1.setEndTime(LocalDateTime.now().plusDays(1).plusHours(1));
        apt1.setTimeSlot(ts1);
        appointments.add(apt1);

        // Appointment 2 - Pending Payment Dermatology
        Appointment apt2 = new Appointment();
        apt2.setId(2L);
        apt2.setPatient(patient);
        apt2.setProvider(provider2);
        apt2.setStatus(Appointment.AppointmentStatus.PENDING_PAYMENT);
        apt2.setBookingDateTime(LocalDateTime.now().minusDays(3));
        apt2.setPaymentRequired(true);
        
        TimeSlot ts2 = new TimeSlot();
        ts2.setStartTime(LocalDateTime.now().plusDays(2));
        ts2.setEndTime(LocalDateTime.now().plusDays(2).plusHours(1));
        apt2.setTimeSlot(ts2);
        appointments.add(apt2);

        // Appointment 3 - Cancelled Cardiology
        Appointment apt3 = new Appointment();
        apt3.setId(3L);
        apt3.setPatient(patient);
        apt3.setProvider(provider1);
        apt3.setStatus(Appointment.AppointmentStatus.CANCELLED);
        apt3.setBookingDateTime(LocalDateTime.now().minusDays(2));
        apt3.setPaymentRequired(false);
        
        TimeSlot ts3 = new TimeSlot();
        ts3.setStartTime(LocalDateTime.now().plusDays(3));
        ts3.setEndTime(LocalDateTime.now().plusDays(3).plusHours(1));
        apt3.setTimeSlot(ts3);
        appointments.add(apt3);

        return appointments;
    }

    @Test
    void testGenerateReport_WithNoFilters() {
        // Arrange
        ReportFilterRequest filters = new ReportFilterRequest();
        filters.setStartDate(LocalDate.now().minusDays(30));
        filters.setEndDate(LocalDate.now());

        when(appointmentRepository.findByBookingDateTimeBetween(any(), any()))
            .thenReturn(testAppointments);
        when(paymentRepository.sumCompletedPaymentsByDateRange(any(), any()))
            .thenReturn(new BigDecimal("100.00"));

        // Act
        ReportResponse report = reportService.generateReport(filters);

        // Assert
        assertNotNull(report);
        assertNotNull(report.getKpis());
        assertEquals(3L, report.getKpis().getTotalVisits());
        assertEquals(1L, report.getKpis().getConfirmedAppointments());
        assertEquals(1L, report.getKpis().getPendingPayments());
        assertEquals(1L, report.getKpis().getCancelledAppointments());
        assertEquals(new BigDecimal("100.00"), report.getKpis().getTotalRevenue());
        
        assertNotNull(report.getDailyVisits());
        assertFalse(report.getDailyVisits().isEmpty());
        
        assertNotNull(report.getDepartmentBreakdowns());
        assertEquals(2, report.getDepartmentBreakdowns().size());
    }

    @Test
    void testGenerateReport_WithHospitalFilter() {
        // Arrange
        ReportFilterRequest filters = new ReportFilterRequest();
        filters.setHospital("City Hospital");
        filters.setStartDate(LocalDate.now().minusDays(30));
        filters.setEndDate(LocalDate.now());

        List<Appointment> filteredAppointments = testAppointments.stream()
            .filter(a -> a.getProvider().getHospitalName().equals("City Hospital"))
            .toList();

        when(appointmentRepository.findByHospitalAndDateRange(any(), any(), any()))
            .thenReturn(filteredAppointments);
        when(paymentRepository.sumCompletedPaymentsByDateRange(any(), any()))
            .thenReturn(BigDecimal.ZERO);

        // Act
        ReportResponse report = reportService.generateReport(filters);

        // Assert
        assertNotNull(report);
        assertEquals(2L, report.getKpis().getTotalVisits());
        assertEquals(1, report.getDepartmentBreakdowns().size());
        assertEquals("Cardiology", report.getDepartmentBreakdowns().get(0).getDepartment());
    }

    @Test
    void testGenerateReport_WithDepartmentFilter() {
        // Arrange
        ReportFilterRequest filters = new ReportFilterRequest();
        filters.setDepartment("Cardiology");
        filters.setStartDate(LocalDate.now().minusDays(30));
        filters.setEndDate(LocalDate.now());

        List<Appointment> filteredAppointments = testAppointments.stream()
            .filter(a -> a.getProvider().getSpecialty().equals("Cardiology"))
            .toList();

        when(appointmentRepository.findBySpecialtyAndDateRange(any(), any(), any()))
            .thenReturn(filteredAppointments);
        when(paymentRepository.sumCompletedPaymentsByDateRange(any(), any()))
            .thenReturn(BigDecimal.ZERO);

        // Act
        ReportResponse report = reportService.generateReport(filters);

        // Assert
        assertNotNull(report);
        assertEquals(2L, report.getKpis().getTotalVisits());
        assertEquals(1, report.getDepartmentBreakdowns().size());
    }

    @Test
    void testGenerateReport_WithHospitalAndDepartmentFilter() {
        // Arrange
        ReportFilterRequest filters = new ReportFilterRequest();
        filters.setHospital("City Hospital");
        filters.setDepartment("Cardiology");
        filters.setStartDate(LocalDate.now().minusDays(30));
        filters.setEndDate(LocalDate.now());

        List<Appointment> filteredAppointments = testAppointments.stream()
            .filter(a -> a.getProvider().getHospitalName().equals("City Hospital") 
                      && a.getProvider().getSpecialty().equals("Cardiology"))
            .toList();

        when(appointmentRepository.findByHospitalSpecialtyAndDateRange(any(), any(), any(), any()))
            .thenReturn(filteredAppointments);
        when(paymentRepository.sumCompletedPaymentsByDateRange(any(), any()))
            .thenReturn(BigDecimal.ZERO);

        // Act
        ReportResponse report = reportService.generateReport(filters);

        // Assert
        assertNotNull(report);
        assertEquals(2L, report.getKpis().getTotalVisits());
    }

    @Test
    void testGenerateReport_NoDataAvailable() {
        // Arrange
        ReportFilterRequest filters = new ReportFilterRequest();
        filters.setStartDate(LocalDate.now().minusDays(30));
        filters.setEndDate(LocalDate.now());

        when(appointmentRepository.findByBookingDateTimeBetween(any(), any()))
            .thenReturn(new ArrayList<>());

        // Act
        ReportResponse report = reportService.generateReport(filters);

        // Assert
        assertNotNull(report);
        assertEquals("No data available for the selected filters and date range", report.getMessage());
        assertEquals(0L, report.getKpis().getTotalVisits());
        assertTrue(report.getDailyVisits().isEmpty());
        assertTrue(report.getDepartmentBreakdowns().isEmpty());
    }

    @Test
    void testCalculateKPIs_WithPayments() {
        // Arrange
        ReportFilterRequest filters = new ReportFilterRequest();
        filters.setStartDate(LocalDate.now().minusDays(30));
        filters.setEndDate(LocalDate.now());

        // Add payment to appointment
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setAmount(new BigDecimal("150.00"));
        payment.setStatus(Payment.PaymentStatus.COMPLETED);
        payment.setPaymentMethod(Payment.PaymentMethod.CREDIT_CARD);
        payment.setPaymentDateTime(LocalDateTime.now());
        testAppointments.get(1).setPayment(payment);

        when(appointmentRepository.findByBookingDateTimeBetween(any(), any()))
            .thenReturn(testAppointments);
        when(paymentRepository.sumCompletedPaymentsByDateRange(any(), any()))
            .thenReturn(new BigDecimal("150.00"));

        // Act
        ReportResponse report = reportService.generateReport(filters);

        // Assert
        assertNotNull(report);
        assertEquals(new BigDecimal("150.00"), report.getKpis().getTotalRevenue());
        assertEquals(1, report.getDepartmentBreakdowns().stream()
            .filter(d -> d.getRevenue().compareTo(BigDecimal.ZERO) > 0)
            .count());
    }

    @Test
    void testCalculateDailyVisits() {
        // Arrange
        ReportFilterRequest filters = new ReportFilterRequest();
        filters.setStartDate(LocalDate.now().minusDays(30));
        filters.setEndDate(LocalDate.now());

        when(appointmentRepository.findByBookingDateTimeBetween(any(), any()))
            .thenReturn(testAppointments);
        when(paymentRepository.sumCompletedPaymentsByDateRange(any(), any()))
            .thenReturn(BigDecimal.ZERO);

        // Act
        ReportResponse report = reportService.generateReport(filters);

        // Assert
        assertNotNull(report.getDailyVisits());
        assertEquals(3, report.getDailyVisits().size()); // 3 different days
        
        // Verify daily visit counts
        report.getDailyVisits().forEach(dv -> {
            assertTrue(dv.getVisitCount() > 0);
            assertTrue(dv.getConfirmedCount() >= 0);
            assertTrue(dv.getCancelledCount() >= 0);
        });
    }

    @Test
    void testCalculateDepartmentBreakdown() {
        // Arrange
        ReportFilterRequest filters = new ReportFilterRequest();
        filters.setStartDate(LocalDate.now().minusDays(30));
        filters.setEndDate(LocalDate.now());

        when(appointmentRepository.findByBookingDateTimeBetween(any(), any()))
            .thenReturn(testAppointments);
        when(paymentRepository.sumCompletedPaymentsByDateRange(any(), any()))
            .thenReturn(BigDecimal.ZERO);

        // Act
        ReportResponse report = reportService.generateReport(filters);

        // Assert
        assertNotNull(report.getDepartmentBreakdowns());
        assertEquals(2, report.getDepartmentBreakdowns().size());
        
        // Verify Cardiology has more appointments (2 vs 1)
        DepartmentBreakdown cardiology = report.getDepartmentBreakdowns().stream()
            .filter(d -> d.getDepartment().equals("Cardiology"))
            .findFirst()
            .orElse(null);
        assertNotNull(cardiology);
        assertEquals(2L, cardiology.getTotalAppointments());
        assertEquals(1L, cardiology.getConfirmedAppointments());
        assertEquals(50.0, cardiology.getCompletionRate());
    }

    @Test
    void testExportReportAsCsv() {
        // Arrange
        ReportFilterRequest filters = new ReportFilterRequest();
        filters.setStartDate(LocalDate.now().minusDays(30));
        filters.setEndDate(LocalDate.now());

        when(appointmentRepository.findByBookingDateTimeBetween(any(), any()))
            .thenReturn(testAppointments);
        when(paymentRepository.sumCompletedPaymentsByDateRange(any(), any()))
            .thenReturn(BigDecimal.ZERO);
        when(reportExportRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        // Act
        String csv = reportService.exportReportAsCsv(filters);

        // Assert
        assertNotNull(csv);
        assertTrue(csv.contains("Healthcare Statistical Report"));
        assertTrue(csv.contains("Key Performance Indicators"));
        assertTrue(csv.contains("Department Breakdown"));
        assertTrue(csv.contains("Daily Visits"));
        assertTrue(csv.contains("Cardiology"));
        assertTrue(csv.contains("Dermatology"));
        verify(reportExportRepository, times(1)).save(any());
    }

    @Test
    void testExportReportAsPdf() {
        // Arrange
        ReportFilterRequest filters = new ReportFilterRequest();
        filters.setStartDate(LocalDate.now().minusDays(30));
        filters.setEndDate(LocalDate.now());

        when(appointmentRepository.findByBookingDateTimeBetween(any(), any()))
            .thenReturn(testAppointments);
        when(paymentRepository.sumCompletedPaymentsByDateRange(any(), any()))
            .thenReturn(BigDecimal.ZERO);
        when(reportExportRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        // Act
        byte[] pdf = reportService.exportReportAsPdf(filters);

        // Assert
        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
        verify(reportExportRepository, times(1)).save(any());
    }

    @Test
    void testGenerateReport_WithDefaultDateRange() {
        // Arrange
        ReportFilterRequest filters = new ReportFilterRequest();
        // No dates provided - should default to last month

        when(appointmentRepository.findByBookingDateTimeBetween(any(), any()))
            .thenReturn(testAppointments);
        when(paymentRepository.sumCompletedPaymentsByDateRange(any(), any()))
            .thenReturn(BigDecimal.ZERO);

        // Act
        ReportResponse report = reportService.generateReport(filters);

        // Assert
        assertNotNull(report);
        assertNotNull(report.getGeneratedAt());
        verify(appointmentRepository, times(1)).findByBookingDateTimeBetween(any(), any());
    }

    @Test
    void testCompletionRateCalculation() {
        // Arrange
        ReportFilterRequest filters = new ReportFilterRequest();
        filters.setStartDate(LocalDate.now().minusDays(30));
        filters.setEndDate(LocalDate.now());

        when(appointmentRepository.findByBookingDateTimeBetween(any(), any()))
            .thenReturn(testAppointments);
        when(paymentRepository.sumCompletedPaymentsByDateRange(any(), any()))
            .thenReturn(BigDecimal.ZERO);

        // Act
        ReportResponse report = reportService.generateReport(filters);

        // Assert
        // 1 confirmed out of 3 total = 33.33%
        double expectedRate = 33.33;
        assertEquals(expectedRate, report.getKpis().getAppointmentCompletionRate());
    }
}
