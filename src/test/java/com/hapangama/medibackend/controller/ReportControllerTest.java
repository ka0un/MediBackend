package com.hapangama.medibackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hapangama.medibackend.dto.ExportRequest;
import com.hapangama.medibackend.dto.ReportFilterRequest;
import com.hapangama.medibackend.model.*;
import com.hapangama.medibackend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private HealthcareProviderRepository providerRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ReportExportRepository reportExportRepository;

    private Patient patient;
    private HealthcareProvider governmentProvider;
    private HealthcareProvider privateProvider;

    @BeforeEach
    void setUp() {
        // Clean up database
        paymentRepository.deleteAll();
        appointmentRepository.deleteAll();
        reportExportRepository.deleteAll();
        timeSlotRepository.deleteAll();
        providerRepository.deleteAll();
        patientRepository.deleteAll();

        // Create test patient
        patient = new Patient();
        patient.setName("John Doe");
        patient.setEmail("john@example.com");
        patient.setPhone("1234567890");
        patient.setDigitalHealthCardNumber("DHC12345");
        patient = patientRepository.save(patient);

        // Create government provider
        governmentProvider = new HealthcareProvider();
        governmentProvider.setName("Dr. Smith");
        governmentProvider.setSpecialty("Cardiology");
        governmentProvider.setHospitalName("City Hospital");
        governmentProvider.setHospitalType(HealthcareProvider.HospitalType.GOVERNMENT);
        governmentProvider = providerRepository.save(governmentProvider);

        // Create private provider
        privateProvider = new HealthcareProvider();
        privateProvider.setName("Dr. Johnson");
        privateProvider.setSpecialty("Dermatology");
        privateProvider.setHospitalName("Private Clinic");
        privateProvider.setHospitalType(HealthcareProvider.HospitalType.PRIVATE);
        privateProvider = providerRepository.save(privateProvider);

        // Create test appointments
        createTestAppointments();
    }

    private void createTestAppointments() {
        // Appointment 1 - Confirmed Cardiology
        TimeSlot ts1 = new TimeSlot();
        ts1.setProvider(governmentProvider);
        ts1.setStartTime(LocalDateTime.now().plusDays(1));
        ts1.setEndTime(LocalDateTime.now().plusDays(1).plusHours(1));
        ts1.setAvailable(false);
        ts1 = timeSlotRepository.save(ts1);

        Appointment apt1 = new Appointment();
        apt1.setPatient(patient);
        apt1.setProvider(governmentProvider);
        apt1.setTimeSlot(ts1);
        apt1.setStatus(Appointment.AppointmentStatus.CONFIRMED);
        apt1.setBookingDateTime(LocalDateTime.now().minusDays(5));
        apt1.setConfirmationNumber("APT-001");
        apt1.setPaymentRequired(false);
        appointmentRepository.save(apt1);

        // Appointment 2 - Pending Payment Dermatology
        TimeSlot ts2 = new TimeSlot();
        ts2.setProvider(privateProvider);
        ts2.setStartTime(LocalDateTime.now().plusDays(2));
        ts2.setEndTime(LocalDateTime.now().plusDays(2).plusHours(1));
        ts2.setAvailable(false);
        ts2 = timeSlotRepository.save(ts2);

        Appointment apt2 = new Appointment();
        apt2.setPatient(patient);
        apt2.setProvider(privateProvider);
        apt2.setTimeSlot(ts2);
        apt2.setStatus(Appointment.AppointmentStatus.PENDING_PAYMENT);
        apt2.setBookingDateTime(LocalDateTime.now().minusDays(3));
        apt2.setConfirmationNumber("APT-002");
        apt2.setPaymentRequired(true);
        apt2 = appointmentRepository.save(apt2);

        // Add payment to second appointment
        Payment payment = new Payment();
        payment.setAppointment(apt2);
        payment.setAmount(new BigDecimal("100.00"));
        payment.setStatus(Payment.PaymentStatus.COMPLETED);
        payment.setPaymentMethod(Payment.PaymentMethod.CREDIT_CARD);
        payment.setTransactionId("TXN-001");
        payment.setPaymentDateTime(LocalDateTime.now());
        paymentRepository.save(payment);

        // Appointment 3 - Cancelled Cardiology
        TimeSlot ts3 = new TimeSlot();
        ts3.setProvider(governmentProvider);
        ts3.setStartTime(LocalDateTime.now().plusDays(3));
        ts3.setEndTime(LocalDateTime.now().plusDays(3).plusHours(1));
        ts3.setAvailable(true);
        ts3 = timeSlotRepository.save(ts3);

        Appointment apt3 = new Appointment();
        apt3.setPatient(patient);
        apt3.setProvider(governmentProvider);
        apt3.setTimeSlot(ts3);
        apt3.setStatus(Appointment.AppointmentStatus.CANCELLED);
        apt3.setBookingDateTime(LocalDateTime.now().minusDays(2));
        apt3.setConfirmationNumber("APT-003");
        apt3.setPaymentRequired(false);
        appointmentRepository.save(apt3);
    }

    @Test
    void testGenerateReport_NoFilters() throws Exception {
        mockMvc.perform(get("/api/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kpis").exists())
                .andExpect(jsonPath("$.kpis.totalVisits", is(3)))
                .andExpect(jsonPath("$.kpis.confirmedAppointments", is(1)))
                .andExpect(jsonPath("$.kpis.pendingPayments", is(1)))
                .andExpect(jsonPath("$.kpis.cancelledAppointments", is(1)))
                .andExpect(jsonPath("$.kpis.totalRevenue", is(100.0)))
                .andExpect(jsonPath("$.dailyVisits").isArray())
                .andExpect(jsonPath("$.dailyVisits", hasSize(3)))
                .andExpect(jsonPath("$.departmentBreakdowns").isArray())
                .andExpect(jsonPath("$.departmentBreakdowns", hasSize(2)))
                .andExpect(jsonPath("$.generatedAt").exists());
    }

    @Test
    void testGenerateReport_WithDateRange() throws Exception {
        String startDate = LocalDateTime.now().minusDays(30).toLocalDate().toString();
        String endDate = LocalDateTime.now().toLocalDate().toString();

        mockMvc.perform(get("/api/reports")
                        .param("startDate", startDate)
                        .param("endDate", endDate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kpis.totalVisits", is(3)))
                .andExpect(jsonPath("$.filters.startDate", is(startDate)))
                .andExpect(jsonPath("$.filters.endDate", is(endDate)));
    }

    @Test
    void testGenerateReport_WithHospitalFilter() throws Exception {
        mockMvc.perform(get("/api/reports")
                        .param("hospital", "City Hospital"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kpis.totalVisits", is(2)))
                .andExpect(jsonPath("$.departmentBreakdowns", hasSize(1)))
                .andExpect(jsonPath("$.departmentBreakdowns[0].department", is("Cardiology")))
                .andExpect(jsonPath("$.filters.hospital", is("City Hospital")));
    }

    @Test
    void testGenerateReport_WithDepartmentFilter() throws Exception {
        mockMvc.perform(get("/api/reports")
                        .param("department", "Cardiology"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kpis.totalVisits", is(2)))
                .andExpect(jsonPath("$.departmentBreakdowns", hasSize(1)))
                .andExpect(jsonPath("$.departmentBreakdowns[0].department", is("Cardiology")))
                .andExpect(jsonPath("$.filters.department", is("Cardiology")));
    }

    @Test
    void testGenerateReport_WithHospitalAndDepartmentFilter() throws Exception {
        mockMvc.perform(get("/api/reports")
                        .param("hospital", "City Hospital")
                        .param("department", "Cardiology"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kpis.totalVisits", is(2)))
                .andExpect(jsonPath("$.filters.hospital", is("City Hospital")))
                .andExpect(jsonPath("$.filters.department", is("Cardiology")));
    }

    @Test
    void testGenerateReport_NoDataAvailable() throws Exception {
        String futureStartDate = LocalDateTime.now().plusDays(10).toLocalDate().toString();
        String futureEndDate = LocalDateTime.now().plusDays(20).toLocalDate().toString();

        mockMvc.perform(get("/api/reports")
                        .param("startDate", futureStartDate)
                        .param("endDate", futureEndDate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", 
                    is("No data available for the selected filters and date range")))
                .andExpect(jsonPath("$.kpis.totalVisits", is(0)))
                .andExpect(jsonPath("$.dailyVisits", hasSize(0)))
                .andExpect(jsonPath("$.departmentBreakdowns", hasSize(0)));
    }

    @Test
    void testDepartmentBreakdown_VerifyCalculations() throws Exception {
        mockMvc.perform(get("/api/reports")
                        .param("department", "Cardiology"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.departmentBreakdowns[0].totalAppointments", is(2)))
                .andExpect(jsonPath("$.departmentBreakdowns[0].confirmedAppointments", is(1)))
                .andExpect(jsonPath("$.departmentBreakdowns[0].revenue", is(0)))
                .andExpect(jsonPath("$.departmentBreakdowns[0].completionRate", is(50.0)));
    }

    @Test
    void testDailyVisits_VerifyStructure() throws Exception {
        mockMvc.perform(get("/api/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dailyVisits").isArray())
                .andExpect(jsonPath("$.dailyVisits[0].date").exists())
                .andExpect(jsonPath("$.dailyVisits[0].visitCount").exists())
                .andExpect(jsonPath("$.dailyVisits[0].confirmedCount").exists())
                .andExpect(jsonPath("$.dailyVisits[0].cancelledCount").exists());
    }

    @Test
    void testExportReport_AsPdf() throws Exception {
        ReportFilterRequest filters = new ReportFilterRequest();
        ExportRequest exportRequest = new ExportRequest();
        exportRequest.setFormat("PDF");
        exportRequest.setFilters(filters);

        mockMvc.perform(post("/api/reports/export")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(exportRequest)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }

    @Test
    void testExportReport_AsCsv() throws Exception {
        ReportFilterRequest filters = new ReportFilterRequest();
        ExportRequest exportRequest = new ExportRequest();
        exportRequest.setFormat("CSV");
        exportRequest.setFilters(filters);

        mockMvc.perform(post("/api/reports/export")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(exportRequest)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv"))
                .andExpect(header().exists("Content-Disposition"));
    }

    @Test
    void testExportReport_UnsupportedFormat() throws Exception {
        ReportFilterRequest filters = new ReportFilterRequest();
        ExportRequest exportRequest = new ExportRequest();
        exportRequest.setFormat("XML");
        exportRequest.setFilters(filters);

        mockMvc.perform(post("/api/reports/export")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(exportRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testExportReport_DefaultFormat() throws Exception {
        ReportFilterRequest filters = new ReportFilterRequest();
        ExportRequest exportRequest = new ExportRequest();
        exportRequest.setFilters(filters);
        // No format specified, should default to PDF

        mockMvc.perform(post("/api/reports/export")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(exportRequest)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"));
    }

    @Test
    void testReportKPIs_VerifyCompletionRate() throws Exception {
        mockMvc.perform(get("/api/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kpis.appointmentCompletionRate", 
                    closeTo(33.33, 0.01)));
    }

    @Test
    void testReportKPIs_VerifyAverageWaitTime() throws Exception {
        mockMvc.perform(get("/api/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kpis.averageWaitTime").exists())
                .andExpect(jsonPath("$.kpis.averageWaitTime").isNumber());
    }

    @Test
    void testCorsEnabled() throws Exception {
        mockMvc.perform(options("/api/reports")
                        .header("Origin", "http://example.com")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"));
    }
}
