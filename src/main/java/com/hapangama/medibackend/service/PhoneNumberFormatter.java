package com.hapangama.medibackend.service;

import org.springframework.stereotype.Component;

/**
 * Utility for formatting phone numbers
 * Follows Single Responsibility Principle - only handles phone number formatting
 */
@Component
public class PhoneNumberFormatter {

    private static final String COUNTRY_CODE = "94";

    /**
     * Format phone number to international format (94xxxxxxxxx)
     *
     * @param phoneNumber Phone number in local or international format
     * @return Formatted phone number
     */
    public String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be null or empty");
        }

        // Remove all non-digit characters
        String cleaned = phoneNumber.replaceAll("[^0-9]", "");

        // If starts with 0, replace with country code
        if (cleaned.startsWith("0")) {
            return COUNTRY_CODE + cleaned.substring(1);
        }

        // If already starts with country code, return as is
        if (cleaned.startsWith(COUNTRY_CODE)) {
            return cleaned;
        }

        // Otherwise, assume it's missing country code
        return COUNTRY_CODE + cleaned;
    }

    /**
     * Format phone number with custom country code
     *
     * @param phoneNumber Phone number to format
     * @param countryCode Country code to use (e.g., "1" for US, "94" for Sri Lanka)
     * @return Formatted phone number
     */
    public String formatPhoneNumber(String phoneNumber, String countryCode) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be null or empty");
        }

        String cleaned = phoneNumber.replaceAll("[^0-9]", "");

        if (cleaned.startsWith("0")) {
            return countryCode + cleaned.substring(1);
        }

        if (cleaned.startsWith(countryCode)) {
            return cleaned;
        }

        return countryCode + cleaned;
    }
}
