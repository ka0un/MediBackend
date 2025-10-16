package com.hapangama.medibackend.service;

import com.hapangama.medibackend.dto.AuthResponse;
import com.hapangama.medibackend.dto.LoginRequest;
import com.hapangama.medibackend.dto.RegisterRequest;
import com.hapangama.medibackend.model.Patient;
import com.hapangama.medibackend.model.User;
import com.hapangama.medibackend.repository.PatientRepository;
import com.hapangama.medibackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // Check if email already exists
        if (patientRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Check if digital health card number already exists
        if (patientRepository.existsByDigitalHealthCardNumber(request.getDigitalHealthCardNumber())) {
            throw new RuntimeException("Digital health card number already exists");
        }

        // Create user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(User.Role.PATIENT);
        user.setActive(true);
        user = userRepository.save(user);

        // Create patient
        Patient patient = new Patient();
        patient.setUser(user);
        patient.setName(request.getName());
        patient.setEmail(request.getEmail());
        patient.setPhone(request.getPhone());
        patient.setDigitalHealthCardNumber(request.getDigitalHealthCardNumber());
        patient.setAddress(request.getAddress());
        
        if (request.getDateOfBirth() != null && !request.getDateOfBirth().isEmpty()) {
            patient.setDateOfBirth(LocalDate.parse(request.getDateOfBirth()));
        }
        
        patient.setEmergencyContactName(request.getEmergencyContactName());
        patient.setEmergencyContactPhone(request.getEmergencyContactPhone());
        patient.setBloodType(request.getBloodType());
        patient.setAllergies(request.getAllergies());
        patient.setMedicalHistory(request.getMedicalHistory());
        
        patient = patientRepository.save(patient);

        return new AuthResponse(
            user.getId(),
            user.getUsername(),
            user.getRole(),
            patient.getId(),
            "Registration successful"
        );
    }

    public AuthResponse login(LoginRequest request) {
        // Find user by username
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        // Check if user is active
        if (!user.getActive()) {
            throw new RuntimeException("Account is inactive");
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        // Get patient ID if user is a patient
        Long patientId = null;
        if (user.getRole() == User.Role.PATIENT) {
            Patient patient = patientRepository.findByUser(user).orElse(null);
            if (patient != null) {
                patientId = patient.getId();
            }
        }

        return new AuthResponse(
            user.getId(),
            user.getUsername(),
            user.getRole(),
            patientId,
            "Login successful"
        );
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
