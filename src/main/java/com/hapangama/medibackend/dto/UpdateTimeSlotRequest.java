package com.hapangama.medibackend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateTimeSlotRequest {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean available;
}
