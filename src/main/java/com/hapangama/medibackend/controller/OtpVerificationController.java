package com.hapangama.medibackend.controller;

import com.hapangama.medibackend.dto.*;
import com.hapangama.medibackend.model.OtpVerification;
import com.hapangama.medibackend.service.OtpVerificationService;
import com.hapangama.medibackend.util.PhoneNumberMasker;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for OTP verification endpoints
 * Follows Single Responsibility Principle - only handles HTTP requests/responses
 * Follows Dependency Inversion Principle - depends on service interfaces
 * Follows Open/Closed Principle - uses DTOs instead of raw Maps
 */
@RestController
@RequestMapping("/api/otp")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OtpVerificationController {

    private final OtpVerificationService otpService;
    private final PhoneNumberMasker phoneNumberMasker;

    /**
     * Send OTP to patient's phone for identity verification
     */
    @PostMapping("/send")
    public ResponseEntity<OtpSendResponse> sendOtp(@RequestBody OtpSendRequest request) {
        OtpVerification otpVerification = otpService.sendOtpForPatientVerification(
                request.getPatientId(), 
                request.getStaffUsername()
        );

        OtpSendResponse response = OtpSendResponse.builder()
                .success(true)
                .message("OTP sent successfully to patient's phone")
                .phoneNumber(phoneNumberMasker.maskPhoneNumber(otpVerification.getPhoneNumber()))
                .expiresAt(otpVerification.getExpiresAt())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Verify OTP code entered by patient
     */
    @PostMapping("/verify")
    public ResponseEntity<OtpVerifyResponse> verifyOtp(@RequestBody OtpVerifyRequest request) {
        boolean verified = otpService.verifyOtp(
                request.getPatientId(), 
                request.getOtpCode(), 
                request.getStaffUsername()
        );

        OtpVerifyResponse response = OtpVerifyResponse.builder()
                .success(verified)
                .message(verified ? "Identity verified successfully" : "Verification failed")
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Check if patient has recent verified OTP
     */
    @GetMapping("/check-verification")
    public ResponseEntity<OtpCheckResponse> checkVerification(@RequestParam Long patientId) {
        boolean hasVerification = otpService.hasRecentVerification(patientId);

        OtpCheckResponse response = OtpCheckResponse.builder()
                .verified(hasVerification)
                .message(hasVerification
                        ? "Patient has recent verification"
                        : "No recent verification found")
                .build();

        return ResponseEntity.ok(response);
    }
}
