package com.hapangama.medibackend.controller;

import com.hapangama.medibackend.dto.AccessLogResponse;
import com.hapangama.medibackend.dto.AddPrescriptionRequest;
import com.hapangama.medibackend.dto.MedicalRecordResponse;
import com.hapangama.medibackend.dto.ScanCardRequest;
import com.hapangama.medibackend.service.MedicalRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/medical-records")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    @PostMapping("/scan-card")
    public ResponseEntity<MedicalRecordResponse> scanCard(@RequestBody ScanCardRequest request) {
        MedicalRecordResponse response = medicalRecordService.accessMedicalRecordsByCardNumber(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{patientId}")
    public ResponseEntity<MedicalRecordResponse> getMedicalRecords(
            @PathVariable Long patientId,
            @RequestParam(required = false, defaultValue = "STAFF_DEFAULT") String staffId,
            @RequestParam(required = false, defaultValue = "General consultation") String purpose) {
        MedicalRecordResponse response = medicalRecordService.accessMedicalRecordsByPatientId(patientId, staffId, purpose);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/prescriptions")
    public ResponseEntity<MedicalRecordResponse> addPrescription(@RequestBody AddPrescriptionRequest request) {
        MedicalRecordResponse response = medicalRecordService.addPrescription(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{patientId}/download")
    public ResponseEntity<byte[]> downloadMedicalRecords(
            @PathVariable Long patientId,
            @RequestParam(required = false, defaultValue = "STAFF_DEFAULT") String staffId,
            @RequestParam(required = false, defaultValue = "Patient copy") String purpose) {
        byte[] pdfData = medicalRecordService.downloadMedicalRecordsAsPdf(patientId, staffId, purpose);
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "medical_record_" + patientId + "_" + timestamp + ".pdf";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(pdfData.length);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfData);
    }

    @GetMapping("/{patientId}/access-logs")
    public ResponseEntity<List<AccessLogResponse>> getAccessLogs(@PathVariable Long patientId) {
        List<AccessLogResponse> logs = medicalRecordService.getAccessLogs(patientId);
        return ResponseEntity.ok(logs);
    }
}
