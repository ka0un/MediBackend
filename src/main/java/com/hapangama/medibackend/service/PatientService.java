package com.hapangama.medibackend.service;

import com.hapangama.medibackend.dto.CreatePatientRequest;
import com.hapangama.medibackend.dto.PatientProfileResponse;
import com.hapangama.medibackend.dto.UpdatePatientRequest;
import com.hapangama.medibackend.exception.BadRequestException;
import com.hapangama.medibackend.model.Appointment;
import com.hapangama.medibackend.model.Patient;
import com.hapangama.medibackend.repository.AppointmentRepository;
import com.hapangama.medibackend.repository.PatientRepository;
import com.hapangama.medibackend.service.validation.PatientCreationValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final AuditService auditService;
    // Optional injection to keep unit tests without Spring context working
    @Nullable
    private final PatientCreationValidator patientCreationValidator;

    @Transactional
    public PatientProfileResponse createPatient(CreatePatientRequest request) {
        // Delegate validation to strategy if present; else fallback to existing checks
        if (patientCreationValidator != null) {
            patientCreationValidator.validate(request);
        } else {
            // Fallback validations for unit tests constructed with @InjectMocks
            if (patientRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new BadRequestException("Email already exists");
            }
            if (patientRepository.findByDigitalHealthCardNumber(request.getDigitalHealthCardNumber()).isPresent()) {
                throw new BadRequestException("Digital Health Card Number already exists");
            }
            validateRequiredFields(request.getName(), request.getEmail(), request.getPhone(),
                    request.getDigitalHealthCardNumber());
        }

        Patient patient = new Patient();
        patient.setName(request.getName());
        patient.setEmail(request.getEmail());
        patient.setPhone(request.getPhone());
        patient.setDigitalHealthCardNumber(request.getDigitalHealthCardNumber());
        patient.setAddress(request.getAddress());
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setEmergencyContactName(request.getEmergencyContactName());
        patient.setEmergencyContactPhone(request.getEmergencyContactPhone());
        patient.setMedicalHistory(request.getMedicalHistory());
        patient.setBloodType(request.getBloodType());
        patient.setAllergies(request.getAllergies());

        patient = patientRepository.save(patient);

        // Create audit log using centralized service
        auditService.logAsync(AuditService.builder()
            .action("PATIENT_CREATED")
            .entityType("Patient")
            .entityId(String.valueOf(patient.getId()))
            .patientId(patient.getId())
            .details(String.format("Patient account created: %s (Email: %s, Card: %s)", 
                patient.getName(), patient.getEmail(), patient.getDigitalHealthCardNumber()))
            .metadata(String.format("{\"email\":\"%s\",\"phone\":\"%s\"}", 
                patient.getEmail(), patient.getPhone())));

        return mapToPatientProfileResponse(patient);
    }

    public PatientProfileResponse getPatientProfile(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new com.hapangama.medibackend.exception.NotFoundException("Patient not found"));
        
        // Audit profile access
        auditService.logAsync(AuditService.builder()
            .action("PATIENT_PROFILE_VIEWED")
            .entityType("Patient")
            .entityId(String.valueOf(patientId))
            .patientId(patientId)
            .details("Patient profile viewed"));
        
        return mapToPatientProfileResponse(patient);
    }

    public List<PatientProfileResponse> getAllPatients() {
        // Audit bulk access
        auditService.logAsync(AuditService.builder()
            .action("PATIENTS_LIST_VIEWED")
            .entityType("Patient")
            .details("All patients list accessed"));
        
        return patientRepository.findAll().stream()
                .map(this::mapToPatientProfileResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PatientProfileResponse updatePatientProfile(Long patientId, UpdatePatientRequest request) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new com.hapangama.medibackend.exception.NotFoundException("Patient not found"));

        // Track changes for audit log
        StringBuilder changes = new StringBuilder();
        StringBuilder metadata = new StringBuilder("{");

        // Validate and update fields
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            if (!request.getName().equals(patient.getName())) {
                changes.append("Name changed from '").append(patient.getName())
                       .append("' to '").append(request.getName()).append("'; ");
                metadata.append("\"name\":{\"old\":\"").append(patient.getName())
                        .append("\",\"new\":\"").append(request.getName()).append("\"},");
            }
            patient.setName(request.getName());
        }

        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            // Check if email is being changed and if new email is unique
            if (!request.getEmail().equals(patient.getEmail())) {
                patientRepository.findByEmail(request.getEmail()).ifPresent(p -> {
                    if (!p.getId().equals(patientId)) {
                        throw new BadRequestException("Email already exists");
                    }
                });
                changes.append("Email changed from '").append(patient.getEmail())
                       .append("' to '").append(request.getEmail()).append("'; ");
                metadata.append("\"email\":{\"old\":\"").append(patient.getEmail())
                        .append("\",\"new\":\"").append(request.getEmail()).append("\"},");
                patient.setEmail(request.getEmail());
            }
        }

        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            if (!request.getPhone().equals(patient.getPhone())) {
                changes.append("Phone changed; ");
                metadata.append("\"phone\":{\"old\":\"").append(patient.getPhone())
                        .append("\",\"new\":\"").append(request.getPhone()).append("\"},");
            }
            patient.setPhone(request.getPhone());
        }

        if (request.getAddress() != null) {
            if (!request.getAddress().equals(patient.getAddress())) {
                changes.append("Address updated; ");
            }
            patient.setAddress(request.getAddress());
        }

        if (request.getDateOfBirth() != null) {
            if (!request.getDateOfBirth().equals(patient.getDateOfBirth())) {
                changes.append("Date of Birth updated; ");
            }
            patient.setDateOfBirth(request.getDateOfBirth());
        }

        if (request.getEmergencyContactName() != null) {
            if (!request.getEmergencyContactName().equals(patient.getEmergencyContactName())) {
                changes.append("Emergency Contact Name updated; ");
            }
            patient.setEmergencyContactName(request.getEmergencyContactName());
        }

        if (request.getEmergencyContactPhone() != null) {
            if (!request.getEmergencyContactPhone().equals(patient.getEmergencyContactPhone())) {
                changes.append("Emergency Contact Phone updated; ");
            }
            patient.setEmergencyContactPhone(request.getEmergencyContactPhone());
        }

        if (request.getMedicalHistory() != null) {
            if (!request.getMedicalHistory().equals(patient.getMedicalHistory())) {
                changes.append("Medical History updated; ");
            }
            patient.setMedicalHistory(request.getMedicalHistory());
        }

        if (request.getBloodType() != null) {
            if (!request.getBloodType().equals(patient.getBloodType())) {
                changes.append("Blood Type updated; ");
            }
            patient.setBloodType(request.getBloodType());
        }

        if (request.getAllergies() != null) {
            if (!request.getAllergies().equals(patient.getAllergies())) {
                changes.append("Allergies updated; ");
            }
            patient.setAllergies(request.getAllergies());
        }

        patient = patientRepository.save(patient);

        // Create audit log if there were changes
        if (changes.length() > 0) {
            metadata.append("\"changedFields\":").append(changes.length()).append("}");
            
            auditService.logAsync(AuditService.builder()
                .action("PATIENT_PROFILE_UPDATED")
                .entityType("Patient")
                .entityId(String.valueOf(patientId))
                .patientId(patientId)
                .details(changes.toString())
                .metadata(metadata.toString()));
        }

        return mapToPatientProfileResponse(patient);
    }

    @Transactional
    public void deletePatient(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new com.hapangama.medibackend.exception.NotFoundException("Patient not found"));
        
        String patientDetails = String.format("Patient deleted: %s (Email: %s, Card: %s)", 
            patient.getName(), patient.getEmail(), patient.getDigitalHealthCardNumber());
        
        auditService.logAsync(AuditService.builder()
            .action("PATIENT_DELETED")
            .entityType("Patient")
            .entityId(String.valueOf(patientId))
            .patientId(patientId)
            .details(patientDetails)
            .metadata(String.format("{\"name\":\"%s\",\"email\":\"%s\",\"card\":\"%s\"}", 
                patient.getName(), patient.getEmail(), patient.getDigitalHealthCardNumber())));

        List<Appointment> appointmentList = appointmentRepository.findByPatientId(patientId);
        if(!appointmentList.isEmpty()){
                appointmentRepository.deleteAll(appointmentList);
        }
        patientRepository.delete(patient);
    }

    private void validateRequiredFields(String name, String email, String phone, String digitalHealthCardNumber) {
        if (name == null || name.trim().isEmpty()) {
            throw new BadRequestException("Missing Required Fields: Name is required");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new BadRequestException("Missing Required Fields: Email is required");
        }
        if (phone == null || phone.trim().isEmpty()) {
            throw new BadRequestException("Missing Required Fields: Phone is required");
        }
        if (digitalHealthCardNumber == null || digitalHealthCardNumber.trim().isEmpty()) {
            throw new BadRequestException("Missing Required Fields: Digital Health Card Number is required");
        }
    }

    private PatientProfileResponse mapToPatientProfileResponse(Patient patient) {
        PatientProfileResponse response = new PatientProfileResponse();
        response.setId(patient.getId());
        response.setName(patient.getName());
        response.setEmail(patient.getEmail());
        response.setPhone(patient.getPhone());
        response.setDigitalHealthCardNumber(patient.getDigitalHealthCardNumber());
        response.setAddress(patient.getAddress());
        response.setDateOfBirth(patient.getDateOfBirth());
        response.setEmergencyContactName(patient.getEmergencyContactName());
        response.setEmergencyContactPhone(patient.getEmergencyContactPhone());
        response.setMedicalHistory(patient.getMedicalHistory());
        response.setBloodType(patient.getBloodType());
        response.setAllergies(patient.getAllergies());
        return response;
    }
}
