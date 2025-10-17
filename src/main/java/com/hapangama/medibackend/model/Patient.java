package com.hapangama.medibackend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

@Entity
@Table(name = "patients")
@Data
@NoArgsConstructor
@ToString(exclude = {"medications", "prescriptions", "testResults", "vaccinations"})
@EqualsAndHashCode(exclude = {"medications", "prescriptions", "testResults", "vaccinations"})
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version; // Optimistic locking for concurrent access (UC-04 E5)

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false, unique = true)
    private String digitalHealthCardNumber;

    // Additional fields for UC-02
    private String address;

    private LocalDate dateOfBirth;

    private String emergencyContactName;

    private String emergencyContactPhone;

    @Column(length = 2000)
    private String medicalHistory;

    private String bloodType;

    private String allergies;

    // Privacy and Access Control Fields (UC-04 E3)
    @Column(nullable = false)
    private boolean restrictedAccess = false; // VIP/Sensitive patient flag

    @Column(length = 1000)
    private String accessRestrictionReason; // Reason for restriction

    @Column(nullable = false)
    private boolean requiresExplicitConsent = false; // Requires additional consent

    @Column(length = 2000)
    private String consentNotes; // Consent management notes

    @ElementCollection
    @CollectionTable(name = "patient_restricted_staff", joinColumns = @JoinColumn(name = "patient_id"))
    @Column(name = "staff_username")
    private java.util.Set<String> restrictedStaffList = new java.util.HashSet<>(); // Staff blocked from access

    @ElementCollection
    @CollectionTable(name = "patient_authorized_staff", joinColumns = @JoinColumn(name = "patient_id"))
    @Column(name = "staff_username")
    private java.util.Set<String> authorizedStaffList = new java.util.HashSet<>(); // Whitelist for restricted patients

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<Medication> medications;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<Prescription> prescriptions;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<TestResult> testResults;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<Vaccination> vaccinations;
}
