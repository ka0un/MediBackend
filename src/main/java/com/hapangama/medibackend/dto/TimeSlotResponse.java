package com.hapangama.medibackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlotResponse {
    private Long id;
    private Long providerId;
    private String providerName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean available;
}
