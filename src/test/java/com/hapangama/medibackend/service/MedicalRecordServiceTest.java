package com.hapangama.medibackend.service;

import com.hapangama.medibackend.dto.AddPrescriptionRequest;
import com.hapangama.medibackend.dto.MedicalRecordResponse;
import com.hapangama.medibackend.dto.ScanCardRequest;
import com.hapangama.medibackend.exception.NotFoundException;
import com.hapangama.medibackend.model.*;
import com.hapangama.medibackend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicalRecordServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private MedicationRepository medicationRepository;

    @Mock
    private PrescriptionRepository prescriptionRepository;

    @Mock
    private TestResultRepository testResultRepository;

    @Mock
    private VaccinationRepository vaccinationRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private MedicalRecordAccessLogRepository accessLogRepository;

    @Mock
    private PrivacyService privacyService;

    @InjectMocks
    private MedicalRecordService medicalRecordService;

    private Patient testPatient;
    private Medication testMedication;
    private Prescription testPrescription;
    private TestResult testTestResult;
    private Vaccination testVaccination;
    private Appointment testAppointment;
    private HealthcareProvider testProvider;
    private TimeSlot testTimeSlot;

    @BeforeEach
    void setUp() {
        // Set up test patient
        testPatient = new Patient();
        testPatient.setId(1L);
        testPatient.setName("John Doe");
        testPatient.setEmail("john@example.com");
        testPatient.setPhone("1234567890");
        testPatient.setDigitalHealthCardNumber("DHC-2024-001");
        testPatient.setAddress("123 Main St");
        testPatient.setDateOfBirth(LocalDate.of(1990, 1, 1));
        testPatient.setBloodType("O+");
        testPatient.setAllergies("Penicillin");
        testPatient.setMedicalHistory("Hypertension");
        testPatient.setEmergencyContactName("Jane Doe");
        testPatient.setEmergencyContactPhone("0987654321");

        // Set up test medication
        testMedication = new Medication();
        testMedication.setId(1L);
        testMedication.setPatient(testPatient);
        testMedication.setMedicationName("Lisinopril");
        testMedication.setDosage("10mg");
        testMedication.setFrequency("Once daily");
        testMedication.setStartDate(LocalDate.now().minusMonths(3));
        testMedication.setPrescribedBy("Dr. Smith");
        testMedication.setActive(true);

        // Set up test prescription
        testPrescription = new Prescription();
        testPrescription.setId(1L);
        testPrescription.setPatient(testPatient);
        testPrescription.setPrescribedBy("Dr. Smith");
        testPrescription.setPrescriptionDate(LocalDateTime.now().minusDays(10));
        testPrescription.setDiagnosis("Hypertension");
        testPrescription.setTreatment("Continue medication");
        testPrescription.setMedications("Lisinopril 10mg");
        testPrescription.setNotes("Patient responding well");

        // Set up test test result
        testTestResult = new TestResult();
        testTestResult.setId(1L);
        testTestResult.setPatient(testPatient);
        testTestResult.setTestName("Blood Pressure");
        testTestResult.setTestDate(LocalDate.now().minusDays(7));
        testTestResult.setResult("120/80");
        testTestResult.setResultUnit("mmHg");
        testTestResult.setReferenceRange("< 120/80");
        testTestResult.setOrderedBy("Dr. Smith");

        // Set up test vaccination
        testVaccination = new Vaccination();
        testVaccination.setId(1L);
        testVaccination.setPatient(testPatient);
        testVaccination.setVaccineName("Influenza");
        testVaccination.setVaccinationDate(LocalDate.now().minusMonths(6));
        testVaccination.setBatchNumber("FLU2024-001");
        testVaccination.setManufacturer("Pfizer");
        testVaccination.setAdministeredBy("Nurse Williams");

        // Set up test provider and appointment
        testProvider = new HealthcareProvider();
        testProvider.setId(1L);
        testProvider.setName("Dr. Smith");
        testProvider.setSpecialty("Cardiology");
        testProvider.setHospitalName("City Hospital");
        testProvider.setHospitalType(HealthcareProvider.HospitalType.GOVERNMENT);

        testTimeSlot = new TimeSlot();
        testTimeSlot.setId(1L);
        testTimeSlot.setProvider(testProvider);
        testTimeSlot.setStartTime(LocalDateTime.now().minusDays(5));
        testTimeSlot.setEndTime(LocalDateTime.now().minusDays(5).plusHours(1));
        testTimeSlot.setAvailable(false);

        testAppointment = new Appointment();
        testAppointment.setId(1L);
        testAppointment.setPatient(testPatient);
        testAppointment.setProvider(testProvider);
        testAppointment.setTimeSlot(testTimeSlot);
        testAppointment.setStatus(Appointment.AppointmentStatus.CONFIRMED);
        testAppointment.setBookingDateTime(LocalDateTime.now().minusDays(10));
        testAppointment.setConfirmationNumber("CONF-001");
    }

    @Test
    void testAccessMedicalRecordsByCardNumber_Success() {
        // Arrange
        ScanCardRequest request = new ScanCardRequest();
        request.setCardNumber("DHC-2024-001");
        request.setStaffId("STAFF-001");
        request.setPurpose("General consultation");

        when(patientRepository.findByDigitalHealthCardNumber("DHC-2024-001"))
                .thenReturn(Optional.of(testPatient));
        doNothing().when(privacyService).validateAccess(any(Patient.class), anyString());
        when(medicationRepository.findByPatientIdAndActiveTrue(1L))
                .thenReturn(List.of(testMedication));
        when(appointmentRepository.findByPatientId(1L))
                .thenReturn(List.of(testAppointment));
        when(prescriptionRepository.findByPatientIdOrderByPrescriptionDateDesc(1L))
                .thenReturn(List.of(testPrescription));
        when(testResultRepository.findByPatientIdOrderByTestDateDesc(1L))
                .thenReturn(List.of(testTestResult));
        when(vaccinationRepository.findByPatientIdOrderByVaccinationDateDesc(1L))
                .thenReturn(List.of(testVaccination));
        when(accessLogRepository.save(any(MedicalRecordAccessLog.class)))
                .thenReturn(new MedicalRecordAccessLog());

        // Act
        MedicalRecordResponse response = medicalRecordService.accessMedicalRecordsByCardNumber(request);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getPatientId());
        assertEquals("John Doe", response.getName());
        assertEquals("DHC-2024-001", response.getDigitalHealthCardNumber());
        assertEquals("O+", response.getBloodType());
        assertEquals("Penicillin", response.getAllergies());
        assertEquals(1, response.getCurrentMedications().size());
        assertEquals("Lisinopril", response.getCurrentMedications().get(0).getMedicationName());
        assertEquals(1, response.getPreviousVisits().size());
        assertEquals(1, response.getPrescriptions().size());
        assertEquals(1, response.getTestResults().size());
        assertEquals(1, response.getVaccinations().size());
        assertEquals("STAFF-001", response.getAccessedBy());
        verify(accessLogRepository, times(1)).save(any(MedicalRecordAccessLog.class));
    }

    @Test
    void testAccessMedicalRecordsByCardNumber_PatientNotFound() {
        // Arrange
        ScanCardRequest request = new ScanCardRequest();
        request.setCardNumber("INVALID-CARD");
        request.setStaffId("STAFF-001");
        request.setPurpose("General consultation");

        when(patientRepository.findByDigitalHealthCardNumber("INVALID-CARD"))
                .thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            medicalRecordService.accessMedicalRecordsByCardNumber(request);
        });

        assertTrue(exception.getMessage().contains("Patient not found with card number: INVALID-CARD"));
        verify(accessLogRepository, never()).save(any(MedicalRecordAccessLog.class));
    }

    @Test
    void testAccessMedicalRecordsByPatientId_Success() {
        // Arrange
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        doNothing().when(privacyService).validateAccess(any(Patient.class), anyString());
        when(medicationRepository.findByPatientIdAndActiveTrue(1L))
                .thenReturn(List.of(testMedication));
        when(appointmentRepository.findByPatientId(1L))
                .thenReturn(List.of(testAppointment));
        when(prescriptionRepository.findByPatientIdOrderByPrescriptionDateDesc(1L))
                .thenReturn(List.of(testPrescription));
        when(testResultRepository.findByPatientIdOrderByTestDateDesc(1L))
                .thenReturn(List.of(testTestResult));
        when(vaccinationRepository.findByPatientIdOrderByVaccinationDateDesc(1L))
                .thenReturn(List.of(testVaccination));
        when(accessLogRepository.save(any(MedicalRecordAccessLog.class)))
                .thenReturn(new MedicalRecordAccessLog());

        // Act
        MedicalRecordResponse response = medicalRecordService.accessMedicalRecordsByPatientId(
                1L, "STAFF-002", "Follow-up visit");

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getPatientId());
        assertEquals("John Doe", response.getName());
        assertEquals("STAFF-002", response.getAccessedBy());
        verify(accessLogRepository, times(1)).save(any(MedicalRecordAccessLog.class));
    }

    @Test
    void testAccessMedicalRecordsByPatientId_PatientNotFound() {
        // Arrange
        when(patientRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            medicalRecordService.accessMedicalRecordsByPatientId(999L, "STAFF-002", "Consultation");
        });

        assertEquals("Patient not found with ID: 999", exception.getMessage());
        verify(accessLogRepository, never()).save(any(MedicalRecordAccessLog.class));
    }

    @Test
    void testAddPrescription_Success() {
        // Arrange
        AddPrescriptionRequest request = new AddPrescriptionRequest();
        request.setPatientId(1L);
        request.setStaffId("Dr. Johnson");
        request.setDiagnosis("Seasonal Allergies");
        request.setTreatment("Antihistamines and rest");
        request.setMedications("Cetirizine 10mg once daily");
        request.setNotes("Review in 2 weeks");
        request.setFollowUpDate(LocalDateTime.now().plusWeeks(2));

        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        doNothing().when(privacyService).validateAccess(any(Patient.class), anyString());
        when(prescriptionRepository.save(any(Prescription.class))).thenReturn(testPrescription);
        when(medicationRepository.findByPatientIdAndActiveTrue(1L))
                .thenReturn(List.of(testMedication));
        when(appointmentRepository.findByPatientId(1L))
                .thenReturn(List.of(testAppointment));
        when(prescriptionRepository.findByPatientIdOrderByPrescriptionDateDesc(1L))
                .thenReturn(List.of(testPrescription));
        when(testResultRepository.findByPatientIdOrderByTestDateDesc(1L))
                .thenReturn(List.of(testTestResult));
        when(vaccinationRepository.findByPatientIdOrderByVaccinationDateDesc(1L))
                .thenReturn(List.of(testVaccination));
        when(accessLogRepository.save(any(MedicalRecordAccessLog.class)))
                .thenReturn(new MedicalRecordAccessLog());

        // Act
        MedicalRecordResponse response = medicalRecordService.addPrescription(request);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getPatientId());
        verify(prescriptionRepository, times(1)).save(any(Prescription.class));
        verify(accessLogRepository, times(1)).save(any(MedicalRecordAccessLog.class));
    }

    @Test
    void testAddPrescription_PatientNotFound() {
        // Arrange
        AddPrescriptionRequest request = new AddPrescriptionRequest();
        request.setPatientId(999L);
        request.setStaffId("Dr. Johnson");
        request.setDiagnosis("Test");

        when(patientRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            medicalRecordService.addPrescription(request);
        });

        assertEquals("Patient not found with ID: 999", exception.getMessage());
        verify(prescriptionRepository, never()).save(any(Prescription.class));
    }

    @Test
    void testDownloadMedicalRecordsAsPdf_Success() {
        // Arrange
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        doNothing().when(privacyService).validateAccess(any(Patient.class), anyString());
        when(medicationRepository.findByPatientIdAndActiveTrue(1L))
                .thenReturn(List.of(testMedication));
        when(appointmentRepository.findByPatientId(1L))
                .thenReturn(new ArrayList<>());
        when(prescriptionRepository.findByPatientIdOrderByPrescriptionDateDesc(1L))
                .thenReturn(List.of(testPrescription));
        when(testResultRepository.findByPatientIdOrderByTestDateDesc(1L))
                .thenReturn(List.of(testTestResult));
        when(vaccinationRepository.findByPatientIdOrderByVaccinationDateDesc(1L))
                .thenReturn(List.of(testVaccination));
        when(accessLogRepository.save(any(MedicalRecordAccessLog.class)))
                .thenReturn(new MedicalRecordAccessLog());

        // Act
        byte[] pdfData = medicalRecordService.downloadMedicalRecordsAsPdf(1L, "STAFF-003", "Patient copy");

        // Assert
        assertNotNull(pdfData);
        assertTrue(pdfData.length > 0);
        verify(accessLogRepository, times(1)).save(any(MedicalRecordAccessLog.class));
    }

    @Test
    void testDownloadMedicalRecordsAsPdf_PatientNotFound() {
        // Arrange
        when(patientRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            medicalRecordService.downloadMedicalRecordsAsPdf(999L, "STAFF-003", "Patient copy");
        });

        assertEquals("Patient not found with ID: 999", exception.getMessage());
    }

    @Test
    void testGetAccessLogs_Success() {
        // Arrange
        MedicalRecordAccessLog log1 = new MedicalRecordAccessLog();
        log1.setId(1L);
        log1.setPatientId(1L);
        log1.setStaffId("STAFF-001");
        log1.setAccessType("VIEW");
        log1.setAccessTimestamp(LocalDateTime.now().minusHours(2));
        log1.setPurpose("Consultation");
        log1.setAccessGranted(true);

        MedicalRecordAccessLog log2 = new MedicalRecordAccessLog();
        log2.setId(2L);
        log2.setPatientId(1L);
        log2.setStaffId("STAFF-002");
        log2.setAccessType("DOWNLOAD");
        log2.setAccessTimestamp(LocalDateTime.now().minusHours(1));
        log2.setPurpose("Patient copy");
        log2.setAccessGranted(true);

        when(accessLogRepository.findByPatientIdOrderByAccessTimestampDesc(1L))
                .thenReturn(List.of(log2, log1));

        // Act
        var logs = medicalRecordService.getAccessLogs(1L);

        // Assert
        assertNotNull(logs);
        assertEquals(2, logs.size());
        assertEquals("STAFF-002", logs.get(0).getStaffId());
        assertEquals("DOWNLOAD", logs.get(0).getAccessType());
        assertEquals("STAFF-001", logs.get(1).getStaffId());
        assertEquals("VIEW", logs.get(1).getAccessType());
    }

    @Test
    void testAccessMedicalRecords_WithEmptyMedicalData() {
        // Arrange
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        doNothing().when(privacyService).validateAccess(any(Patient.class), anyString());
        when(medicationRepository.findByPatientIdAndActiveTrue(1L))
                .thenReturn(new ArrayList<>());
        when(appointmentRepository.findByPatientId(1L))
                .thenReturn(new ArrayList<>());
        when(prescriptionRepository.findByPatientIdOrderByPrescriptionDateDesc(1L))
                .thenReturn(new ArrayList<>());
        when(testResultRepository.findByPatientIdOrderByTestDateDesc(1L))
                .thenReturn(new ArrayList<>());
        when(vaccinationRepository.findByPatientIdOrderByVaccinationDateDesc(1L))
                .thenReturn(new ArrayList<>());
        when(accessLogRepository.save(any(MedicalRecordAccessLog.class)))
                .thenReturn(new MedicalRecordAccessLog());

        // Act
        MedicalRecordResponse response = medicalRecordService.accessMedicalRecordsByPatientId(
                1L, "STAFF-001", "Initial consultation");

        // Assert
        assertNotNull(response);
        assertEquals(0, response.getCurrentMedications().size());
        assertEquals(0, response.getPreviousVisits().size());
        assertEquals(0, response.getPrescriptions().size());
        assertEquals(0, response.getTestResults().size());
        assertEquals(0, response.getVaccinations().size());
    }
}
