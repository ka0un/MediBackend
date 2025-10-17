package com.hapangama.medibackend.service;

import com.hapangama.medibackend.exception.BadRequestException;
import com.hapangama.medibackend.exception.NotFoundException;
import com.hapangama.medibackend.model.OtpVerification;
import com.hapangama.medibackend.model.Patient;
import com.hapangama.medibackend.repository.OtpVerificationRepository;
import com.hapangama.medibackend.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for OTP verification operations
 * Follows Dependency Inversion Principle - depends on SmsGateway interface, not concrete implementation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OtpVerificationService {

    private final OtpVerificationRepository otpRepository;
    private final PatientRepository patientRepository;
    private final SmsGateway smsGateway; // Changed from SmsService to SmsGateway interface
    private final SecureRandom secureRandom = new SecureRandom();

    private static final int MAX_ATTEMPTS = 3;
    private static final int MAX_REQUESTS_PER_HOUR = 3;

    /**
     * Generate a random 6-digit OTP code
     */
    private String generateOtpCode() {
        int otp = secureRandom.nextInt(900000) + 100000; // Generates 100000 to 999999
        return String.valueOf(otp);
    }

    /**
     * Send OTP for patient identity verification
     * @param patientId Patient ID
     * @param staffUsername Staff member requesting verification
     * @return OTP Verification record
     */
    @Transactional
    public OtpVerification sendOtpForPatientVerification(Long patientId, String staffUsername) {
        // Find patient
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new NotFoundException("Patient not found with ID: " + patientId));

        String phoneNumber = patient.getPhone();
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            throw new BadRequestException("Patient does not have a phone number registered");
        }

        // Check rate limiting (max 3 requests per hour)
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        List<OtpVerification> recentRequests = otpRepository.findByPhoneNumberAndCreatedAtAfter(
                phoneNumber, oneHourAgo
        );

        if (recentRequests.size() >= MAX_REQUESTS_PER_HOUR) {
            throw new BadRequestException("Too many OTP requests. Please try again later.");
        }

        // Generate OTP
        String otpCode = generateOtpCode();

        // Create OTP verification record
        OtpVerification otpVerification = new OtpVerification();
        otpVerification.setPhoneNumber(phoneNumber);
        otpVerification.setOtpCode(otpCode);
        otpVerification.setPurpose("IDENTITY_VERIFICATION");
        otpVerification.setPatientId(patientId);
        otpVerification.setVerifiedBy(staffUsername);

        otpVerification = otpRepository.save(otpVerification);

        // Send SMS using gateway interface
        boolean smsSent = smsGateway.sendOtpSms(phoneNumber, otpCode, patient.getName());

        if (!smsSent) {
            log.warn("Failed to send OTP SMS to patient: {}", patientId);
            // Don't throw exception, just log warning. In production, you might want to handle this differently
        }

        log.info("OTP sent to patient {} by staff {}", patientId, staffUsername);

        return otpVerification;
    }

    /**
     * Verify OTP code
     * @param patientId Patient ID
     * @param otpCode OTP code entered by patient
     * @param staffUsername Staff member verifying
     * @return true if OTP is valid
     */
    @Transactional
    public boolean verifyOtp(Long patientId, String otpCode, String staffUsername) {
        // Find patient
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new NotFoundException("Patient not found with ID: " + patientId));

        String phoneNumber = patient.getPhone();

        // Find latest unused OTP for this phone number
        OtpVerification otpVerification = otpRepository
                .findTopByPhoneNumberAndUsedFalseAndVerifiedFalseOrderByCreatedAtDesc(phoneNumber)
                .orElseThrow(() -> new BadRequestException("No active OTP found. Please request a new OTP."));

        // Check if expired
        if (otpVerification.isExpired()) {
            throw new BadRequestException("OTP has expired. Please request a new OTP.");
        }

        // Check max attempts
        if (otpVerification.getAttempts() >= MAX_ATTEMPTS) {
            otpVerification.setUsed(true);
            otpRepository.save(otpVerification);
            throw new BadRequestException("Maximum verification attempts exceeded. Please request a new OTP.");
        }

        // Increment attempts
        otpVerification.setAttempts(otpVerification.getAttempts() + 1);

        // Verify OTP code
        if (!otpVerification.getOtpCode().equals(otpCode)) {
            otpRepository.save(otpVerification);
            int remainingAttempts = MAX_ATTEMPTS - otpVerification.getAttempts();
            throw new BadRequestException(
                    String.format("Invalid OTP code. %d attempt(s) remaining.", remainingAttempts)
            );
        }

        // Mark as verified
        otpVerification.setVerified(true);
        otpVerification.setUsed(true);
        otpVerification.setVerifiedAt(LocalDateTime.now());
        otpRepository.save(otpVerification);

        log.info("OTP verified successfully for patient {} by staff {}", patientId, staffUsername);

        return true;
    }

    /**
     * Check if patient has a verified OTP within the last 30 minutes
     * @param patientId Patient ID
     * @return true if patient has recent verified OTP
     */
    public boolean hasRecentVerification(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new NotFoundException("Patient not found with ID: " + patientId));

        LocalDateTime thirtyMinutesAgo = LocalDateTime.now().minusMinutes(30);
        List<OtpVerification> recentVerifications = otpRepository
                .findByPhoneNumberAndCreatedAtAfter(patient.getPhone(), thirtyMinutesAgo);

        return recentVerifications.stream()
                .anyMatch(OtpVerification::isVerified);
    }

    /**
     * Cleanup expired OTP records (can be scheduled)
     */
    @Transactional
    public void cleanupExpiredOtps() {
        List<OtpVerification> expiredOtps = otpRepository
                .findByExpiresAtBeforeAndUsedFalse(LocalDateTime.now());

        expiredOtps.forEach(otp -> {
            otp.setUsed(true);
            otpRepository.save(otp);
        });

        log.info("Cleaned up {} expired OTP records", expiredOtps.size());
    }
}
