package com.hapangama.medibackend.service;

import com.hapangama.medibackend.dto.AccessLogResponse;
import com.hapangama.medibackend.dto.AddPrescriptionRequest;
import com.hapangama.medibackend.dto.MedicalRecordResponse;
import com.hapangama.medibackend.dto.ScanCardRequest;
import com.hapangama.medibackend.exception.NotFoundException;
import com.hapangama.medibackend.model.*;
import com.hapangama.medibackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicalRecordService {

    private final PatientRepository patientRepository;
    private final MedicationRepository medicationRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final TestResultRepository testResultRepository;
    private final VaccinationRepository vaccinationRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicalRecordAccessLogRepository accessLogRepository;

    @Transactional(readOnly = true)
    public MedicalRecordResponse accessMedicalRecordsByCardNumber(ScanCardRequest request) {
        // Find patient by digital health card number
        Patient patient = patientRepository.findByDigitalHealthCardNumber(request.getCardNumber())
                .orElseThrow(() -> new NotFoundException("Patient not found with card number: " + request.getCardNumber()));

        // Log the access
        logAccess(patient.getId(), request.getStaffId(), "VIEW", request.getPurpose(), true, null);

        // Build and return comprehensive medical record
        return buildMedicalRecordResponse(patient, request.getStaffId());
    }

    @Transactional(readOnly = true)
    public MedicalRecordResponse accessMedicalRecordsByPatientId(Long patientId, String staffId, String purpose) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new NotFoundException("Patient not found with ID: " + patientId));

        // Log the access
        logAccess(patientId, staffId, "VIEW", purpose, true, null);

        return buildMedicalRecordResponse(patient, staffId);
    }

    @Transactional
    public MedicalRecordResponse addPrescription(AddPrescriptionRequest request) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new NotFoundException("Patient not found with ID: " + request.getPatientId()));

        // Create new prescription
        Prescription prescription = new Prescription();
        prescription.setPatient(patient);
        prescription.setPrescribedBy(request.getStaffId());
        prescription.setPrescriptionDate(LocalDateTime.now());
        prescription.setDiagnosis(request.getDiagnosis());
        prescription.setTreatment(request.getTreatment());
        prescription.setNotes(request.getNotes());
        prescription.setMedications(request.getMedications());
        prescription.setFollowUpDate(request.getFollowUpDate());

        prescriptionRepository.save(prescription);

        // Log the access
        logAccess(patient.getId(), request.getStaffId(), "UPDATE", 
                 "Added prescription - Diagnosis: " + request.getDiagnosis(), true, null);

        return buildMedicalRecordResponse(patient, request.getStaffId());
    }

    @Transactional(readOnly = true)
    public byte[] downloadMedicalRecordsAsPdf(Long patientId, String staffId, String purpose) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new NotFoundException("Patient not found with ID: " + patientId));

        // Log the download access
        logAccess(patientId, staffId, "DOWNLOAD", purpose, true, null);

        // Generate PDF
        return generateMedicalRecordPdf(patient);
    }

    @Transactional(readOnly = true)
    public List<AccessLogResponse> getAccessLogs(Long patientId) {
        List<MedicalRecordAccessLog> logs = accessLogRepository.findByPatientIdOrderByAccessTimestampDesc(patientId);
        return logs.stream()
                .map(this::mapToAccessLogResponse)
                .collect(Collectors.toList());
    }

    private MedicalRecordResponse buildMedicalRecordResponse(Patient patient, String staffId) {
        MedicalRecordResponse response = new MedicalRecordResponse();

        // Patient Demographics
        response.setPatientId(patient.getId());
        response.setName(patient.getName());
        response.setEmail(patient.getEmail());
        response.setPhone(patient.getPhone());
        response.setDigitalHealthCardNumber(patient.getDigitalHealthCardNumber());
        response.setAddress(patient.getAddress());
        response.setDateOfBirth(patient.getDateOfBirth());
        response.setBloodType(patient.getBloodType());

        // Emergency Contact
        response.setEmergencyContactName(patient.getEmergencyContactName());
        response.setEmergencyContactPhone(patient.getEmergencyContactPhone());

        // Medical Information
        response.setMedicalHistory(patient.getMedicalHistory());
        response.setAllergies(patient.getAllergies());

        // Current Medications
        List<Medication> medications = medicationRepository.findByPatientIdAndActiveTrue(patient.getId());
        response.setCurrentMedications(medications.stream()
                .map(this::mapToMedicationInfo)
                .collect(Collectors.toList()));

        // Previous Visit Records
        List<Appointment> appointments = appointmentRepository.findByPatientId(patient.getId());
        response.setPreviousVisits(appointments.stream()
                .map(this::mapToVisitRecord)
                .collect(Collectors.toList()));

        // Prescriptions and Treatments
        List<Prescription> prescriptions = prescriptionRepository.findByPatientIdOrderByPrescriptionDateDesc(patient.getId());
        response.setPrescriptions(prescriptions.stream()
                .map(this::mapToPrescriptionInfo)
                .collect(Collectors.toList()));

        // Test Results
        List<TestResult> testResults = testResultRepository.findByPatientIdOrderByTestDateDesc(patient.getId());
        response.setTestResults(testResults.stream()
                .map(this::mapToTestResultInfo)
                .collect(Collectors.toList()));

        // Vaccination Records
        List<Vaccination> vaccinations = vaccinationRepository.findByPatientIdOrderByVaccinationDateDesc(patient.getId());
        response.setVaccinations(vaccinations.stream()
                .map(this::mapToVaccinationInfo)
                .collect(Collectors.toList()));

        // Access tracking
        response.setAccessedAt(LocalDateTime.now());
        response.setAccessedBy(staffId);

        return response;
    }

    private void logAccess(Long patientId, String staffId, String accessType, String purpose, 
                          Boolean accessGranted, String denialReason) {
        MedicalRecordAccessLog log = new MedicalRecordAccessLog();
        log.setPatientId(patientId);
        log.setStaffId(staffId != null ? staffId : "SYSTEM");
        log.setAccessType(accessType);
        log.setAccessTimestamp(LocalDateTime.now());
        log.setPurpose(purpose);
        log.setAccessGranted(accessGranted);
        log.setDenialReason(denialReason);
        accessLogRepository.save(log);
    }

    private byte[] generateMedicalRecordPdf(Patient patient) {
        // Build comprehensive medical record for PDF
        MedicalRecordResponse record = buildMedicalRecordResponse(patient, "SYSTEM");
        
        try {
            // Use iText to generate PDF
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            com.itextpdf.kernel.pdf.PdfWriter writer = new com.itextpdf.kernel.pdf.PdfWriter(baos);
            com.itextpdf.kernel.pdf.PdfDocument pdf = new com.itextpdf.kernel.pdf.PdfDocument(writer);
            com.itextpdf.layout.Document document = new com.itextpdf.layout.Document(pdf);

            // Add title
            document.add(new com.itextpdf.layout.element.Paragraph("Medical Records")
                    .setFontSize(20)
                    .setBold());
            
            // Patient Demographics
            document.add(new com.itextpdf.layout.element.Paragraph("Patient Information")
                    .setFontSize(16)
                    .setBold());
            document.add(new com.itextpdf.layout.element.Paragraph("Name: " + patient.getName()));
            document.add(new com.itextpdf.layout.element.Paragraph("Digital Health Card: " + patient.getDigitalHealthCardNumber()));
            document.add(new com.itextpdf.layout.element.Paragraph("Date of Birth: " + (patient.getDateOfBirth() != null ? patient.getDateOfBirth().toString() : "N/A")));
            document.add(new com.itextpdf.layout.element.Paragraph("Blood Type: " + (patient.getBloodType() != null ? patient.getBloodType() : "N/A")));
            document.add(new com.itextpdf.layout.element.Paragraph("Phone: " + patient.getPhone()));
            document.add(new com.itextpdf.layout.element.Paragraph("Email: " + patient.getEmail()));
            document.add(new com.itextpdf.layout.element.Paragraph("Address: " + (patient.getAddress() != null ? patient.getAddress() : "N/A")));
            document.add(new com.itextpdf.layout.element.Paragraph("\n"));

            // Medical History
            document.add(new com.itextpdf.layout.element.Paragraph("Medical History")
                    .setFontSize(16)
                    .setBold());
            document.add(new com.itextpdf.layout.element.Paragraph(patient.getMedicalHistory() != null ? patient.getMedicalHistory() : "No medical history recorded"));
            document.add(new com.itextpdf.layout.element.Paragraph("\n"));

            // Allergies
            document.add(new com.itextpdf.layout.element.Paragraph("Allergies")
                    .setFontSize(16)
                    .setBold());
            document.add(new com.itextpdf.layout.element.Paragraph(patient.getAllergies() != null ? patient.getAllergies() : "No known allergies"));
            document.add(new com.itextpdf.layout.element.Paragraph("\n"));

            // Current Medications
            document.add(new com.itextpdf.layout.element.Paragraph("Current Medications")
                    .setFontSize(16)
                    .setBold());
            if (record.getCurrentMedications().isEmpty()) {
                document.add(new com.itextpdf.layout.element.Paragraph("No current medications"));
            } else {
                for (MedicalRecordResponse.MedicationInfo med : record.getCurrentMedications()) {
                    document.add(new com.itextpdf.layout.element.Paragraph(
                            med.getMedicationName() + " - " + med.getDosage() + " (" + med.getFrequency() + ")"));
                }
            }
            document.add(new com.itextpdf.layout.element.Paragraph("\n"));

            // Recent Prescriptions
            document.add(new com.itextpdf.layout.element.Paragraph("Recent Prescriptions")
                    .setFontSize(16)
                    .setBold());
            if (record.getPrescriptions().isEmpty()) {
                document.add(new com.itextpdf.layout.element.Paragraph("No prescriptions recorded"));
            } else {
                for (MedicalRecordResponse.PrescriptionInfo presc : record.getPrescriptions()) {
                    document.add(new com.itextpdf.layout.element.Paragraph("Date: " + presc.getPrescriptionDate()));
                    document.add(new com.itextpdf.layout.element.Paragraph("Prescribed by: " + presc.getPrescribedBy()));
                    document.add(new com.itextpdf.layout.element.Paragraph("Diagnosis: " + presc.getDiagnosis()));
                    if (presc.getTreatment() != null) {
                        document.add(new com.itextpdf.layout.element.Paragraph("Treatment: " + presc.getTreatment()));
                    }
                    document.add(new com.itextpdf.layout.element.Paragraph(""));
                }
            }
            document.add(new com.itextpdf.layout.element.Paragraph("\n"));

            // Test Results
            document.add(new com.itextpdf.layout.element.Paragraph("Test Results")
                    .setFontSize(16)
                    .setBold());
            if (record.getTestResults().isEmpty()) {
                document.add(new com.itextpdf.layout.element.Paragraph("No test results recorded"));
            } else {
                for (MedicalRecordResponse.TestResultInfo test : record.getTestResults()) {
                    document.add(new com.itextpdf.layout.element.Paragraph(
                            test.getTestName() + " (" + test.getTestDate() + "): " + test.getResult()));
                }
            }
            document.add(new com.itextpdf.layout.element.Paragraph("\n"));

            // Vaccination Records
            document.add(new com.itextpdf.layout.element.Paragraph("Vaccination Records")
                    .setFontSize(16)
                    .setBold());
            if (record.getVaccinations().isEmpty()) {
                document.add(new com.itextpdf.layout.element.Paragraph("No vaccination records"));
            } else {
                for (MedicalRecordResponse.VaccinationInfo vac : record.getVaccinations()) {
                    document.add(new com.itextpdf.layout.element.Paragraph(
                            vac.getVaccineName() + " - " + vac.getVaccinationDate()));
                }
            }

            // Footer
            document.add(new com.itextpdf.layout.element.Paragraph("\n\nGenerated: " + LocalDateTime.now())
                    .setFontSize(10));

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage(), e);
        }
    }

    private MedicalRecordResponse.MedicationInfo mapToMedicationInfo(Medication medication) {
        return new MedicalRecordResponse.MedicationInfo(
                medication.getId(),
                medication.getMedicationName(),
                medication.getDosage(),
                medication.getFrequency(),
                medication.getStartDate(),
                medication.getEndDate(),
                medication.getPrescribedBy(),
                medication.getNotes(),
                medication.getActive()
        );
    }

    private MedicalRecordResponse.VisitRecord mapToVisitRecord(Appointment appointment) {
        return new MedicalRecordResponse.VisitRecord(
                appointment.getId(),
                appointment.getTimeSlot().getStartTime(),
                appointment.getProvider().getName(),
                appointment.getProvider().getSpecialty(),
                appointment.getProvider().getHospitalName(),
                appointment.getStatus().toString()
        );
    }

    private MedicalRecordResponse.PrescriptionInfo mapToPrescriptionInfo(Prescription prescription) {
        return new MedicalRecordResponse.PrescriptionInfo(
                prescription.getId(),
                prescription.getPrescribedBy(),
                prescription.getPrescriptionDate(),
                prescription.getDiagnosis(),
                prescription.getTreatment(),
                prescription.getNotes(),
                prescription.getMedications(),
                prescription.getFollowUpDate()
        );
    }

    private MedicalRecordResponse.TestResultInfo mapToTestResultInfo(TestResult testResult) {
        return new MedicalRecordResponse.TestResultInfo(
                testResult.getId(),
                testResult.getTestName(),
                testResult.getTestDate(),
                testResult.getResult(),
                testResult.getResultUnit(),
                testResult.getReferenceRange(),
                testResult.getOrderedBy(),
                testResult.getPerformedBy(),
                testResult.getNotes()
        );
    }

    private MedicalRecordResponse.VaccinationInfo mapToVaccinationInfo(Vaccination vaccination) {
        return new MedicalRecordResponse.VaccinationInfo(
                vaccination.getId(),
                vaccination.getVaccineName(),
                vaccination.getVaccinationDate(),
                vaccination.getBatchNumber(),
                vaccination.getManufacturer(),
                vaccination.getAdministeredBy(),
                vaccination.getNextDoseDate(),
                vaccination.getNotes()
        );
    }

    private AccessLogResponse mapToAccessLogResponse(MedicalRecordAccessLog log) {
        return new AccessLogResponse(
                log.getId(),
                log.getPatientId(),
                log.getStaffId(),
                log.getAccessType(),
                log.getAccessTimestamp(),
                log.getPurpose(),
                log.getAccessGranted(),
                log.getDenialReason()
        );
    }
}
