package com.hapangama.medibackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "test_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(nullable = false)
    private String testName;

    @Column(nullable = false)
    private LocalDate testDate;

    @Column(nullable = false, length = 2000)
    private String result;

    private String resultUnit;

    private String referenceRange;

    private String orderedBy;

    private String performedBy;

    @Column(length = 1000)
    private String notes;
}
