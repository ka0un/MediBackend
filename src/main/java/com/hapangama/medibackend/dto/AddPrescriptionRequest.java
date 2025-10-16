package com.hapangama.medibackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddPrescriptionRequest {
    private Long patientId;
    private String staffId; // Doctor/staff member adding the prescription
    private String diagnosis;
    private String treatment;
    private String notes;
    private String medications;
    private LocalDateTime followUpDate;
}
