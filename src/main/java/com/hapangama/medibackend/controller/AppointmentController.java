package com.hapangama.medibackend.controller;

import com.hapangama.medibackend.dto.*;
import com.hapangama.medibackend.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @GetMapping("/providers")
    public ResponseEntity<List<ProviderResponse>> getProviders(
            @RequestParam(required = false) String specialty) {
        List<ProviderResponse> providers = appointmentService.getProvidersBySpecialty(specialty);
        return ResponseEntity.ok(providers);
    }

    @GetMapping("/timeslots")
    public ResponseEntity<List<TimeSlotResponse>> getAvailableTimeSlots(
            @RequestParam Long providerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) {
        List<TimeSlotResponse> timeSlots = appointmentService.getAvailableTimeSlots(providerId, date);
        return ResponseEntity.ok(timeSlots);
    }

    @PostMapping("/book")
    public ResponseEntity<AppointmentResponse> bookAppointment(
            @RequestBody BookAppointmentRequest request) {
        AppointmentResponse response = appointmentService.bookAppointment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/payment")
    public ResponseEntity<AppointmentResponse> processPayment(
            @RequestBody PaymentRequest request) {
        AppointmentResponse response = appointmentService.processPayment(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/confirmation/{confirmationNumber}")
    public ResponseEntity<AppointmentResponse> getAppointmentByConfirmation(
            @PathVariable String confirmationNumber) {
        AppointmentResponse response = appointmentService.getAppointmentByConfirmationNumber(confirmationNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<AppointmentResponse>> getPatientAppointments(
            @PathVariable Long patientId) {
        List<AppointmentResponse> appointments = appointmentService.getPatientAppointments(patientId);
        return ResponseEntity.ok(appointments);
    }
}
