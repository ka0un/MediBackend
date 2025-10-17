package com.hapangama.medibackend.repository;

import com.hapangama.medibackend.model.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {
    
    Optional<OtpVerification> findTopByPhoneNumberAndUsedFalseAndVerifiedFalseOrderByCreatedAtDesc(String phoneNumber);
    
    List<OtpVerification> findByPhoneNumberAndCreatedAtAfter(String phoneNumber, LocalDateTime since);
    
    Optional<OtpVerification> findByPhoneNumberAndOtpCodeAndUsedFalse(String phoneNumber, String otpCode);
    
    List<OtpVerification> findByExpiresAtBeforeAndUsedFalse(LocalDateTime expiresAt);
}
