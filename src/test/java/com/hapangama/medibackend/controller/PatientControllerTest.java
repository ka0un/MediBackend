package com.hapangama.medibackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hapangama.medibackend.dto.CreatePatientRequest;
import com.hapangama.medibackend.dto.UpdatePatientRequest;
import com.hapangama.medibackend.model.Patient;
import com.hapangama.medibackend.repository.AppointmentRepository;
import com.hapangama.medibackend.repository.AuditLogRepository;
import com.hapangama.medibackend.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @BeforeEach
    void setUp() {
        // Clean up database - delete in order to respect FK constraints
        appointmentRepository.deleteAll();
        auditLogRepository.deleteAll();
        patientRepository.deleteAll();
    }

    @Test
    void testCreatePatient_Success() throws Exception {
        CreatePatientRequest request = new CreatePatientRequest();
        request.setName("John Doe");
        request.setEmail("john@example.com");
        request.setPhone("1234567890");
        request.setDigitalHealthCardNumber("DHC12345");
        request.setAddress("123 Main St");
        request.setDateOfBirth(LocalDate.of(1990, 1, 1));
        request.setEmergencyContactName("Jane Doe");
        request.setEmergencyContactPhone("0987654321");

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.email", is("john@example.com")))
                .andExpect(jsonPath("$.phone", is("1234567890")))
                .andExpect(jsonPath("$.digitalHealthCardNumber", is("DHC12345")))
                .andExpect(jsonPath("$.address", is("123 Main St")))
                .andExpect(jsonPath("$.emergencyContactName", is("Jane Doe")));
    }

    @Test
    void testCreatePatient_EmailAlreadyExists() throws Exception {
        // Create first patient
        Patient existingPatient = new Patient();
        existingPatient.setName("Existing Patient");
        existingPatient.setEmail("existing@example.com");
        existingPatient.setPhone("1111111111");
        existingPatient.setDigitalHealthCardNumber("DHC11111");
        patientRepository.save(existingPatient);

        // Try to create patient with same email
        CreatePatientRequest request = new CreatePatientRequest();
        request.setName("New Patient");
        request.setEmail("existing@example.com");
        request.setPhone("2222222222");
        request.setDigitalHealthCardNumber("DHC22222");

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Email already exists")));
    }

    @Test
    void testCreatePatient_MissingRequiredFields() throws Exception {
        CreatePatientRequest request = new CreatePatientRequest();
        request.setEmail("test@example.com");
        request.setPhone("1234567890");
        request.setDigitalHealthCardNumber("DHC12345");
        // Name is missing

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Missing Required Fields")));
    }

    @Test
    void testGetPatientProfile_Success() throws Exception {
        Patient patient = new Patient();
        patient.setName("John Doe");
        patient.setEmail("john@example.com");
        patient.setPhone("1234567890");
        patient.setDigitalHealthCardNumber("DHC12345");
        patient.setAddress("123 Main St");
        patient.setDateOfBirth(LocalDate.of(1990, 1, 1));
        patient = patientRepository.save(patient);

        mockMvc.perform(get("/api/patients/" + patient.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(patient.getId().intValue())))
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.email", is("john@example.com")))
                .andExpect(jsonPath("$.address", is("123 Main St")));
    }

    @Test
    void testGetPatientProfile_NotFound() throws Exception {
        mockMvc.perform(get("/api/patients/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Patient not found")));
    }

    @Test
    void testGetAllPatients() throws Exception {
        Patient patient1 = new Patient();
        patient1.setName("John Doe");
        patient1.setEmail("john@example.com");
        patient1.setPhone("1234567890");
        patient1.setDigitalHealthCardNumber("DHC12345");
        patientRepository.save(patient1);

        Patient patient2 = new Patient();
        patient2.setName("Jane Smith");
        patient2.setEmail("jane@example.com");
        patient2.setPhone("0987654321");
        patient2.setDigitalHealthCardNumber("DHC54321");
        patientRepository.save(patient2);

        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is(anyOf(is("John Doe"), is("Jane Smith")))))
                .andExpect(jsonPath("$[1].name", is(anyOf(is("John Doe"), is("Jane Smith")))));
    }

    @Test
    void testUpdatePatientProfile_Success() throws Exception {
        Patient patient = new Patient();
        patient.setName("John Doe");
        patient.setEmail("john@example.com");
        patient.setPhone("1234567890");
        patient.setDigitalHealthCardNumber("DHC12345");
        patient.setAddress("123 Main St");
        patient = patientRepository.save(patient);

        UpdatePatientRequest updateRequest = new UpdatePatientRequest();
        updateRequest.setName("John Updated");
        updateRequest.setPhone("9999999999");
        updateRequest.setAddress("456 New St");

        mockMvc.perform(put("/api/patients/" + patient.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("John Updated")))
                .andExpect(jsonPath("$.phone", is("9999999999")))
                .andExpect(jsonPath("$.address", is("456 New St")))
                .andExpect(jsonPath("$.email", is("john@example.com"))); // Email unchanged
    }

    @Test
    void testUpdatePatientProfile_NotFound() throws Exception {
        UpdatePatientRequest updateRequest = new UpdatePatientRequest();
        updateRequest.setName("John Updated");

        mockMvc.perform(put("/api/patients/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Patient not found")));
    }

    @Test
    void testUpdatePatientProfile_EmailAlreadyExists() throws Exception {
        Patient patient1 = new Patient();
        patient1.setName("John Doe");
        patient1.setEmail("john@example.com");
        patient1.setPhone("1234567890");
        patient1.setDigitalHealthCardNumber("DHC12345");
        patient1 = patientRepository.save(patient1);

        Patient patient2 = new Patient();
        patient2.setName("Jane Smith");
        patient2.setEmail("jane@example.com");
        patient2.setPhone("0987654321");
        patient2.setDigitalHealthCardNumber("DHC54321");
        patientRepository.save(patient2);

        UpdatePatientRequest updateRequest = new UpdatePatientRequest();
        updateRequest.setEmail("jane@example.com");

        mockMvc.perform(put("/api/patients/" + patient1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Email already exists")));
    }

//    @Test
//    void testDeletePatient_Success() throws Exception {
//        Patient patient = new Patient();
//        patient.setName("John Doe");
//        patient.setEmail("john@example.com");
//        patient.setPhone("1234567890");
//        patient.setDigitalHealthCardNumber("DHC12345");
//        patient = patientRepository.save(patient);
//
//        mockMvc.perform(delete("/api/patients/" + patient.getId()))
//                .andExpect(status().isNoContent());
//
//        // Verify patient was deleted
//        mockMvc.perform(get("/api/patients/" + patient.getId()))
//                .andExpect(status().isNotFound());
//    }

    @Test
    void testDeletePatient_NotFound() throws Exception {
        mockMvc.perform(delete("/api/patients/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Patient not found")));
    }
}
