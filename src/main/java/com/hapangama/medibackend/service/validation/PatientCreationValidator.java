package com.hapangama.medibackend.service.validation;

import com.hapangama.medibackend.dto.CreatePatientRequest;

/**
 * Strategy interface for validating patient creation requests.
 * Enables adding new validations without modifying PatientService (Open/Closed Principle).
 */
public interface PatientCreationValidator {
    /**
     * Validate the given request. Implementations should throw runtime exceptions used by the API on failure.
     * @param request patient creation request
     */
    void validate(CreatePatientRequest request);
}

