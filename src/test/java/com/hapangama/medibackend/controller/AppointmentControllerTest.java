package com.hapangama.medibackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hapangama.medibackend.dto.*;
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
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private HealthcareProviderRepository providerRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    private Patient testPatient;
    private HealthcareProvider governmentProvider;
    private HealthcareProvider privateProvider;
    private TimeSlot testTimeSlot;

    @BeforeEach
    void setUp() {
        // Clean up database
        appointmentRepository.deleteAll();
        timeSlotRepository.deleteAll();
        providerRepository.deleteAll();
        patientRepository.deleteAll();

        // Create test patient
        testPatient = new Patient();
        testPatient.setName("John Doe");
        testPatient.setEmail("john@example.com");
        testPatient.setPhone("1234567890");
        testPatient.setDigitalHealthCardNumber("DHC12345");
        testPatient = patientRepository.save(testPatient);

        // Create government provider
        governmentProvider = new HealthcareProvider();
        governmentProvider.setName("Dr. Smith");
        governmentProvider.setSpecialty("Cardiology");
        governmentProvider.setHospitalName("Government Hospital");
        governmentProvider.setHospitalType(HealthcareProvider.HospitalType.GOVERNMENT);
        governmentProvider = providerRepository.save(governmentProvider);

        // Create private provider
        privateProvider = new HealthcareProvider();
        privateProvider.setName("Dr. Johnson");
        privateProvider.setSpecialty("Dermatology");
        privateProvider.setHospitalName("Private Clinic");
        privateProvider.setHospitalType(HealthcareProvider.HospitalType.PRIVATE);
        privateProvider = providerRepository.save(privateProvider);

        // Create test time slot
        testTimeSlot = new TimeSlot();
        testTimeSlot.setProvider(governmentProvider);
        testTimeSlot.setStartTime(LocalDateTime.now().plusDays(1));
        testTimeSlot.setEndTime(LocalDateTime.now().plusDays(1).plusHours(1));
        testTimeSlot.setAvailable(true);
        testTimeSlot = timeSlotRepository.save(testTimeSlot);
    }

    @Test
    void testGetProviders_All() throws Exception {
        mockMvc.perform(get("/api/appointments/providers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is(anyOf(is("Dr. Smith"), is("Dr. Johnson")))))
                .andExpect(jsonPath("$[1].name", is(anyOf(is("Dr. Smith"), is("Dr. Johnson")))));
    }

    @Test
    void testGetProviders_BySpecialty() throws Exception {
        mockMvc.perform(get("/api/appointments/providers")
                        .param("specialty", "Cardiology"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Dr. Smith")))
                .andExpect(jsonPath("$[0].specialty", is("Cardiology")));
    }

    @Test
    void testGetAvailableTimeSlots() throws Exception {
        LocalDateTime date = LocalDateTime.now().plusDays(1);
        String dateStr = date.toString();

        mockMvc.perform(get("/api/appointments/timeslots")
                        .param("providerId", governmentProvider.getId().toString())
                        .param("date", dateStr))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].providerId", is(governmentProvider.getId().intValue())))
                .andExpect(jsonPath("$[0].available", is(true)));
    }

    @Test
    void testBookAppointment_GovernmentHospital_Success() throws Exception {
        BookAppointmentRequest request = new BookAppointmentRequest();
        request.setPatientId(testPatient.getId());
        request.setProviderId(governmentProvider.getId());
        request.setTimeSlotId(testTimeSlot.getId());

        mockMvc.perform(post("/api/appointments/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.patientName", is("John Doe")))
                .andExpect(jsonPath("$.providerName", is("Dr. Smith")))
                .andExpect(jsonPath("$.status", is("CONFIRMED")))
                .andExpect(jsonPath("$.paymentRequired", is(false)))
                .andExpect(jsonPath("$.confirmationNumber", notNullValue()));
    }

    @Test
    void testBookAppointment_PrivateHospital_RequiresPayment() throws Exception {
        TimeSlot privateTimeSlot = new TimeSlot();
        privateTimeSlot.setProvider(privateProvider);
        privateTimeSlot.setStartTime(LocalDateTime.now().plusDays(1));
        privateTimeSlot.setEndTime(LocalDateTime.now().plusDays(1).plusHours(1));
        privateTimeSlot.setAvailable(true);
        privateTimeSlot = timeSlotRepository.save(privateTimeSlot);

        BookAppointmentRequest request = new BookAppointmentRequest();
        request.setPatientId(testPatient.getId());
        request.setProviderId(privateProvider.getId());
        request.setTimeSlotId(privateTimeSlot.getId());

        mockMvc.perform(post("/api/appointments/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.patientName", is("John Doe")))
                .andExpect(jsonPath("$.providerName", is("Dr. Johnson")))
                .andExpect(jsonPath("$.status", is("PENDING_PAYMENT")))
                .andExpect(jsonPath("$.paymentRequired", is(true)))
                .andExpect(jsonPath("$.confirmationNumber", notNullValue()));
    }

    @Test
    void testBookAppointment_TimeSlotNotAvailable() throws Exception {
        // Book the time slot first
        BookAppointmentRequest request1 = new BookAppointmentRequest();
        request1.setPatientId(testPatient.getId());
        request1.setProviderId(governmentProvider.getId());
        request1.setTimeSlotId(testTimeSlot.getId());

        mockMvc.perform(post("/api/appointments/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        // Try to book the same time slot again
        Patient anotherPatient = new Patient();
        anotherPatient.setName("Jane Doe");
        anotherPatient.setEmail("jane@example.com");
        anotherPatient.setPhone("0987654321");
        anotherPatient.setDigitalHealthCardNumber("DHC54321");
        anotherPatient = patientRepository.save(anotherPatient);

        BookAppointmentRequest request2 = new BookAppointmentRequest();
        request2.setPatientId(anotherPatient.getId());
        request2.setProviderId(governmentProvider.getId());
        request2.setTimeSlotId(testTimeSlot.getId());

        mockMvc.perform(post("/api/appointments/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void testProcessPayment_Success() throws Exception {
        // Create a private appointment first
        TimeSlot privateTimeSlot = new TimeSlot();
        privateTimeSlot.setProvider(privateProvider);
        privateTimeSlot.setStartTime(LocalDateTime.now().plusDays(1));
        privateTimeSlot.setEndTime(LocalDateTime.now().plusDays(1).plusHours(1));
        privateTimeSlot.setAvailable(true);
        privateTimeSlot = timeSlotRepository.save(privateTimeSlot);

        Appointment appointment = new Appointment();
        appointment.setPatient(testPatient);
        appointment.setProvider(privateProvider);
        appointment.setTimeSlot(privateTimeSlot);
        appointment.setStatus(Appointment.AppointmentStatus.PENDING_PAYMENT);
        appointment.setBookingDateTime(LocalDateTime.now());
        appointment.setConfirmationNumber("APT-TEST123");
        appointment.setPaymentRequired(true);
        appointment = appointmentRepository.save(appointment);

        privateTimeSlot.setAvailable(false);
        timeSlotRepository.save(privateTimeSlot);

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAppointmentId(appointment.getId());
        paymentRequest.setAmount(new BigDecimal("100.00"));
        paymentRequest.setPaymentMethod(Payment.PaymentMethod.CREDIT_CARD);
        paymentRequest.setCardNumber("1234567890123456");
        paymentRequest.setCvv("123");

        mockMvc.perform(post("/api/appointments/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CONFIRMED")))
                .andExpect(jsonPath("$.paymentRequired", is(true)));
    }

    @Test
    void testGetAppointmentByConfirmationNumber() throws Exception {
        // Book an appointment
        BookAppointmentRequest request = new BookAppointmentRequest();
        request.setPatientId(testPatient.getId());
        request.setProviderId(governmentProvider.getId());
        request.setTimeSlotId(testTimeSlot.getId());

        String response = mockMvc.perform(post("/api/appointments/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        AppointmentResponse appointmentResponse = objectMapper.readValue(response, AppointmentResponse.class);
        String confirmationNumber = appointmentResponse.getConfirmationNumber();

        // Get appointment by confirmation number
        mockMvc.perform(get("/api/appointments/confirmation/" + confirmationNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmationNumber", is(confirmationNumber)))
                .andExpect(jsonPath("$.patientName", is("John Doe")));
    }

    @Test
    void testGetPatientAppointments() throws Exception {
        // Book an appointment
        BookAppointmentRequest request = new BookAppointmentRequest();
        request.setPatientId(testPatient.getId());
        request.setProviderId(governmentProvider.getId());
        request.setTimeSlotId(testTimeSlot.getId());

        mockMvc.perform(post("/api/appointments/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Get patient appointments
        mockMvc.perform(get("/api/appointments/patient/" + testPatient.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].patientName", is("John Doe")));
    }
}
