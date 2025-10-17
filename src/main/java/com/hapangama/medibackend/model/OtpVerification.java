package com.hapangama.medibackend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "otp_verifications")
@Data
public class OtpVerification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String phoneNumber;
    
    @Column(nullable = false)
    private String otpCode;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(nullable = false)
    private boolean verified = false;
    
    @Column(nullable = false)
    private boolean used = false;
    
    @Column
    private String purpose; // e.g., "IDENTITY_VERIFICATION"
    
    @Column
    private Long patientId;
    
    @Column
    private String verifiedBy; // Staff username who requested verification
    
    @Column
    private LocalDateTime verifiedAt;
    
    @Column
    private int attempts = 0; // Track failed verification attempts
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        // OTP expires after 10 minutes
        expiresAt = createdAt.plusMinutes(10);
    }
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    public boolean isValid() {
        return !isExpired() && !used && !verified;
    }
}
