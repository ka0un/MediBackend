package com.hapangama.medibackend.controller;

import com.hapangama.medibackend.model.*;
import com.hapangama.medibackend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private HealthcareProviderRepository providerRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        // Clean up database
        appointmentRepository.deleteAll();
        timeSlotRepository.deleteAll();
        providerRepository.deleteAll();
        patientRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testGetDashboard_Success() throws Exception {
        // Create test data
        createTestPatient("patient1", "patient1@test.com", "DHC-001");
        createTestPatient("patient2", "patient2@test.com", "DHC-002");

        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPatients", is(2)))
                .andExpect(jsonPath("$.totalAppointments", is(0)))
                .andExpect(jsonPath("$.recentPatients", hasSize(2)))
                .andExpect(jsonPath("$.recentAppointments", hasSize(0)));
    }

    @Test
    void testGetDashboard_WithAppointments() throws Exception {
        // Create test data
        Patient patient = createTestPatient("patient1", "patient1@test.com", "DHC-001");
        HealthcareProvider provider = createTestProvider();
        TimeSlot timeSlot = createTestTimeSlot(provider);
        createTestAppointment(patient, provider, timeSlot);

        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPatients", is(1)))
                .andExpect(jsonPath("$.totalAppointments", is(1)))
                .andExpect(jsonPath("$.recentPatients", hasSize(1)))
                .andExpect(jsonPath("$.recentAppointments", hasSize(1)));
    }

    @Test
    void testGetAllPatients_Success() throws Exception {
        // Create test patients
        createTestPatient("patient1", "patient1@test.com", "DHC-001");
        createTestPatient("patient2", "patient2@test.com", "DHC-002");
        createTestPatient("patient3", "patient3@test.com", "DHC-003");

        mockMvc.perform(get("/api/admin/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].email", notNullValue()))
                .andExpect(jsonPath("$[1].email", notNullValue()))
                .andExpect(jsonPath("$[2].email", notNullValue()));
    }

    @Test
    void testGetAllPatients_Empty() throws Exception {
        mockMvc.perform(get("/api/admin/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testGetAllAppointments_Success() throws Exception {
        // Create test data
        Patient patient1 = createTestPatient("patient1", "patient1@test.com", "DHC-001");
        Patient patient2 = createTestPatient("patient2", "patient2@test.com", "DHC-002");
        HealthcareProvider provider = createTestProvider();
        TimeSlot timeSlot1 = createTestTimeSlot(provider);
        TimeSlot timeSlot2 = createTestTimeSlot(provider);
        
        createTestAppointment(patient1, provider, timeSlot1);
        createTestAppointment(patient2, provider, timeSlot2);

        mockMvc.perform(get("/api/admin/appointments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].patientName", notNullValue()))
                .andExpect(jsonPath("$[1].patientName", notNullValue()));
    }

    @Test
    void testGetAllAppointments_Empty() throws Exception {
        mockMvc.perform(get("/api/admin/appointments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testDeletePatient_Success() throws Exception {
        // Create test patient
        Patient patient = createTestPatient("patient1", "patient1@test.com", "DHC-001");

        mockMvc.perform(delete("/api/admin/patients/" + patient.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Patient deleted successfully")));

        // Verify patient is deleted
        mockMvc.perform(get("/api/admin/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testDeletePatient_NotFound() throws Exception {
        mockMvc.perform(delete("/api/admin/patients/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCancelAppointment_Success() throws Exception {
        // Create test appointment
        Patient patient = createTestPatient("patient1", "patient1@test.com", "DHC-001");
        HealthcareProvider provider = createTestProvider();
        TimeSlot timeSlot = createTestTimeSlot(provider);
        Appointment appointment = createTestAppointment(patient, provider, timeSlot);

        mockMvc.perform(delete("/api/admin/appointments/" + appointment.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Appointment cancelled successfully")));
    }

    @Test
    void testCancelAppointment_NotFound() throws Exception {
        mockMvc.perform(delete("/api/admin/appointments/999"))
                .andExpect(status().isNotFound());
    }

    // Healthcare Provider Management Tests
    @Test
    void testCreateProvider_Success() throws Exception {
        String providerJson = """
            {
                "name": "Dr. New Provider",
                "specialty": "Cardiology",
                "hospitalName": "Heart Hospital",
                "hospitalType": "PRIVATE"
            }
            """;

        mockMvc.perform(post("/api/admin/providers")
                        .contentType("application/json")
                        .content(providerJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Dr. New Provider")))
                .andExpect(jsonPath("$.specialty", is("Cardiology")))
                .andExpect(jsonPath("$.hospitalName", is("Heart Hospital")))
                .andExpect(jsonPath("$.hospitalType", is("PRIVATE")));
    }

    @Test
    void testGetProvider_Success() throws Exception {
        HealthcareProvider provider = createTestProvider();

        mockMvc.perform(get("/api/admin/providers/" + provider.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Dr. Test")))
                .andExpect(jsonPath("$.specialty", is("General Medicine")))
                .andExpect(jsonPath("$.hospitalName", is("Test Hospital")));
    }

    @Test
    void testGetProvider_NotFound() throws Exception {
        mockMvc.perform(get("/api/admin/providers/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateProvider_Success() throws Exception {
        HealthcareProvider provider = createTestProvider();

        String updateJson = """
            {
                "name": "Dr. Updated",
                "specialty": "Neurology"
            }
            """;

        mockMvc.perform(put("/api/admin/providers/" + provider.getId())
                        .contentType("application/json")
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Dr. Updated")))
                .andExpect(jsonPath("$.specialty", is("Neurology")))
                .andExpect(jsonPath("$.hospitalName", is("Test Hospital")));
    }

    @Test
    void testUpdateProvider_NotFound() throws Exception {
        String updateJson = """
            {
                "name": "Dr. Updated"
            }
            """;

        mockMvc.perform(put("/api/admin/providers/999")
                        .contentType("application/json")
                        .content(updateJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteProvider_Success() throws Exception {
        HealthcareProvider provider = createTestProvider();

        mockMvc.perform(delete("/api/admin/providers/" + provider.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Provider deleted successfully")));
    }

    @Test
    void testDeleteProvider_NotFound() throws Exception {
        mockMvc.perform(delete("/api/admin/providers/999"))
                .andExpect(status().isNotFound());
    }

    // Time Slot Management Tests
    @Test
    void testCreateTimeSlot_Success() throws Exception {
        HealthcareProvider provider = createTestProvider();

        String timeSlotJson = """
            {
                "startTime": "2025-12-01T09:00:00",
                "endTime": "2025-12-01T10:00:00",
                "available": true
            }
            """;

        mockMvc.perform(post("/api/admin/providers/" + provider.getId() + "/timeslots")
                        .contentType("application/json")
                        .content(timeSlotJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.providerId", is(provider.getId().intValue())))
                .andExpect(jsonPath("$.available", is(true)));
    }

    @Test
    void testCreateTimeSlot_ProviderNotFound() throws Exception {
        String timeSlotJson = """
            {
                "startTime": "2025-12-01T09:00:00",
                "endTime": "2025-12-01T10:00:00",
                "available": true
            }
            """;

        mockMvc.perform(post("/api/admin/providers/999/timeslots")
                        .contentType("application/json")
                        .content(timeSlotJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetProviderTimeSlots_Success() throws Exception {
        HealthcareProvider provider = createTestProvider();
        createTestTimeSlot(provider);
        createTestTimeSlot(provider);

        mockMvc.perform(get("/api/admin/providers/" + provider.getId() + "/timeslots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void testGetProviderTimeSlots_Empty() throws Exception {
        HealthcareProvider provider = createTestProvider();

        mockMvc.perform(get("/api/admin/providers/" + provider.getId() + "/timeslots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testUpdateTimeSlot_Success() throws Exception {
        HealthcareProvider provider = createTestProvider();
        TimeSlot timeSlot = createTestTimeSlot(provider);

        String updateJson = """
            {
                "available": false
            }
            """;

        mockMvc.perform(put("/api/admin/timeslots/" + timeSlot.getId())
                        .contentType("application/json")
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available", is(false)));
    }

    @Test
    void testUpdateTimeSlot_NotFound() throws Exception {
        String updateJson = """
            {
                "available": false
            }
            """;

        mockMvc.perform(put("/api/admin/timeslots/999")
                        .contentType("application/json")
                        .content(updateJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteTimeSlot_Success() throws Exception {
        HealthcareProvider provider = createTestProvider();
        TimeSlot timeSlot = createTestTimeSlot(provider);

        mockMvc.perform(delete("/api/admin/timeslots/" + timeSlot.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Time slot deleted successfully")));
    }

    @Test
    void testDeleteTimeSlot_NotFound() throws Exception {
        mockMvc.perform(delete("/api/admin/timeslots/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateAppointmentStatus_Success() throws Exception {
        // Arrange: create appointment in CONFIRMED
        Patient patient = createTestPatient("patient1", "patient1@test.com", "DHC-001");
        HealthcareProvider provider = createTestProvider();
        TimeSlot timeSlot = createTestTimeSlot(provider);
        Appointment appointment = createTestAppointment(patient, provider, timeSlot);

        String body = "{\n  \"status\": \"COMPLETED\"\n}";

        // Act & Assert
        mockMvc.perform(put("/api/admin/appointments/" + appointment.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(appointment.getId().intValue())))
                .andExpect(jsonPath("$.status", is("COMPLETED")));
    }

    // Helper methods
    private Patient createTestPatient(String username, String email, String cardNumber) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRole(User.Role.PATIENT);
        user.setActive(true);
        user = userRepository.save(user);

        Patient patient = new Patient();
        patient.setUser(user);
        patient.setName("Test Patient " + username);
        patient.setEmail(email);
        patient.setPhone("1234567890");
        patient.setDigitalHealthCardNumber(cardNumber);
        patient.setAddress("123 Test St");
        patient.setDateOfBirth(LocalDate.of(1990, 1, 1));
        return patientRepository.save(patient);
    }

    private HealthcareProvider createTestProvider() {
        HealthcareProvider provider = new HealthcareProvider();
        provider.setName("Dr. Test");
        provider.setSpecialty("General Medicine");
        provider.setHospitalName("Test Hospital");
        provider.setHospitalType(HealthcareProvider.HospitalType.GOVERNMENT);
        return providerRepository.save(provider);
    }

    private TimeSlot createTestTimeSlot(HealthcareProvider provider) {
        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setProvider(provider);
        timeSlot.setStartTime(LocalDateTime.now().plusDays(1));
        timeSlot.setEndTime(LocalDateTime.now().plusDays(1).plusHours(1));
        timeSlot.setAvailable(true);
        return timeSlotRepository.save(timeSlot);
    }

    private Appointment createTestAppointment(Patient patient, HealthcareProvider provider, TimeSlot timeSlot) {
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setProvider(provider);
        appointment.setTimeSlot(timeSlot);
        appointment.setBookingDateTime(LocalDateTime.now());
        appointment.setConfirmationNumber("TEST-" + System.currentTimeMillis());
        appointment.setStatus(Appointment.AppointmentStatus.CONFIRMED);
        appointment.setPaymentRequired(false);
        return appointmentRepository.save(appointment);
    }
}
