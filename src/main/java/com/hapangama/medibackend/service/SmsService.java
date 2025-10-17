package com.hapangama.medibackend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class SmsService {

    @Value("${sms.api.url:#{null}}")
    private String smsApiUrl;

    @Value("${sms.api.token:#{null}}")
    private String smsApiToken;

    @Value("${sms.sender.id:TextLKDemo}")
    private String smsSenderId;

    private final RestTemplate restTemplate;

    public SmsService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Send SMS message using Text.lk SMS gateway
     * @param phoneNumber Recipient phone number (can be 0xxxxxxxxx or 94xxxxxxxxx)
     * @param message SMS message content
     * @return Success status
     */
    public boolean sendSMS(String phoneNumber, String message) {
        try {
            // Check if SMS service is configured
            if (smsApiUrl == null || smsApiUrl.isEmpty() || smsApiToken == null || smsApiToken.isEmpty()) {
                log.warn("SMS service is not configured. Skipping SMS to: {}", phoneNumber);
                log.info("OTP Message would be: {}", message);
                return true; // Return true in development mode
            }

            // Validate phone number
            if (phoneNumber == null || phoneNumber.isEmpty()) {
                throw new IllegalArgumentException("Phone number is required");
            }

            // Format phone number: convert 0xxxxxxxxx to 94xxxxxxxxx
            String formattedPhone = formatPhoneNumber(phoneNumber);

            // Prepare request body
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("recipient", formattedPhone);
            requestBody.put("sender_id", smsSenderId);
            requestBody.put("type", "plain");
            requestBody.put("message", message);

            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + smsApiToken);
            headers.set("Content-Type", "application/json");
            headers.set("Accept", "application/json");

            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

            // Send SMS
            ResponseEntity<String> response = restTemplate.exchange(
                    smsApiUrl,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            log.info("SMS sent successfully to: {}", formattedPhone);
            return response.getStatusCode().is2xxSuccessful();

        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", phoneNumber, e.getMessage());
            return false;
        }
    }

    /**
     * Format phone number to international format
     * @param phoneNumber Phone number in local or international format
     * @return Formatted phone number (94xxxxxxxxx)
     */
    private String formatPhoneNumber(String phoneNumber) {
        // Remove all non-digit characters
        String cleaned = phoneNumber.replaceAll("[^0-9]", "");

        // If starts with 0, replace with 94
        if (cleaned.startsWith("0")) {
            return "94" + cleaned.substring(1);
        }

        // If already starts with 94, return as is
        if (cleaned.startsWith("94")) {
            return cleaned;
        }

        // Otherwise, assume it's missing country code
        return "94" + cleaned;
    }

    /**
     * Send OTP SMS
     * @param phoneNumber Recipient phone number
     * @param otpCode OTP code to send
     * @param patientName Patient name (optional)
     * @return Success status
     */
    public boolean sendOtpSms(String phoneNumber, String otpCode, String patientName) {
        String message = String.format(
                "MediSystem Identity Verification\n\n" +
                "Your OTP code is: %s\n\n" +
                "This code will expire in 10 minutes.\n" +
                "Do not share this code with anyone.",
                otpCode
        );

        if (patientName != null && !patientName.isEmpty()) {
            message = String.format(
                    "Hello %s,\n\n" +
                    "MediSystem Identity Verification\n\n" +
                    "Your OTP code is: %s\n\n" +
                    "This code will expire in 10 minutes.\n" +
                    "Do not share this code with anyone.",
                    patientName,
                    otpCode
            );
        }

        return sendSMS(phoneNumber, message);
    }
}
