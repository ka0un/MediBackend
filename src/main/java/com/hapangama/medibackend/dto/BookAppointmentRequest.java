package com.hapangama.medibackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookAppointmentRequest {
    private Long patientId;
    private Long providerId;
    private Long timeSlotId;
}
