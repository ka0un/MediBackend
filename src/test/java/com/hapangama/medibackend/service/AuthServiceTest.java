package com.hapangama.medibackend.service;

import com.hapangama.medibackend.dto.AuthResponse;
import com.hapangama.medibackend.dto.LoginRequest;
import com.hapangama.medibackend.dto.RegisterRequest;
import com.hapangama.medibackend.model.Patient;
import com.hapangama.medibackend.model.User;
import com.hapangama.medibackend.repository.PatientRepository;
import com.hapangama.medibackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AuthService authService;

    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
    }

    @Test
    void testRegister_Success() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setName("Test User");
        request.setEmail("test@example.com");
        request.setPhone("1234567890");
        request.setDigitalHealthCardNumber("DHC-TEST-001");
        request.setAddress("123 Test St");
        request.setDateOfBirth("1990-01-01");

        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(patientRepository.existsByEmail(anyString())).thenReturn(false);
        when(patientRepository.existsByDigitalHealthCardNumber(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> {
            Patient patient = invocation.getArgument(0);
            patient.setId(1L);
            return patient;
        });

        // Act
        AuthResponse response = authService.register(request);

        // Assert
        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        assertEquals(User.Role.PATIENT, response.getRole());
        assertEquals(1L, response.getPatientId());
        assertEquals("Registration successful", response.getMessage());
        verify(userRepository).save(any(User.class));
        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    void testRegister_UsernameAlreadyExists() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existinguser");
        request.setPassword("password123");

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.register(request);
        });
        assertEquals("Username already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegister_EmailAlreadyExists() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("existing@example.com");

        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(patientRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.register(request);
        });
        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegister_DigitalHealthCardAlreadyExists() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("new@example.com");
        request.setDigitalHealthCardNumber("DHC-EXISTING-001");

        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(patientRepository.existsByEmail(anyString())).thenReturn(false);
        when(patientRepository.existsByDigitalHealthCardNumber("DHC-EXISTING-001")).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.register(request);
        });
        assertEquals("Digital health card number already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLogin_Success() {
        // Arrange
        LoginRequest request = new LoginRequest("testuser", "password123");

        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRole(User.Role.PATIENT);
        user.setActive(true);

        Patient patient = new Patient();
        patient.setId(1L);
        patient.setUser(user);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(patientRepository.findByUser(user)).thenReturn(Optional.of(patient));

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        assertEquals(User.Role.PATIENT, response.getRole());
        assertEquals(1L, response.getPatientId());
        assertEquals("Login successful", response.getMessage());
    }

    @Test
    void testLogin_AdminSuccess() {
        // Arrange
        LoginRequest request = new LoginRequest("admin", "admin");

        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword(passwordEncoder.encode("admin"));
        user.setRole(User.Role.ADMIN);
        user.setActive(true);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals("admin", response.getUsername());
        assertEquals(User.Role.ADMIN, response.getRole());
        assertNull(response.getPatientId());
        assertEquals("Login successful", response.getMessage());
    }

    @Test
    void testLogin_InvalidUsername() {
        // Arrange
        LoginRequest request = new LoginRequest("nonexistent", "password123");

        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(request);
        });
        assertEquals("Invalid username or password", exception.getMessage());
    }

    @Test
    void testLogin_InvalidPassword() {
        // Arrange
        LoginRequest request = new LoginRequest("testuser", "wrongpassword");

        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRole(User.Role.PATIENT);
        user.setActive(true);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(request);
        });
        assertEquals("Invalid username or password", exception.getMessage());
    }

    @Test
    void testLogin_InactiveAccount() {
        // Arrange
        LoginRequest request = new LoginRequest("testuser", "password123");

        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRole(User.Role.PATIENT);
        user.setActive(false);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(request);
        });
        assertEquals("Account is inactive", exception.getMessage());
    }

    @Test
    void testGetUserById_Success() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        User result = authService.getUserById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void testGetUserById_NotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.getUserById(1L);
        });
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testGetUserByUsername_Success() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // Act
        User result = authService.getUserByUsername("testuser");

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void testGetUserByUsername_NotFound() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.getUserByUsername("nonexistent");
        });
        assertEquals("User not found", exception.getMessage());
    }
}
