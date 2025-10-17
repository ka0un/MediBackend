package com.hapangama.medibackend.service;

import com.hapangama.medibackend.model.Patient;

public interface PrivacyService {
    void validateAccess(Patient patient, String staffUsername);

    boolean requiresExplicitConsent(Patient patient);

    String maskSensitiveData(String data);

    void addRestrictedStaff(Patient patient, String staffUsername);

    void addAuthorizedStaff(Patient patient, String staffUsername);

    void removeRestrictedStaff(Patient patient, String staffUsername);

    void removeAuthorizedStaff(Patient patient, String staffUsername);
}
