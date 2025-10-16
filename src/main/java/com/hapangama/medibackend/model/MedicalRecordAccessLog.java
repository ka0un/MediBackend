package com.hapangama.medibackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "medical_record_access_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecordAccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long patientId;

    @Column(nullable = false)
    private String staffId;

    @Column(nullable = false)
    private String accessType; // VIEW, DOWNLOAD, UPDATE

    @Column(nullable = false)
    private LocalDateTime accessTimestamp;

    @Column(length = 1000)
    private String purpose;

    private String ipAddress;

    @Column(nullable = false)
    private Boolean accessGranted;

    private String denialReason;
}
