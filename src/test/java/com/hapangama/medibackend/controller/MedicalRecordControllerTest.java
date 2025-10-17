package com.hapangama.medibackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hapangama.medibackend.dto.AddPrescriptionRequest;
import com.hapangama.medibackend.dto.ScanCardRequest;
import com.hapangama.medibackend.model.*;
import com.hapangama.medibackend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MedicalRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private MedicationRepository medicationRepository;

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private TestResultRepository testResultRepository;

    @Autowired
    private VaccinationRepository vaccinationRepository;

    @Autowired
    private MedicalRecordAccessLogRepository accessLogRepository;

    @Autowired
    private HealthcareProviderRepository providerRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    private Patient testPatient;
    private HealthcareProvider testProvider;

    @BeforeEach
    void setUp() {
        // Clean up databases
        accessLogRepository.deleteAll();
        appointmentRepository.deleteAll();
        timeSlotRepository.deleteAll();
        medicationRepository.deleteAll();
        prescriptionRepository.deleteAll();
        testResultRepository.deleteAll();
        vaccinationRepository.deleteAll();
        providerRepository.deleteAll();
        patientRepository.deleteAll();

        // Create test patient
        testPatient = new Patient();
        testPatient.setName("Test Patient");
        testPatient.setEmail("test@example.com");
        testPatient.setPhone("1234567890");
        testPatient.setDigitalHealthCardNumber("DHC-TEST-001");
        testPatient.setAddress("123 Test St");
        testPatient.setDateOfBirth(LocalDate.of(1985, 6, 15));
        testPatient.setBloodType("A+");
        testPatient.setAllergies("Pollen");
        testPatient.setMedicalHistory("Seasonal allergies");
        testPatient.setEmergencyContactName("Emergency Contact");
        testPatient.setEmergencyContactPhone("9876543210");
        testPatient = patientRepository.save(testPatient);

        // Create test provider
        testProvider = new HealthcareProvider();
        testProvider.setName("Dr. Test");
        testProvider.setSpecialty("General Medicine");
        testProvider.setHospitalName("Test Hospital");
        testProvider.setHospitalType(HealthcareProvider.HospitalType.GOVERNMENT);
        testProvider = providerRepository.save(testProvider);

        // Create sample medical data
        createSampleMedicalData();
    }

    private void createSampleMedicalData() {
        // Create medication
        Medication medication = new Medication();
        medication.setPatient(testPatient);
        medication.setMedicationName("Aspirin");
        medication.setDosage("100mg");
        medication.setFrequency("Once daily");
        medication.setStartDate(LocalDate.now().minusMonths(1));
        medication.setPrescribedBy("Dr. Test");
        medication.setActive(true);
        medicationRepository.save(medication);

        // Create prescription
        Prescription prescription = new Prescription();
        prescription.setPatient(testPatient);
        prescription.setPrescribedBy("Dr. Test");
        prescription.setPrescriptionDate(LocalDateTime.now().minusDays(10));
        prescription.setDiagnosis("Common Cold");
        prescription.setTreatment("Rest and hydration");
        prescription.setMedications("Paracetamol 500mg");
        prescription.setNotes("Review in 7 days");
        prescriptionRepository.save(prescription);

        // Create test result
        TestResult testResult = new TestResult();
        testResult.setPatient(testPatient);
        testResult.setTestName("Blood Test");
        testResult.setTestDate(LocalDate.now().minusDays(5));
        testResult.setResult("Normal");
        testResult.setResultUnit("N/A");
        testResult.setReferenceRange("Normal ranges");
        testResult.setOrderedBy("Dr. Test");
        testResultRepository.save(testResult);

        // Create vaccination
        Vaccination vaccination = new Vaccination();
        vaccination.setPatient(testPatient);
        vaccination.setVaccineName("COVID-19");
        vaccination.setVaccinationDate(LocalDate.now().minusMonths(2));
        vaccination.setBatchNumber("COVID-BATCH-001");
        vaccination.setManufacturer("Test Pharma");
        vaccination.setAdministeredBy("Nurse Test");
        vaccinationRepository.save(vaccination);
    }

    @Test
    void testScanCard_Success() throws Exception {
        ScanCardRequest request = new ScanCardRequest();
        request.setCardNumber("DHC-TEST-001");
        request.setStaffId("STAFF-001");
        request.setPurpose("General consultation");

        mockMvc.perform(post("/api/medical-records/scan-card")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId", is(testPatient.getId().intValue())))
                .andExpect(jsonPath("$.name", is("Test Patient")))
                .andExpect(jsonPath("$.digitalHealthCardNumber", is("DHC-TEST-001")))
                .andExpect(jsonPath("$.bloodType", is("A+")))
                .andExpect(jsonPath("$.allergies", is("Pollen")))
                .andExpect(jsonPath("$.currentMedications", hasSize(1)))
                .andExpect(jsonPath("$.currentMedications[0].medicationName", is("Aspirin")))
                .andExpect(jsonPath("$.prescriptions", hasSize(1)))
                .andExpect(jsonPath("$.testResults", hasSize(1)))
                .andExpect(jsonPath("$.vaccinations", hasSize(1)))
                .andExpect(jsonPath("$.accessedBy", is("STAFF-001")));
    }

    @Test
    void testScanCard_CardNotFound() throws Exception {
        ScanCardRequest request = new ScanCardRequest();
        request.setCardNumber("INVALID-CARD");
        request.setStaffId("STAFF-001");
        request.setPurpose("General consultation");

        mockMvc.perform(post("/api/medical-records/scan-card")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Patient not found with card number: INVALID-CARD")));
    }

    @Test
    void testGetMedicalRecords_Success() throws Exception {
        mockMvc.perform(get("/api/medical-records/" + testPatient.getId())
                        .param("staffId", "STAFF-002")
                        .param("purpose", "Follow-up visit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId", is(testPatient.getId().intValue())))
                .andExpect(jsonPath("$.name", is("Test Patient")))
                .andExpect(jsonPath("$.currentMedications", hasSize(1)))
                .andExpect(jsonPath("$.prescriptions", hasSize(1)))
                .andExpect(jsonPath("$.testResults", hasSize(1)))
                .andExpect(jsonPath("$.vaccinations", hasSize(1)))
                .andExpect(jsonPath("$.accessedBy", is("STAFF-002")));
    }

    @Test
    void testGetMedicalRecords_WithDefaultParameters() throws Exception {
        mockMvc.perform(get("/api/medical-records/" + testPatient.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId", is(testPatient.getId().intValue())))
                .andExpect(jsonPath("$.accessedBy", is("STAFF_DEFAULT")));
    }

    @Test
    void testGetMedicalRecords_PatientNotFound() throws Exception {
        mockMvc.perform(get("/api/medical-records/99999")
                        .param("staffId", "STAFF-002")
                        .param("purpose", "Consultation"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Patient not found with ID: 99999")));
    }

    @Test
    void testAddPrescription_Success() throws Exception {
        AddPrescriptionRequest request = new AddPrescriptionRequest();
        request.setPatientId(testPatient.getId());
        request.setStaffId("Dr. Johnson");
        request.setDiagnosis("Hypertension");
        request.setTreatment("Lifestyle changes and medication");
        request.setMedications("Lisinopril 10mg once daily");
        request.setNotes("Monitor blood pressure weekly");
        request.setFollowUpDate(LocalDateTime.now().plusMonths(1));

        mockMvc.perform(post("/api/medical-records/prescriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.patientId", is(testPatient.getId().intValue())))
                .andExpect(jsonPath("$.prescriptions", hasSize(greaterThan(1))))
                .andExpect(jsonPath("$.accessedBy", is("Dr. Johnson")));
    }

    @Test
    void testAddPrescription_PatientNotFound() throws Exception {
        AddPrescriptionRequest request = new AddPrescriptionRequest();
        request.setPatientId(99999L);
        request.setStaffId("Dr. Johnson");
        request.setDiagnosis("Test");

        mockMvc.perform(post("/api/medical-records/prescriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Patient not found with ID: 99999")));
    }

    @Test
    void testDownloadMedicalRecords_Success() throws Exception {
        mockMvc.perform(get("/api/medical-records/" + testPatient.getId() + "/download")
                        .param("staffId", "STAFF-003")
                        .param("purpose", "Patient copy"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(header().string("Content-Disposition", containsString("medical_record_")));
    }

    @Test
    void testDownloadMedicalRecords_WithDefaultParameters() throws Exception {
        mockMvc.perform(get("/api/medical-records/" + testPatient.getId() + "/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"));
    }

    @Test
    void testDownloadMedicalRecords_PatientNotFound() throws Exception {
        mockMvc.perform(get("/api/medical-records/99999/download")
                        .param("staffId", "STAFF-003")
                        .param("purpose", "Patient copy"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Patient not found with ID: 99999")));
    }

    @Test
    void testGetAccessLogs_Success() throws Exception {
        // First, create some access logs by accessing the medical record
        mockMvc.perform(get("/api/medical-records/" + testPatient.getId())
                        .param("staffId", "STAFF-LOG-1")
                        .param("purpose", "Initial consultation"));

        mockMvc.perform(get("/api/medical-records/" + testPatient.getId())
                        .param("staffId", "STAFF-LOG-2")
                        .param("purpose", "Follow-up"));

        // Now get the access logs
        mockMvc.perform(get("/api/medical-records/" + testPatient.getId() + "/access-logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].patientId", is(testPatient.getId().intValue())))
                .andExpect(jsonPath("$[0].accessType", is("VIEW")))
                .andExpect(jsonPath("$[0].accessGranted", is(true)))
                .andExpect(jsonPath("$[1].accessType", is("VIEW")));
    }

    @Test
    void testGetAccessLogs_EmptyList() throws Exception {
        // Create a new patient without any access logs
        Patient newPatient = new Patient();
        newPatient.setName("New Patient");
        newPatient.setEmail("new@example.com");
        newPatient.setPhone("1111111111");
        newPatient.setDigitalHealthCardNumber("DHC-NEW-001");
        newPatient = patientRepository.save(newPatient);

        mockMvc.perform(get("/api/medical-records/" + newPatient.getId() + "/access-logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testCompleteMedicalRecordFlow() throws Exception {
        // 1. Scan card to access medical records
        ScanCardRequest scanRequest = new ScanCardRequest();
        scanRequest.setCardNumber("DHC-TEST-001");
        scanRequest.setStaffId("DR-FLOW-TEST");
        scanRequest.setPurpose("Complete consultation");

        mockMvc.perform(post("/api/medical-records/scan-card")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scanRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Test Patient")));

        // 2. Add a prescription
        AddPrescriptionRequest prescRequest = new AddPrescriptionRequest();
        prescRequest.setPatientId(testPatient.getId());
        prescRequest.setStaffId("DR-FLOW-TEST");
        prescRequest.setDiagnosis("Seasonal Allergies");
        prescRequest.setTreatment("Antihistamines");
        prescRequest.setMedications("Cetirizine 10mg");

        mockMvc.perform(post("/api/medical-records/prescriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(prescRequest)))
                .andExpect(status().isCreated());

        // 3. Download medical records
        mockMvc.perform(get("/api/medical-records/" + testPatient.getId() + "/download")
                        .param("staffId", "DR-FLOW-TEST")
                        .param("purpose", "Referral"))
                .andExpect(status().isOk());

        // 4. Verify access logs
        mockMvc.perform(get("/api/medical-records/" + testPatient.getId() + "/access-logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3))) // VIEW, UPDATE, DOWNLOAD
                .andExpect(jsonPath("$[0].accessType", anyOf(is("VIEW"), is("UPDATE"), is("DOWNLOAD"))))
                .andExpect(jsonPath("$[1].accessType", anyOf(is("VIEW"), is("UPDATE"), is("DOWNLOAD"))))
                .andExpect(jsonPath("$[2].accessType", anyOf(is("VIEW"), is("UPDATE"), is("DOWNLOAD"))));
    }

    @Test
    void testCorsConfiguration() throws Exception {
        mockMvc.perform(options("/api/medical-records/scan-card")
                        .header("Origin", "http://example.com")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk());
    }
}
