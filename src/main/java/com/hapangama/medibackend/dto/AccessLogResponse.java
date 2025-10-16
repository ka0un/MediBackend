package com.hapangama.medibackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccessLogResponse {
    private Long id;
    private Long patientId;
    private String staffId;
    private String accessType;
    private LocalDateTime accessTimestamp;
    private String purpose;
    private Boolean accessGranted;
    private String denialReason;
}
