package com.hapangama.medibackend.controller;

import com.hapangama.medibackend.dto.*;
import com.hapangama.medibackend.service.AppointmentService;
import com.hapangama.medibackend.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminController {

    private final PatientService patientService;
    private final AppointmentService appointmentService;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        
        List<PatientProfileResponse> patients = patientService.getAllPatients();
        List<AppointmentResponse> appointments = appointmentService.getAllAppointments();
        
        dashboard.put("totalPatients", patients.size());
        dashboard.put("totalAppointments", appointments.size());
        dashboard.put("recentPatients", patients.stream().limit(5).toList());
        dashboard.put("recentAppointments", appointments.stream().limit(5).toList());
        
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/patients")
    public ResponseEntity<List<PatientProfileResponse>> getAllPatients() {
        List<PatientProfileResponse> patients = patientService.getAllPatients();
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/appointments")
    public ResponseEntity<List<AppointmentResponse>> getAllAppointments() {
        List<AppointmentResponse> appointments = appointmentService.getAllAppointments();
        return ResponseEntity.ok(appointments);
    }

    @DeleteMapping("/patients/{patientId}")
    public ResponseEntity<Map<String, String>> deletePatient(@PathVariable Long patientId) {
        patientService.deletePatient(patientId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Patient deleted successfully");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/appointments/{appointmentId}")
    public ResponseEntity<Map<String, String>> cancelAppointment(@PathVariable Long appointmentId) {
        appointmentService.cancelAppointment(appointmentId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Appointment cancelled successfully");
        return ResponseEntity.ok(response);
    }

    // Healthcare Provider Management
    @PostMapping("/providers")
    public ResponseEntity<ProviderResponse> createProvider(@RequestBody CreateProviderRequest request) {
        ProviderResponse provider = appointmentService.createProvider(request);
        return ResponseEntity.ok(provider);
    }

    @GetMapping("/providers/{providerId}")
    public ResponseEntity<ProviderResponse> getProvider(@PathVariable Long providerId) {
        ProviderResponse provider = appointmentService.getProviderById(providerId);
        return ResponseEntity.ok(provider);
    }

    @PutMapping("/providers/{providerId}")
    public ResponseEntity<ProviderResponse> updateProvider(
            @PathVariable Long providerId,
            @RequestBody UpdateProviderRequest request) {
        ProviderResponse provider = appointmentService.updateProvider(providerId, request);
        return ResponseEntity.ok(provider);
    }

    @DeleteMapping("/providers/{providerId}")
    public ResponseEntity<Map<String, String>> deleteProvider(@PathVariable Long providerId) {
        appointmentService.deleteProvider(providerId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Provider deleted successfully");
        return ResponseEntity.ok(response);
    }

    // Time Slot Management
    @PostMapping("/providers/{providerId}/timeslots")
    public ResponseEntity<TimeSlotResponse> createTimeSlot(
            @PathVariable Long providerId,
            @RequestBody CreateTimeSlotRequest request) {
        TimeSlotResponse timeSlot = appointmentService.createTimeSlot(providerId, request);
        return ResponseEntity.ok(timeSlot);
    }

    @GetMapping("/providers/{providerId}/timeslots")
    public ResponseEntity<List<TimeSlotResponse>> getProviderTimeSlots(@PathVariable Long providerId) {
        List<TimeSlotResponse> timeSlots = appointmentService.getTimeSlotsByProvider(providerId);
        return ResponseEntity.ok(timeSlots);
    }

    @PutMapping("/timeslots/{timeSlotId}")
    public ResponseEntity<TimeSlotResponse> updateTimeSlot(
            @PathVariable Long timeSlotId,
            @RequestBody UpdateTimeSlotRequest request) {
        TimeSlotResponse timeSlot = appointmentService.updateTimeSlot(timeSlotId, request);
        return ResponseEntity.ok(timeSlot);
    }

    @DeleteMapping("/timeslots/{timeSlotId}")
    public ResponseEntity<Map<String, String>> deleteTimeSlot(@PathVariable Long timeSlotId) {
        appointmentService.deleteTimeSlot(timeSlotId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Time slot deleted successfully");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/appointments/{appointmentId}/status")
    public ResponseEntity<AppointmentResponse> updateAppointmentStatus(
            @PathVariable Long appointmentId,
            @RequestBody Map<String, String> body) {
        String status = body != null ? body.get("status") : null;
        AppointmentResponse updated = appointmentService.updateAppointmentStatus(appointmentId, status);
        return ResponseEntity.ok(updated);
    }
}
