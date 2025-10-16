package com.hapangama.medibackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    private String username;
    private String password;
    
    // Patient information
    private String name;
    private String email;
    private String phone;
    private String digitalHealthCardNumber;
    private String address;
    private String dateOfBirth; // Format: YYYY-MM-DD
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String bloodType;
    private String allergies;
    private String medicalHistory;
}
