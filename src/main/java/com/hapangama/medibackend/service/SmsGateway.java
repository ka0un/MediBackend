package com.hapangama.medibackend.service;

/**
 * Interface for SMS sending operations
 * Follows Dependency Inversion Principle - depends on abstraction, not concrete implementation
 * Follows Interface Segregation Principle - focused interface for SMS operations
 */
public interface SmsGateway {
    
    /**
     * Send SMS message to a phone number
     *
     * @param phoneNumber Recipient phone number
     * @param message SMS message content
     * @return true if SMS sent successfully, false otherwise
     */
    boolean sendSms(String phoneNumber, String message);
    
    /**
     * Send OTP SMS with standard formatting
     *
     * @param phoneNumber Recipient phone number
     * @param otpCode OTP code to send
     * @param recipientName Optional recipient name for personalization
     * @return true if SMS sent successfully, false otherwise
     */
    boolean sendOtpSms(String phoneNumber, String otpCode, String recipientName);
}
