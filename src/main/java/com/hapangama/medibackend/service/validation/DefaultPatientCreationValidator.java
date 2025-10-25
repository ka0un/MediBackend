package com.hapangama.medibackend.service.validation;

import com.hapangama.medibackend.dto.CreatePatientRequest;
import com.hapangama.medibackend.exception.BadRequestException;
import com.hapangama.medibackend.repository.PatientRepository;
import org.springframework.stereotype.Component;

@Component
public class DefaultPatientCreationValidator implements PatientCreationValidator {

    private final PatientRepository patientRepository;

    public DefaultPatientCreationValidator(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Override
    public void validate(CreatePatientRequest request) {
        // Validate required fields
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BadRequestException("Missing Required Fields: Name is required");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new BadRequestException("Missing Required Fields: Email is required");
        }
        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            throw new BadRequestException("Missing Required Fields: Phone is required");
        }
        if (request.getDigitalHealthCardNumber() == null || request.getDigitalHealthCardNumber().trim().isEmpty()) {
            throw new BadRequestException("Missing Required Fields: Digital Health Card Number is required");
        }

        // Validate uniqueness
        patientRepository.findByEmail(request.getEmail()).ifPresent(p -> {
            throw new BadRequestException("Email already exists");
        });
        patientRepository.findByDigitalHealthCardNumber(request.getDigitalHealthCardNumber()).ifPresent(p -> {
            throw new BadRequestException("Digital Health Card Number already exists");
        });
    }
}

