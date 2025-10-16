package com.hapangama.medibackend.controller;

import com.hapangama.medibackend.dto.AppointmentResponse;
import com.hapangama.medibackend.dto.PatientProfileResponse;
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
}
