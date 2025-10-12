package com.hapangama.medibackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "healthcare_providers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HealthcareProvider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String specialty;

    @Column(nullable = false)
    private String hospitalName;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private HospitalType hospitalType;

    public enum HospitalType {
        GOVERNMENT,  // Free appointments
        PRIVATE      // May require payment
    }
}
