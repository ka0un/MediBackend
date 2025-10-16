package com.hapangama.medibackend.controller;

import com.hapangama.medibackend.model.*;
import com.hapangama.medibackend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
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
