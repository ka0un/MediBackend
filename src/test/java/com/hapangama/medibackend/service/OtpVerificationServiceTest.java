package com.hapangama.medibackend.service;

import com.hapangama.medibackend.exception.BadRequestException;
import com.hapangama.medibackend.exception.NotFoundException;
import com.hapangama.medibackend.model.OtpVerification;
import com.hapangama.medibackend.model.Patient;
import com.hapangama.medibackend.repository.OtpVerificationRepository;
import com.hapangama.medibackend.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpVerificationServiceTest {

    @Mock
    private OtpVerificationRepository otpRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private SmsGateway smsGateway; // Changed from SmsService to SmsGateway

    @Mock
    private AuditService auditService;

    @InjectMocks
    private OtpVerificationService otpService;

    private Patient testPatient;
    private OtpVerification testOtp;

    @BeforeEach
    void setUp() {
        // Setup test patient
        testPatient = new Patient();
        testPatient.setId(1L);
        testPatient.setName("John Doe");
        testPatient.setPhone("+94771234567");

        // Setup test OTP
        testOtp = new OtpVerification();
        testOtp.setId(1L);
        testOtp.setPhoneNumber("+94771234567");
        testOtp.setOtpCode("123456");
        testOtp.setPurpose("IDENTITY_VERIFICATION");
        testOtp.setPatientId(1L);
        testOtp.setVerifiedBy("staff123");
        testOtp.setCreatedAt(LocalDateTime.now());
        testOtp.setExpiresAt(LocalDateTime.now().plusMinutes(10));
    }

    @Test
    void testSendOtpForPatientVerification_Success() {
        // Arrange
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(otpRepository.findByPhoneNumberAndCreatedAtAfter(anyString(), any(LocalDateTime.class)))
            .thenReturn(new ArrayList<>());
        when(otpRepository.save(any(OtpVerification.class))).thenReturn(testOtp);
        when(smsGateway.sendOtpSms(anyString(), anyString(), anyString())).thenReturn(true);

        // Act
        OtpVerification result = otpService.sendOtpForPatientVerification(1L, "staff123");

        // Assert
        assertNotNull(result);
        assertEquals("+94771234567", result.getPhoneNumber());
        assertEquals("IDENTITY_VERIFICATION", result.getPurpose());
        assertEquals(1L, result.getPatientId());
        assertEquals("staff123", result.getVerifiedBy());

        verify(patientRepository).findById(1L);
        verify(smsGateway).sendOtpSms(eq("+94771234567"), anyString(), eq("John Doe"));
        verify(otpRepository).save(any(OtpVerification.class));
    }

    @Test
    void testSendOtpForPatientVerification_PatientNotFound() {
        // Arrange
        when(patientRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            otpService.sendOtpForPatientVerification(1L, "staff123");
        });

        verify(patientRepository).findById(1L);
        verify(otpRepository, never()).save(any());
        verify(smsGateway, never()).sendOtpSms(anyString(), anyString(), anyString());
    }

    @Test
    void testSendOtpForPatientVerification_NoPhoneNumber() {
        // Arrange
        testPatient.setPhone(null);
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            otpService.sendOtpForPatientVerification(1L, "staff123");
        });

        assertEquals("Patient does not have a phone number registered", exception.getMessage());
        verify(smsGateway, never()).sendOtpSms(anyString(), anyString(), anyString());
    }

    @Test
    void testSendOtpForPatientVerification_EmptyPhoneNumber() {
        // Arrange
        testPatient.setPhone("");
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            otpService.sendOtpForPatientVerification(1L, "staff123");
        });

        assertEquals("Patient does not have a phone number registered", exception.getMessage());
    }

    @Test
    void testSendOtpForPatientVerification_RateLimitExceeded() {
        // Arrange
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        
        // Create 3 recent requests (maximum allowed)
        List<OtpVerification> recentRequests = Arrays.asList(
            new OtpVerification(), new OtpVerification(), new OtpVerification()
        );
        when(otpRepository.findByPhoneNumberAndCreatedAtAfter(anyString(), any(LocalDateTime.class)))
            .thenReturn(recentRequests);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            otpService.sendOtpForPatientVerification(1L, "staff123");
        });

        assertEquals("Too many OTP requests. Please try again later.", exception.getMessage());
        verify(smsGateway, never()).sendOtpSms(anyString(), anyString(), anyString());
    }

    @Test
    void testSendOtpForPatientVerification_SmsFailure_NoException() {
        // Arrange
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(otpRepository.findByPhoneNumberAndCreatedAtAfter(anyString(), any(LocalDateTime.class)))
            .thenReturn(new ArrayList<>());
        when(otpRepository.save(any(OtpVerification.class))).thenReturn(testOtp);
        when(smsGateway.sendOtpSms(anyString(), anyString(), anyString())).thenReturn(false);

        // Act
        OtpVerification result = otpService.sendOtpForPatientVerification(1L, "staff123");

        // Assert
        assertNotNull(result); // Should not throw exception, just log warning
        verify(smsGateway).sendOtpSms(anyString(), anyString(), anyString());
    }

    @Test
    void testVerifyOtp_Success() {
        // Arrange
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(otpRepository.findTopByPhoneNumberAndUsedFalseAndVerifiedFalseOrderByCreatedAtDesc(anyString()))
            .thenReturn(Optional.of(testOtp));
        when(otpRepository.save(any(OtpVerification.class))).thenReturn(testOtp);

        // Act
        boolean result = otpService.verifyOtp(1L, "123456", "staff123");

        // Assert
        assertTrue(result);
        
        ArgumentCaptor<OtpVerification> captor = ArgumentCaptor.forClass(OtpVerification.class);
        verify(otpRepository).save(captor.capture());
        
        OtpVerification savedOtp = captor.getValue();
        assertTrue(savedOtp.isVerified());
        assertTrue(savedOtp.isUsed());
        assertNotNull(savedOtp.getVerifiedAt());
    }

    @Test
    void testVerifyOtp_PatientNotFound() {
        // Arrange
        when(patientRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            otpService.verifyOtp(1L, "123456", "staff123");
        });
    }

    @Test
    void testVerifyOtp_NoActiveOtp() {
        // Arrange
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(otpRepository.findTopByPhoneNumberAndUsedFalseAndVerifiedFalseOrderByCreatedAtDesc(anyString()))
            .thenReturn(Optional.empty());

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            otpService.verifyOtp(1L, "123456", "staff123");
        });

        assertEquals("No active OTP found. Please request a new OTP.", exception.getMessage());
    }

    @Test
    void testVerifyOtp_Expired() {
        // Arrange
        testOtp.setExpiresAt(LocalDateTime.now().minusMinutes(1)); // Expired
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(otpRepository.findTopByPhoneNumberAndUsedFalseAndVerifiedFalseOrderByCreatedAtDesc(anyString()))
            .thenReturn(Optional.of(testOtp));

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            otpService.verifyOtp(1L, "123456", "staff123");
        });

        assertEquals("OTP has expired. Please request a new OTP.", exception.getMessage());
    }

    @Test
    void testVerifyOtp_MaxAttemptsExceeded() {
        // Arrange
        testOtp.setAttempts(3); // Max attempts
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(otpRepository.findTopByPhoneNumberAndUsedFalseAndVerifiedFalseOrderByCreatedAtDesc(anyString()))
            .thenReturn(Optional.of(testOtp));

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            otpService.verifyOtp(1L, "123456", "staff123");
        });

        assertEquals("Maximum verification attempts exceeded. Please request a new OTP.", exception.getMessage());
        
        ArgumentCaptor<OtpVerification> captor = ArgumentCaptor.forClass(OtpVerification.class);
        verify(otpRepository).save(captor.capture());
        assertTrue(captor.getValue().isUsed());
    }

    @Test
    void testVerifyOtp_InvalidCode_FirstAttempt() {
        // Arrange
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(otpRepository.findTopByPhoneNumberAndUsedFalseAndVerifiedFalseOrderByCreatedAtDesc(anyString()))
            .thenReturn(Optional.of(testOtp));

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            otpService.verifyOtp(1L, "999999", "staff123"); // Wrong code
        });

        assertTrue(exception.getMessage().contains("Invalid OTP code"));
        assertTrue(exception.getMessage().contains("2 attempt(s) remaining"));
        
        ArgumentCaptor<OtpVerification> captor = ArgumentCaptor.forClass(OtpVerification.class);
        verify(otpRepository).save(captor.capture());
        assertEquals(1, captor.getValue().getAttempts());
    }

    @Test
    void testVerifyOtp_InvalidCode_SecondAttempt() {
        // Arrange
        testOtp.setAttempts(1);
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(otpRepository.findTopByPhoneNumberAndUsedFalseAndVerifiedFalseOrderByCreatedAtDesc(anyString()))
            .thenReturn(Optional.of(testOtp));

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            otpService.verifyOtp(1L, "999999", "staff123");
        });

        assertTrue(exception.getMessage().contains("1 attempt(s) remaining"));
        
        ArgumentCaptor<OtpVerification> captor = ArgumentCaptor.forClass(OtpVerification.class);
        verify(otpRepository).save(captor.capture());
        assertEquals(2, captor.getValue().getAttempts());
    }

    @Test
    void testVerifyOtp_InvalidCode_LastAttempt() {
        // Arrange
        testOtp.setAttempts(2);
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(otpRepository.findTopByPhoneNumberAndUsedFalseAndVerifiedFalseOrderByCreatedAtDesc(anyString()))
            .thenReturn(Optional.of(testOtp));

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            otpService.verifyOtp(1L, "999999", "staff123");
        });

        assertTrue(exception.getMessage().contains("0 attempt(s) remaining"));
    }

    @Test
    void testHasRecentVerification_True() {
        // Arrange
        OtpVerification verifiedOtp = new OtpVerification();
        verifiedOtp.setVerified(true);
        verifiedOtp.setCreatedAt(LocalDateTime.now().minusMinutes(5));
        
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(otpRepository.findByPhoneNumberAndCreatedAtAfter(anyString(), any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(verifiedOtp));

        // Act
        boolean result = otpService.hasRecentVerification(1L);

        // Assert
        assertTrue(result);
    }

    @Test
    void testHasRecentVerification_False_NoVerifications() {
        // Arrange
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(otpRepository.findByPhoneNumberAndCreatedAtAfter(anyString(), any(LocalDateTime.class)))
            .thenReturn(new ArrayList<>());

        // Act
        boolean result = otpService.hasRecentVerification(1L);

        // Assert
        assertFalse(result);
    }

    @Test
    void testHasRecentVerification_False_UnverifiedOtp() {
        // Arrange
        OtpVerification unverifiedOtp = new OtpVerification();
        unverifiedOtp.setVerified(false);
        unverifiedOtp.setCreatedAt(LocalDateTime.now().minusMinutes(5));
        
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(otpRepository.findByPhoneNumberAndCreatedAtAfter(anyString(), any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(unverifiedOtp));

        // Act
        boolean result = otpService.hasRecentVerification(1L);

        // Assert
        assertFalse(result);
    }

    @Test
    void testHasRecentVerification_PatientNotFound() {
        // Arrange
        when(patientRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            otpService.hasRecentVerification(1L);
        });
    }

    @Test
    void testCleanupExpiredOtps() {
        // Arrange
        OtpVerification expiredOtp1 = new OtpVerification();
        expiredOtp1.setId(1L);
        expiredOtp1.setUsed(false);
        
        OtpVerification expiredOtp2 = new OtpVerification();
        expiredOtp2.setId(2L);
        expiredOtp2.setUsed(false);
        
        List<OtpVerification> expiredOtps = Arrays.asList(expiredOtp1, expiredOtp2);
        when(otpRepository.findByExpiresAtBeforeAndUsedFalse(any(LocalDateTime.class)))
            .thenReturn(expiredOtps);
        when(otpRepository.save(any(OtpVerification.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        otpService.cleanupExpiredOtps();

        // Assert
        verify(otpRepository).findByExpiresAtBeforeAndUsedFalse(any(LocalDateTime.class));
        verify(otpRepository, times(2)).save(any(OtpVerification.class));
        
        assertTrue(expiredOtp1.isUsed());
        assertTrue(expiredOtp2.isUsed());
    }

    @Test
    void testCleanupExpiredOtps_NoExpiredOtps() {
        // Arrange
        when(otpRepository.findByExpiresAtBeforeAndUsedFalse(any(LocalDateTime.class)))
            .thenReturn(new ArrayList<>());

        // Act
        otpService.cleanupExpiredOtps();

        // Assert
        verify(otpRepository).findByExpiresAtBeforeAndUsedFalse(any(LocalDateTime.class));
        verify(otpRepository, never()).save(any());
    }

    @Test
    void testSendOtpForPatientVerification_GeneratesValidOtpCode() {
        // Arrange
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(otpRepository.findByPhoneNumberAndCreatedAtAfter(anyString(), any(LocalDateTime.class)))
            .thenReturn(new ArrayList<>());
        when(otpRepository.save(any(OtpVerification.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(smsGateway.sendOtpSms(anyString(), anyString(), anyString())).thenReturn(true);

        // Act
        OtpVerification result = otpService.sendOtpForPatientVerification(1L, "staff123");

        // Assert
        assertNotNull(result.getOtpCode());
        assertEquals(6, result.getOtpCode().length());
        assertTrue(result.getOtpCode().matches("\\d{6}")); // 6 digits
    }

    @Test
    void testSendOtpForPatientVerification_SetsCorrectExpiryTime() {
        // Arrange
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(otpRepository.findByPhoneNumberAndCreatedAtAfter(anyString(), any(LocalDateTime.class)))
            .thenReturn(new ArrayList<>());
        when(smsGateway.sendOtpSms(anyString(), anyString(), anyString())).thenReturn(true);
        
        ArgumentCaptor<OtpVerification> captor = ArgumentCaptor.forClass(OtpVerification.class);
        when(otpRepository.save(captor.capture())).thenAnswer(invocation -> {
            OtpVerification otp = invocation.getArgument(0);
            // Simulate @PrePersist behavior
            if (otp.getCreatedAt() == null) {
                otp.setCreatedAt(LocalDateTime.now());
            }
            if (otp.getExpiresAt() == null) {
                otp.setExpiresAt(otp.getCreatedAt().plusMinutes(10));
            }
            return otp;
        });

        // Act
        LocalDateTime beforeCall = LocalDateTime.now();
        otpService.sendOtpForPatientVerification(1L, "staff123");
        LocalDateTime afterCall = LocalDateTime.now();

        // Assert
        OtpVerification savedOtp = captor.getValue();
        assertNotNull(savedOtp);
        assertNotNull(savedOtp.getExpiresAt());
        assertTrue(savedOtp.getExpiresAt().isAfter(beforeCall.plusMinutes(9))); // At least 9 minutes from now
        assertTrue(savedOtp.getExpiresAt().isBefore(afterCall.plusMinutes(11))); // At most 11 minutes from now
    }

    @Test
    void testVerifyOtp_CorrectCode_AllFieldsUpdated() {
        // Arrange
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(otpRepository.findTopByPhoneNumberAndUsedFalseAndVerifiedFalseOrderByCreatedAtDesc(anyString()))
            .thenReturn(Optional.of(testOtp));
        
        ArgumentCaptor<OtpVerification> captor = ArgumentCaptor.forClass(OtpVerification.class);
        when(otpRepository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        LocalDateTime beforeVerify = LocalDateTime.now();
        boolean result = otpService.verifyOtp(1L, "123456", "staff123");
        LocalDateTime afterVerify = LocalDateTime.now();

        // Assert
        assertTrue(result);
        OtpVerification savedOtp = captor.getValue();
        assertTrue(savedOtp.isVerified());
        assertTrue(savedOtp.isUsed());
        assertNotNull(savedOtp.getVerifiedAt());
        assertTrue(savedOtp.getVerifiedAt().isAfter(beforeVerify.minusSeconds(1)));
        assertTrue(savedOtp.getVerifiedAt().isBefore(afterVerify.plusSeconds(1)));
    }
}
