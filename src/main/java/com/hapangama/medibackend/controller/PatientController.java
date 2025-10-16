package com.hapangama.medibackend.controller;

import com.hapangama.medibackend.dto.CreatePatientRequest;
import com.hapangama.medibackend.dto.PatientProfileResponse;
import com.hapangama.medibackend.dto.UpdatePatientRequest;
import com.hapangama.medibackend.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PatientController {

    private final PatientService patientService;

    @PostMapping
    public ResponseEntity<PatientProfileResponse> createPatient(@RequestBody CreatePatientRequest request) {
        PatientProfileResponse response = patientService.createPatient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{patientId}")
    public ResponseEntity<PatientProfileResponse> getPatientProfile(@PathVariable Long patientId) {
        PatientProfileResponse response = patientService.getPatientProfile(patientId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<PatientProfileResponse>> getAllPatients() {
        List<PatientProfileResponse> response = patientService.getAllPatients();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{patientId}")
    public ResponseEntity<PatientProfileResponse> updatePatientProfile(
            @PathVariable Long patientId,
            @RequestBody UpdatePatientRequest request) {
        PatientProfileResponse response = patientService.updatePatientProfile(patientId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{patientId}")
    public ResponseEntity<Void> deletePatient(@PathVariable Long patientId) {
        patientService.deletePatient(patientId);
        return ResponseEntity.noContent().build();
    }
}
