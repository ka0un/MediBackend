package com.hapangama.medibackend.dto;

import com.hapangama.medibackend.model.Appointment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {
    private Long id;
    private Long patientId;
    private String patientName;
    private Long providerId;
    private String providerName;
    private String specialty;
    private LocalDateTime appointmentTime;
    private String confirmationNumber;
    private Appointment.AppointmentStatus status;
    private Boolean paymentRequired;
    private String hospitalName;
}
