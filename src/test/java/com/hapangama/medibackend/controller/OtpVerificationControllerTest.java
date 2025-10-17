package com.hapangama.medibackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hapangama.medibackend.dto.*;
import com.hapangama.medibackend.exception.BadRequestException;
import com.hapangama.medibackend.exception.NotFoundException;
import com.hapangama.medibackend.model.OtpVerification;
import com.hapangama.medibackend.service.OtpVerificationService;
import com.hapangama.medibackend.util.PhoneNumberMasker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OtpVerificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OtpVerificationService otpService;

    @MockitoBean
    private PhoneNumberMasker phoneNumberMasker;

    private OtpVerification testOtp;

    @BeforeEach
    void setUp() {
        testOtp = new OtpVerification();
        testOtp.setId(1L);
        testOtp.setPhoneNumber("+94771234567");
        testOtp.setOtpCode("123456");
        testOtp.setPurpose("IDENTITY_VERIFICATION");
        testOtp.setPatientId(1L);
        testOtp.setVerifiedBy("staff123");
        testOtp.setCreatedAt(LocalDateTime.now());
        testOtp.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        
        // Mock phone number masker
        when(phoneNumberMasker.maskPhoneNumber(anyString())).thenAnswer(invocation -> {
            String phone = invocation.getArgument(0);
            if (phone == null || phone.length() < 4) {
                return "****";
            }
            String lastDigits = phone.substring(phone.length() - 4);
            return "*".repeat(phone.length() - 4) + lastDigits;
        });
    }

    @Test
    void testSendOtp_Success() throws Exception {
        // Arrange
        OtpSendRequest request = new OtpSendRequest(1L, "staff123");
        when(otpService.sendOtpForPatientVerification(1L, "staff123")).thenReturn(testOtp);

        // Act & Assert
        mockMvc.perform(post("/api/otp/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("OTP sent successfully to patient's phone"))
            .andExpect(jsonPath("$.phoneNumber").value("********4567"))
            .andExpect(jsonPath("$.expiresAt").exists());

        verify(otpService).sendOtpForPatientVerification(1L, "staff123");
    }

    @Test
    void testSendOtp_PatientNotFound() throws Exception {
        // Arrange
        OtpSendRequest request = new OtpSendRequest(1L, "staff123");
        when(otpService.sendOtpForPatientVerification(1L, "staff123"))
            .thenThrow(new NotFoundException("Patient not found with ID: 1"));

        // Act & Assert
        mockMvc.perform(post("/api/otp/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());

        verify(otpService).sendOtpForPatientVerification(1L, "staff123");
    }

    @Test
    void testSendOtp_NoPhoneNumber() throws Exception {
        // Arrange
        OtpSendRequest request = new OtpSendRequest(1L, "staff123");
        when(otpService.sendOtpForPatientVerification(1L, "staff123"))
            .thenThrow(new BadRequestException("Patient does not have a phone number registered"));

        // Act & Assert
        mockMvc.perform(post("/api/otp/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        verify(otpService).sendOtpForPatientVerification(1L, "staff123");
    }

    @Test
    void testSendOtp_RateLimitExceeded() throws Exception {
        // Arrange
        OtpSendRequest request = new OtpSendRequest(1L, "staff123");
        when(otpService.sendOtpForPatientVerification(1L, "staff123"))
            .thenThrow(new BadRequestException("Too many OTP requests. Please try again later."));

        // Act & Assert
        mockMvc.perform(post("/api/otp/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        verify(otpService).sendOtpForPatientVerification(1L, "staff123");
    }

    @Test
    void testVerifyOtp_Success() throws Exception {
        // Arrange
        OtpVerifyRequest request = new OtpVerifyRequest(1L, "123456", "staff123");
        when(otpService.verifyOtp(1L, "123456", "staff123")).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/otp/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Identity verified successfully"));

        verify(otpService).verifyOtp(1L, "123456", "staff123");
    }

    @Test
    void testVerifyOtp_InvalidCode() throws Exception {
        // Arrange
        OtpVerifyRequest request = new OtpVerifyRequest(1L, "999999", "staff123");
        when(otpService.verifyOtp(1L, "999999", "staff123"))
            .thenThrow(new BadRequestException("Invalid OTP code. 2 attempt(s) remaining."));

        // Act & Assert
        mockMvc.perform(post("/api/otp/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        verify(otpService).verifyOtp(1L, "999999", "staff123");
    }

    @Test
    void testVerifyOtp_Expired() throws Exception {
        // Arrange
        OtpVerifyRequest request = new OtpVerifyRequest(1L, "123456", "staff123");
        when(otpService.verifyOtp(1L, "123456", "staff123"))
            .thenThrow(new BadRequestException("OTP has expired. Please request a new OTP."));

        // Act & Assert
        mockMvc.perform(post("/api/otp/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        verify(otpService).verifyOtp(1L, "123456", "staff123");
    }

    @Test
    void testVerifyOtp_NoActiveOtp() throws Exception {
        // Arrange
        OtpVerifyRequest request = new OtpVerifyRequest(1L, "123456", "staff123");
        when(otpService.verifyOtp(1L, "123456", "staff123"))
            .thenThrow(new BadRequestException("No active OTP found. Please request a new OTP."));

        // Act & Assert
        mockMvc.perform(post("/api/otp/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        verify(otpService).verifyOtp(1L, "123456", "staff123");
    }

    @Test
    void testVerifyOtp_MaxAttemptsExceeded() throws Exception {
        // Arrange
        OtpVerifyRequest request = new OtpVerifyRequest(1L, "123456", "staff123");
        when(otpService.verifyOtp(1L, "123456", "staff123"))
            .thenThrow(new BadRequestException("Maximum verification attempts exceeded. Please request a new OTP."));

        // Act & Assert
        mockMvc.perform(post("/api/otp/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        verify(otpService).verifyOtp(1L, "123456", "staff123");
    }

    @Test
    void testCheckVerification_HasRecentVerification() throws Exception {
        // Arrange
        when(otpService.hasRecentVerification(1L)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/api/otp/check-verification")
                .param("patientId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.verified").value(true))
            .andExpect(jsonPath("$.message").value("Patient has recent verification"));

        verify(otpService).hasRecentVerification(1L);
    }

    @Test
    void testCheckVerification_NoRecentVerification() throws Exception {
        // Arrange
        when(otpService.hasRecentVerification(1L)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/otp/check-verification")
                .param("patientId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.verified").value(false))
            .andExpect(jsonPath("$.message").value("No recent verification found"));

        verify(otpService).hasRecentVerification(1L);
    }

    @Test
    void testCheckVerification_PatientNotFound() throws Exception {
        // Arrange
        when(otpService.hasRecentVerification(1L))
            .thenThrow(new NotFoundException("Patient not found with ID: 1"));

        // Act & Assert
        mockMvc.perform(get("/api/otp/check-verification")
                .param("patientId", "1"))
            .andExpect(status().isNotFound());

        verify(otpService).hasRecentVerification(1L);
    }

    @Test
    void testSendOtp_PhoneNumberMasking() throws Exception {
        // Arrange
        OtpSendRequest request = new OtpSendRequest(1L, "staff123");
        when(otpService.sendOtpForPatientVerification(1L, "staff123")).thenReturn(testOtp);

        // Act & Assert
        mockMvc.perform(post("/api/otp/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.phoneNumber").value("********4567"));

        verify(phoneNumberMasker).maskPhoneNumber("+94771234567");
    }

    @Test
    void testVerifyOtp_FailureMessage() throws Exception {
        // Arrange
        OtpVerifyRequest request = new OtpVerifyRequest(1L, "123456", "staff123");
        when(otpService.verifyOtp(1L, "123456", "staff123")).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/api/otp/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Verification failed"));
    }
}
