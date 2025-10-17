package com.hapangama.medibackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "utilization_reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UtilizationReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String reportName;

    @Column(nullable = false)
    private LocalDate reportDate;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    private String department;

    private String doctor;

    private String serviceCategory;

    @Column(nullable = false)
    private Integer totalServices;

    @Column(nullable = false)
    private Integer totalPatients;

    @Column(nullable = false)
    private Double averageUtilization;

    private String peakHours;

    @Column(length = 2000)
    private String notes;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        reportDate = LocalDate.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
