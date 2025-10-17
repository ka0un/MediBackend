package com.hapangama.medibackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecordResponse {
    // Patient Demographics
    private Long patientId;
    private Long version; // For optimistic locking (UC-04 E5)
    private String name;
    private String email;
    private String phone;
    private String digitalHealthCardNumber;
    private String address;
    private LocalDate dateOfBirth;
    private String bloodType;
    
    // Emergency Contact
    private String emergencyContactName;
    private String emergencyContactPhone;
    
    // Medical Information
    private String medicalHistory;
    private String allergies;
    
    // Current Medications
    private List<MedicationInfo> currentMedications;
    
    // Previous Visit Records
    private List<VisitRecord> previousVisits;
    
    // Prescriptions and Treatments
    private List<PrescriptionInfo> prescriptions;
    
    // Test Results
    private List<TestResultInfo> testResults;
    
    // Vaccination Records
    private List<VaccinationInfo> vaccinations;
    
    // Access tracking
    private LocalDateTime accessedAt;
    private String accessedBy;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MedicationInfo {
        private Long id;
        private String medicationName;
        private String dosage;
        private String frequency;
        private LocalDate startDate;
        private LocalDate endDate;
        private String prescribedBy;
        private String notes;
        private Boolean active;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VisitRecord {
        private Long appointmentId;
        private LocalDateTime visitDate;
        private String providerName;
        private String specialty;
        private String hospitalName;
        private String status;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrescriptionInfo {
        private Long id;
        private String prescribedBy;
        private LocalDateTime prescriptionDate;
        private String diagnosis;
        private String treatment;
        private String notes;
        private String medications;
        private LocalDateTime followUpDate;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestResultInfo {
        private Long id;
        private String testName;
        private LocalDate testDate;
        private String result;
        private String resultUnit;
        private String referenceRange;
        private String orderedBy;
        private String performedBy;
        private String notes;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VaccinationInfo {
        private Long id;
        private String vaccineName;
        private LocalDate vaccinationDate;
        private String batchNumber;
        private String manufacturer;
        private String administeredBy;
        private LocalDate nextDoseDate;
        private String notes;
    }
}
