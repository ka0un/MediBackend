package com.hapangama.medibackend.service;

import com.hapangama.medibackend.dto.CreatePatientRequest;
import com.hapangama.medibackend.dto.PatientProfileResponse;
import com.hapangama.medibackend.dto.UpdatePatientRequest;
import com.hapangama.medibackend.model.AuditLog;
import com.hapangama.medibackend.model.Patient;
import com.hapangama.medibackend.repository.AppointmentRepository;
import com.hapangama.medibackend.repository.AuditLogRepository;
import com.hapangama.medibackend.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private PatientService patientService;

    private Patient testPatient;
    private CreatePatientRequest createRequest;
    private UpdatePatientRequest updateRequest;

    @BeforeEach
    void setUp() {
        testPatient = new Patient();
        testPatient.setId(1L);
        testPatient.setName("John Doe");
        testPatient.setEmail("john@example.com");
        testPatient.setPhone("1234567890");
        testPatient.setDigitalHealthCardNumber("DHC12345");
        testPatient.setAddress("123 Main St");
        testPatient.setDateOfBirth(LocalDate.of(1990, 1, 1));
        testPatient.setEmergencyContactName("Jane Doe");
        testPatient.setEmergencyContactPhone("0987654321");
        testPatient.setMedicalHistory("No major health issues");
        testPatient.setBloodType("O+");
        testPatient.setAllergies("None");

        createRequest = new CreatePatientRequest();
        createRequest.setName("John Doe");
        createRequest.setEmail("john@example.com");
        createRequest.setPhone("1234567890");
        createRequest.setDigitalHealthCardNumber("DHC12345");
        createRequest.setAddress("123 Main St");
        createRequest.setDateOfBirth(LocalDate.of(1990, 1, 1));

        updateRequest = new UpdatePatientRequest();
        updateRequest.setName("John Updated");
        updateRequest.setPhone("9999999999");
    }

    @Test
    void testCreatePatient_Success() {
        when(patientRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(patientRepository.findByDigitalHealthCardNumber(any())).thenReturn(Optional.empty());
        when(patientRepository.save(any(Patient.class))).thenReturn(testPatient);

        PatientProfileResponse response = patientService.createPatient(createRequest);

        assertNotNull(response);
        assertEquals("John Doe", response.getName());
        assertEquals("john@example.com", response.getEmail());
        verify(patientRepository, times(1)).save(any(Patient.class));
        verify(auditService, times(1)).logAsync(any(AuditService.AuditLogBuilder.class));
    }

    @Test
    void testCreatePatient_EmailAlreadyExists() {
        when(patientRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testPatient));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            patientService.createPatient(createRequest);
        });

        assertEquals("Email already exists", exception.getMessage());
        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    void testCreatePatient_DigitalHealthCardNumberAlreadyExists() {
        when(patientRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(patientRepository.findByDigitalHealthCardNumber("DHC12345")).thenReturn(Optional.of(testPatient));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            patientService.createPatient(createRequest);
        });

        assertEquals("Digital Health Card Number already exists", exception.getMessage());
        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    void testCreatePatient_MissingRequiredFields() {
        CreatePatientRequest invalidRequest = new CreatePatientRequest();
        invalidRequest.setEmail("test@test.com");
        invalidRequest.setPhone("1234567890");
        invalidRequest.setDigitalHealthCardNumber("DHC123");
        // Name is missing

        when(patientRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(patientRepository.findByDigitalHealthCardNumber(any())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            patientService.createPatient(invalidRequest);
        });

        assertTrue(exception.getMessage().contains("Missing Required Fields"));
        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    void testGetPatientProfile_Success() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));

        PatientProfileResponse response = patientService.getPatientProfile(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("John Doe", response.getName());
        assertEquals("john@example.com", response.getEmail());
        verify(patientRepository, times(1)).findById(1L);
    }

    @Test
    void testGetPatientProfile_NotFound() {
        when(patientRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            patientService.getPatientProfile(999L);
        });

        assertEquals("Patient not found", exception.getMessage());
    }

    @Test
    void testUpdatePatientProfile_Success() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(patientRepository.save(any(Patient.class))).thenReturn(testPatient);

        PatientProfileResponse response = patientService.updatePatientProfile(1L, updateRequest);

        assertNotNull(response);
        verify(patientRepository, times(1)).save(any(Patient.class));
        verify(auditService, times(1)).logAsync(any(AuditService.AuditLogBuilder.class));
    }

    @Test
    void testUpdatePatientProfile_NotFound() {
        when(patientRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            patientService.updatePatientProfile(999L, updateRequest);
        });

        assertEquals("Patient not found", exception.getMessage());
        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    void testUpdatePatientProfile_EmailAlreadyExists() {
        Patient anotherPatient = new Patient();
        anotherPatient.setId(2L);
        anotherPatient.setEmail("another@example.com");

        UpdatePatientRequest request = new UpdatePatientRequest();
        request.setEmail("another@example.com");

        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(patientRepository.findByEmail("another@example.com")).thenReturn(Optional.of(anotherPatient));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            patientService.updatePatientProfile(1L, request);
        });

        assertEquals("Email already exists", exception.getMessage());
    }

    @Test
    void testDeletePatient_Success() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(appointmentRepository.findByPatientId(1L)).thenReturn(Collections.emptyList());
        doNothing().when(patientRepository).delete(any(Patient.class));

        patientService.deletePatient(1L);

        verify(appointmentRepository, times(1)).findByPatientId(1L);
        verify(patientRepository, times(1)).delete(testPatient);
        verify(auditService, times(1)).logAsync(any(AuditService.AuditLogBuilder.class));
    }

    @Test
    void testDeletePatient_NotFound() {
        when(patientRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            patientService.deletePatient(999L);
        });

        assertEquals("Patient not found", exception.getMessage());
        verify(patientRepository, never()).delete(any(Patient.class));
    }
}
