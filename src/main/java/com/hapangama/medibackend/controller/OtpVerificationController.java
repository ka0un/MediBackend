package com.hapangama.medibackend.controller;

import com.hapangama.medibackend.model.OtpVerification;
import com.hapangama.medibackend.service.OtpVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/otp")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OtpVerificationController {

    private final OtpVerificationService otpService;

    /**
     * Send OTP to patient's phone for identity verification
     */
    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendOtp(
            @RequestParam Long patientId,
            @RequestParam String staffUsername
    ) {
        OtpVerification otpVerification = otpService.sendOtpForPatientVerification(patientId, staffUsername);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "OTP sent successfully to patient's phone");
        response.put("phoneNumber", maskPhoneNumber(otpVerification.getPhoneNumber()));
        response.put("expiresAt", otpVerification.getExpiresAt());

        return ResponseEntity.ok(response);
    }

    /**
     * Verify OTP code entered by patient
     */
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyOtp(
            @RequestBody Map<String, Object> request
    ) {
        Long patientId = Long.valueOf(request.get("patientId").toString());
        String otpCode = request.get("otpCode").toString();
        String staffUsername = request.get("staffUsername").toString();

        boolean verified = otpService.verifyOtp(patientId, otpCode, staffUsername);

        Map<String, Object> response = new HashMap<>();
        response.put("success", verified);
        response.put("message", verified ? "Identity verified successfully" : "Verification failed");

        return ResponseEntity.ok(response);
    }

    /**
     * Check if patient has recent verified OTP
     */
    @GetMapping("/check-verification")
    public ResponseEntity<Map<String, Object>> checkVerification(
            @RequestParam Long patientId
    ) {
        boolean hasVerification = otpService.hasRecentVerification(patientId);

        Map<String, Object> response = new HashMap<>();
        response.put("verified", hasVerification);
        response.put("message", hasVerification
                ? "Patient has recent verification"
                : "No recent verification found");

        return ResponseEntity.ok(response);
    }

    /**
     * Mask phone number for security (show only last 4 digits)
     * Example: +1234567890 -> ******7890
     */
    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 4) {
            return "****";
        }
        int visibleDigits = 4;
        String lastDigits = phoneNumber.substring(phoneNumber.length() - visibleDigits);
        return "*".repeat(phoneNumber.length() - visibleDigits) + lastDigits;
    }
}
