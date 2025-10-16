package com.hapangama.medibackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hapangama.medibackend.dto.LoginRequest;
import com.hapangama.medibackend.dto.RegisterRequest;
import com.hapangama.medibackend.model.User;
import com.hapangama.medibackend.repository.PatientRepository;
import com.hapangama.medibackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        // Clean up database
        patientRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testRegister_Success() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setPassword("password123");
        request.setName("New User");
        request.setEmail("newuser@example.com");
        request.setPhone("1234567890");
        request.setDigitalHealthCardNumber("DHC-NEW-001");
        request.setAddress("123 New St");
        request.setDateOfBirth("1990-01-01");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username", is("newuser")))
                .andExpect(jsonPath("$.role", is("PATIENT")))
                .andExpect(jsonPath("$.patientId", notNullValue()))
                .andExpect(jsonPath("$.message", is("Registration successful")));
    }

    @Test
    void testRegister_UsernameAlreadyExists() throws Exception {
        // Create existing user
        User existingUser = new User();
        existingUser.setUsername("existinguser");
        existingUser.setPassword(passwordEncoder.encode("password123"));
        existingUser.setRole(User.Role.PATIENT);
        existingUser.setActive(true);
        userRepository.save(existingUser);

        RegisterRequest request = new RegisterRequest();
        request.setUsername("existinguser");
        request.setPassword("password123");
        request.setName("Test User");
        request.setEmail("test@example.com");
        request.setPhone("1234567890");
        request.setDigitalHealthCardNumber("DHC-TEST-001");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Username already exists")));
    }

    @Test
    void testLogin_Success() throws Exception {
        // Create test user first via register
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("password123");
        registerRequest.setName("Test User");
        registerRequest.setEmail("testuser@example.com");
        registerRequest.setPhone("1234567890");
        registerRequest.setDigitalHealthCardNumber("DHC-TEST-002");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        // Now login
        LoginRequest loginRequest = new LoginRequest("testuser", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.role", is("PATIENT")))
                .andExpect(jsonPath("$.patientId", notNullValue()))
                .andExpect(jsonPath("$.message", is("Login successful")));
    }

    @Test
    void testLogin_AdminSuccess() throws Exception {
        // Create admin user
        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setPassword(passwordEncoder.encode("admin"));
        adminUser.setRole(User.Role.ADMIN);
        adminUser.setActive(true);
        userRepository.save(adminUser);

        LoginRequest loginRequest = new LoginRequest("admin", "admin");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("admin")))
                .andExpect(jsonPath("$.role", is("ADMIN")))
                .andExpect(jsonPath("$.patientId").doesNotExist())
                .andExpect(jsonPath("$.message", is("Login successful")));
    }

    @Test
    void testLogin_InvalidUsername() throws Exception {
        LoginRequest loginRequest = new LoginRequest("nonexistent", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", is("Invalid username or password")));
    }

    @Test
    void testLogin_InvalidPassword() throws Exception {
        // Create test user
        User user = new User();
        user.setUsername("testuser");
        user.setPassword(passwordEncoder.encode("correctpassword"));
        user.setRole(User.Role.PATIENT);
        user.setActive(true);
        userRepository.save(user);

        LoginRequest loginRequest = new LoginRequest("testuser", "wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", is("Invalid username or password")));
    }

    @Test
    void testLogin_InactiveAccount() throws Exception {
        // Create inactive user
        User user = new User();
        user.setUsername("inactiveuser");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRole(User.Role.PATIENT);
        user.setActive(false);
        userRepository.save(user);

        LoginRequest loginRequest = new LoginRequest("inactiveuser", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", is("Account is inactive")));
    }

    @Test
    void testLogout_Success() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Logout successful")));
    }
}
