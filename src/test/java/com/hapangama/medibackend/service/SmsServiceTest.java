package com.hapangama.medibackend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmsServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private PhoneNumberFormatter phoneNumberFormatter;

    @InjectMocks
    private SmsService smsService;

    @BeforeEach
    void setUp() {
        // Inject field values using ReflectionTestUtils
        ReflectionTestUtils.setField(smsService, "smsApiUrl", "https://api.test.lk/sms/send");
        ReflectionTestUtils.setField(smsService, "smsApiToken", "test-token-123");
        ReflectionTestUtils.setField(smsService, "smsSenderId", "TESTAPP");
        
        // Setup formatter to return formatted number (lenient to avoid unnecessary stubbing warnings)
        lenient().when(phoneNumberFormatter.formatPhoneNumber(anyString())).thenAnswer(invocation -> {
            String phone = invocation.getArgument(0);
            String cleaned = phone.replaceAll("[^0-9]", "");
            if (cleaned.startsWith("0")) {
                return "94" + cleaned.substring(1);
            }
            if (cleaned.startsWith("94")) {
                return cleaned;
            }
            return "94" + cleaned;
        });
    }

    @Test
    void testSendSMS_Success() {
        // Arrange
        String phoneNumber = "+94771234567";
        String message = "Test message";
        
        ResponseEntity<String> mockResponse = new ResponseEntity<>("Success", HttpStatus.OK);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(mockResponse);

        // Act
        boolean result = smsService.sendSms(phoneNumber, message);

        // Assert
        assertTrue(result);
        verify(restTemplate, times(1)).exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        );
    }

    @Test
    void testSendSMS_WithLocalPhoneNumber_Success() {
        // Arrange
        String phoneNumber = "0771234567"; // Local format
        String message = "Test message";
        
        ResponseEntity<String> mockResponse = new ResponseEntity<>("Success", HttpStatus.OK);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(mockResponse);

        // Act
        boolean result = smsService.sendSms(phoneNumber, message);

        // Assert
        assertTrue(result);
        
        // Verify that phone number was formatted correctly
        ArgumentCaptor<HttpEntity<?>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.POST),
                captor.capture(),
                eq(String.class)
        );
        
        @SuppressWarnings("unchecked")
        Map<String, String> requestBody = (Map<String, String>) captor.getValue().getBody();
        assertEquals("94771234567", requestBody.get("recipient"));
    }

    @Test
    void testSendSMS_NoConfiguration_DevelopmentMode() {
        // Arrange
        ReflectionTestUtils.setField(smsService, "smsApiUrl", null);
        ReflectionTestUtils.setField(smsService, "smsApiToken", null);
        
        String phoneNumber = "+94771234567";
        String message = "Test message";

        // Act
        boolean result = smsService.sendSms(phoneNumber, message);

        // Assert
        assertTrue(result); // Should return true in development mode
        verify(restTemplate, never()).exchange(anyString(), any(), any(), eq(String.class));
    }

    @Test
    void testSendSMS_EmptyPhoneNumber_ThrowsException() {
        // Arrange
        String phoneNumber = "";
        String message = "Test message";

        // Act
        boolean result = smsService.sendSms(phoneNumber, message);

        // Assert
        assertFalse(result); // Should catch exception and return false
        verify(restTemplate, never()).exchange(anyString(), any(), any(), eq(String.class));
    }

    @Test
    void testSendSMS_NullPhoneNumber_ThrowsException() {
        // Arrange
        String message = "Test message";

        // Act
        boolean result = smsService.sendSms(null, message);

        // Assert
        assertFalse(result); // Should catch exception and return false
        verify(restTemplate, never()).exchange(anyString(), any(), any(), eq(String.class));
    }

    @Test
    void testSendSMS_RestTemplateThrowsException_ReturnsFalse() {
        // Arrange
        String phoneNumber = "+94771234567";
        String message = "Test message";
        
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenThrow(new RestClientException("Network error"));

        // Act
        boolean result = smsService.sendSms(phoneNumber, message);

        // Assert
        assertFalse(result);
    }

    @Test
    void testSendSMS_Non2xxResponse_ReturnsFalse() {
        // Arrange
        String phoneNumber = "+94771234567";
        String message = "Test message";
        
        ResponseEntity<String> mockResponse = new ResponseEntity<>("Error", HttpStatus.INTERNAL_SERVER_ERROR);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(mockResponse);

        // Act
        boolean result = smsService.sendSms(phoneNumber, message);

        // Assert
        assertFalse(result);
    }

    @Test
    void testSendOtpSms_WithPatientName_Success() {
        // Arrange
        String phoneNumber = "+94771234567";
        String otpCode = "123456";
        String patientName = "John Doe";
        
        ResponseEntity<String> mockResponse = new ResponseEntity<>("Success", HttpStatus.OK);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(mockResponse);

        // Act
        boolean result = smsService.sendOtpSms(phoneNumber, otpCode, patientName);

        // Assert
        assertTrue(result);
        
        // Verify message contains patient name
        ArgumentCaptor<HttpEntity<?>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.POST),
                captor.capture(),
                eq(String.class)
        );
        
        @SuppressWarnings("unchecked")
        Map<String, String> requestBody = (Map<String, String>) captor.getValue().getBody();
        String sentMessage = requestBody.get("message");
        assertTrue(sentMessage.contains(patientName));
        assertTrue(sentMessage.contains(otpCode));
    }

    @Test
    void testSendOtpSms_WithoutPatientName_Success() {
        // Arrange
        String phoneNumber = "+94771234567";
        String otpCode = "123456";
        
        ResponseEntity<String> mockResponse = new ResponseEntity<>("Success", HttpStatus.OK);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(mockResponse);

        // Act
        boolean result = smsService.sendOtpSms(phoneNumber, otpCode, null);

        // Assert
        assertTrue(result);
        
        // Verify message contains OTP
        ArgumentCaptor<HttpEntity<?>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.POST),
                captor.capture(),
                eq(String.class)
        );
        
        @SuppressWarnings("unchecked")
        Map<String, String> requestBody = (Map<String, String>) captor.getValue().getBody();
        String sentMessage = requestBody.get("message");
        assertTrue(sentMessage.contains(otpCode));
        assertTrue(sentMessage.contains("MediSystem Identity Verification"));
    }

    @Test
    void testSendOtpSms_EmptyPatientName_Success() {
        // Arrange
        String phoneNumber = "+94771234567";
        String otpCode = "123456";
        
        ResponseEntity<String> mockResponse = new ResponseEntity<>("Success", HttpStatus.OK);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(mockResponse);

        // Act
        boolean result = smsService.sendOtpSms(phoneNumber, otpCode, "");

        // Assert
        assertTrue(result);
    }

    @Test
    void testFormatPhoneNumber_LocalFormat() {
        // Arrange
        ReflectionTestUtils.setField(smsService, "smsApiUrl", "https://api.test.lk/sms/send");
        String phoneNumber = "0771234567";
        String message = "Test";
        
        ResponseEntity<String> mockResponse = new ResponseEntity<>("Success", HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
            .thenReturn(mockResponse);

        // Act
        smsService.sendSms(phoneNumber, message);

        // Assert
        ArgumentCaptor<HttpEntity<?>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), captor.capture(), eq(String.class));
        
        @SuppressWarnings("unchecked")
        Map<String, String> requestBody = (Map<String, String>) captor.getValue().getBody();
        assertEquals("94771234567", requestBody.get("recipient"));
    }

    @Test
    void testFormatPhoneNumber_InternationalFormat() {
        // Arrange
        String phoneNumber = "+94771234567";
        String message = "Test";
        
        ResponseEntity<String> mockResponse = new ResponseEntity<>("Success", HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
            .thenReturn(mockResponse);

        // Act
        smsService.sendSms(phoneNumber, message);

        // Assert
        ArgumentCaptor<HttpEntity<?>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), captor.capture(), eq(String.class));
        
        @SuppressWarnings("unchecked")
        Map<String, String> requestBody = (Map<String, String>) captor.getValue().getBody();
        assertEquals("94771234567", requestBody.get("recipient"));
    }

    @Test
    void testFormatPhoneNumber_AlreadyFormatted() {
        // Arrange
        String phoneNumber = "94771234567";
        String message = "Test";
        
        ResponseEntity<String> mockResponse = new ResponseEntity<>("Success", HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
            .thenReturn(mockResponse);

        // Act
        smsService.sendSms(phoneNumber, message);

        // Assert
        ArgumentCaptor<HttpEntity<?>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), captor.capture(), eq(String.class));
        
        @SuppressWarnings("unchecked")
        Map<String, String> requestBody = (Map<String, String>) captor.getValue().getBody();
        assertEquals("94771234567", requestBody.get("recipient"));
    }

    @Test
    void testSendSMS_VerifyHeaders() {
        // Arrange
        String phoneNumber = "+94771234567";
        String message = "Test message";
        
        ResponseEntity<String> mockResponse = new ResponseEntity<>("Success", HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
            .thenReturn(mockResponse);

        // Act
        smsService.sendSms(phoneNumber, message);

        // Assert
        ArgumentCaptor<HttpEntity<?>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), captor.capture(), eq(String.class));
        
        HttpEntity<?> request = captor.getValue();
        assertEquals("Bearer test-token-123", request.getHeaders().getFirst("Authorization"));
        assertEquals("application/json", request.getHeaders().getFirst("Content-Type"));
        assertEquals("application/json", request.getHeaders().getFirst("Accept"));
    }

    @Test
    void testSendSMS_VerifyRequestBody() {
        // Arrange
        String phoneNumber = "0771234567";
        String message = "Test message";
        
        ResponseEntity<String> mockResponse = new ResponseEntity<>("Success", HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
            .thenReturn(mockResponse);

        // Act
        smsService.sendSms(phoneNumber, message);

        // Assert
        ArgumentCaptor<HttpEntity<?>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), captor.capture(), eq(String.class));
        
        @SuppressWarnings("unchecked")
        Map<String, String> requestBody = (Map<String, String>) captor.getValue().getBody();
        assertEquals("94771234567", requestBody.get("recipient"));
        assertEquals("TESTAPP", requestBody.get("sender_id"));
        assertEquals("plain", requestBody.get("type"));
        assertEquals(message, requestBody.get("message"));
    }
}
