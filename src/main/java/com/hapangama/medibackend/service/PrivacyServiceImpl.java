package com.hapangama.medibackend.service;

import com.hapangama.medibackend.exception.AccessRestrictedException;
import com.hapangama.medibackend.model.Patient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for managing patient privacy and access restrictions (UC-04 E3)
 */
@Service
@Slf4j
public class PrivacyServiceImpl implements PrivacyService {

    /**
     * Validate if staff member has access to patient records
     * @param patient Patient whose records are being accessed
     * @param staffUsername Staff member attempting access
     * @throws AccessRestrictedException if access is denied
     */
    @Override
    public void validateAccess(Patient patient, String staffUsername) {
        if (patient == null || staffUsername == null || staffUsername.isEmpty()) {
            return;
        }

        // Check if staff is in the restricted list (blacklist)
        if (patient.getRestrictedStaffList() != null && 
            patient.getRestrictedStaffList().contains(staffUsername)) {
            log.warn("Access denied: Staff {} is restricted from accessing patient {}", 
                    staffUsername, patient.getId());
            throw new AccessRestrictedException(
                    "Access denied: You are restricted from accessing this patient's records. " +
                    "Please contact the privacy officer if you believe this is an error."
            );
        }

        // Check if patient has restricted access enabled
        if (patient.isRestrictedAccess()) {
            // For restricted patients, staff must be on the authorized list (whitelist)
            if (patient.getAuthorizedStaffList() == null || 
                !patient.getAuthorizedStaffList().contains(staffUsername)) {
                log.warn("Access denied: Staff {} not authorized for restricted patient {}", 
                        staffUsername, patient.getId());
                throw new AccessRestrictedException(
                        "Access denied: This patient has restricted access. " +
                        "You are not on the authorized staff list. Reason: " + 
                        (patient.getAccessRestrictionReason() != null ? 
                            patient.getAccessRestrictionReason() : "Privacy restriction")
                );
            }
        }

        log.debug("Access granted: Staff {} can access patient {}", staffUsername, patient.getId());
    }

    /**
     * Check if explicit consent is required before displaying sensitive data
     * @param patient Patient whose records are being accessed
     * @return true if explicit consent is required
     */
    @Override
    public boolean requiresExplicitConsent(Patient patient) {
        return patient != null && patient.isRequiresExplicitConsent();
    }

    /**
     * Mask sensitive data for display (for consent warnings)
     * @param data Sensitive data to mask
     * @return Masked data
     */
    @Override
    public String maskSensitiveData(String data) {
        if (data == null || data.length() <= 4) {
            return "****";
        }
        return data.substring(0, 2) + "****" + data.substring(data.length() - 2);
    }

    /**
     * Add staff to patient's restricted list
     * @param patient Patient to update
     * @param staffUsername Staff to restrict
     */
    @Override
    public void addRestrictedStaff(Patient patient, String staffUsername) {
        if (patient.getRestrictedStaffList() == null) {
            patient.setRestrictedStaffList(new java.util.HashSet<>());
        }
        patient.getRestrictedStaffList().add(staffUsername);
        log.info("Added staff {} to restricted list for patient {}", staffUsername, patient.getId());
    }

    /**
     * Add staff to patient's authorized list
     * @param patient Patient to update
     * @param staffUsername Staff to authorize
     */
    @Override
    public void addAuthorizedStaff(Patient patient, String staffUsername) {
        if (patient.getAuthorizedStaffList() == null) {
            patient.setAuthorizedStaffList(new java.util.HashSet<>());
        }
        patient.getAuthorizedStaffList().add(staffUsername);
        log.info("Added staff {} to authorized list for patient {}", staffUsername, patient.getId());
    }

    /**
     * Remove staff from patient's restricted list
     * @param patient Patient to update
     * @param staffUsername Staff to remove from restriction
     */
    @Override
    public void removeRestrictedStaff(Patient patient, String staffUsername) {
        if (patient.getRestrictedStaffList() != null) {
            patient.getRestrictedStaffList().remove(staffUsername);
            log.info("Removed staff {} from restricted list for patient {}", staffUsername, patient.getId());
        }
    }

    /**
     * Remove staff from patient's authorized list
     * @param patient Patient to update
     * @param staffUsername Staff to remove from authorization
     */
    @Override
    public void removeAuthorizedStaff(Patient patient, String staffUsername) {
        if (patient.getAuthorizedStaffList() != null) {
            patient.getAuthorizedStaffList().remove(staffUsername);
            log.info("Removed staff {} from authorized list for patient {}", staffUsername, patient.getId());
        }
    }
}
