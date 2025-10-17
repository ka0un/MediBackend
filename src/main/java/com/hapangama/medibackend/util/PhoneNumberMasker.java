package com.hapangama.medibackend.util;

import org.springframework.stereotype.Component;

/**
 * Utility class for phone number operations
 * Follows Single Responsibility Principle - only handles phone number masking
 */
@Component
public class PhoneNumberMasker {

    private static final int VISIBLE_DIGITS = 4;
    private static final String MASK_CHAR = "*";
    private static final String DEFAULT_MASK = "****";

    /**
     * Mask phone number for security (show only last 4 digits)
     * Example: +1234567890 -> ******7890
     *
     * @param phoneNumber Phone number to mask
     * @return Masked phone number
     */
    public String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < VISIBLE_DIGITS) {
            return DEFAULT_MASK;
        }
        
        String lastDigits = phoneNumber.substring(phoneNumber.length() - VISIBLE_DIGITS);
        String maskedPart = MASK_CHAR.repeat(phoneNumber.length() - VISIBLE_DIGITS);
        
        return maskedPart + lastDigits;
    }

    /**
     * Mask phone number with custom visible digits
     *
     * @param phoneNumber Phone number to mask
     * @param visibleDigits Number of digits to show at the end
     * @return Masked phone number
     */
    public String maskPhoneNumber(String phoneNumber, int visibleDigits) {
        if (phoneNumber == null || phoneNumber.length() < visibleDigits) {
            return MASK_CHAR.repeat(visibleDigits);
        }
        
        String lastDigits = phoneNumber.substring(phoneNumber.length() - visibleDigits);
        String maskedPart = MASK_CHAR.repeat(phoneNumber.length() - visibleDigits);
        
        return maskedPart + lastDigits;
    }
}
