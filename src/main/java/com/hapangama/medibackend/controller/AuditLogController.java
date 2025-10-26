package com.hapangama.medibackend.controller;

import com.hapangama.medibackend.dto.AuditLogFilterRequest;
import com.hapangama.medibackend.dto.AuditLogResponse;
import com.hapangama.medibackend.model.AuditLog;
import com.hapangama.medibackend.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    /**
     * Query audit logs with filters and pagination
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAuditLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String entityId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        // Parse dates if provided
        java.time.LocalDateTime startDateTime = startDate != null ? 
            java.time.LocalDateTime.parse(startDate) : null;
        java.time.LocalDateTime endDateTime = endDate != null ? 
            java.time.LocalDateTime.parse(endDate) : null;

        // Create sort
        Sort sort = sortDirection.equalsIgnoreCase("ASC") ? 
            Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);

        // Query with filters
        Page<AuditLog> auditPage = auditLogRepository.findByFilters(
            userId, username, action, entityType, entityId,
            startDateTime, endDateTime, pageable
        );

        // Map to response
        Page<AuditLogResponse> responsePage = auditPage.map(this::mapToResponse);

        Map<String, Object> response = new HashMap<>();
        response.put("content", responsePage.getContent());
        response.put("currentPage", responsePage.getNumber());
        response.put("totalItems", responsePage.getTotalElements());
        response.put("totalPages", responsePage.getTotalPages());
        response.put("pageSize", responsePage.getSize());
        response.put("hasNext", responsePage.hasNext());
        response.put("hasPrevious", responsePage.hasPrevious());

        return ResponseEntity.ok(response);
    }

    /**
     * Get audit log by hash ID
     */
    @GetMapping("/{auditHash}")
    public ResponseEntity<AuditLogResponse> getAuditLogByHash(@PathVariable String auditHash) {
        return auditLogRepository.findByAuditHash(auditHash)
            .map(this::mapToResponse)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get audit logs for a specific entity
     */
    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<Map<String, Object>> getAuditLogsByEntity(
            @PathVariable String entityType,
            @PathVariable String entityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<AuditLog> auditPage = auditLogRepository
            .findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId, pageable);

        Page<AuditLogResponse> responsePage = auditPage.map(this::mapToResponse);

        Map<String, Object> response = new HashMap<>();
        response.put("content", responsePage.getContent());
        response.put("currentPage", responsePage.getNumber());
        response.put("totalItems", responsePage.getTotalElements());
        response.put("totalPages", responsePage.getTotalPages());

        return ResponseEntity.ok(response);
    }

    /**
     * Get audit logs for a specific user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getAuditLogsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<AuditLog> auditPage = auditLogRepository.findByUserIdOrderByTimestampDesc(userId, pageable);

        Page<AuditLogResponse> responsePage = auditPage.map(this::mapToResponse);

        Map<String, Object> response = new HashMap<>();
        response.put("content", responsePage.getContent());
        response.put("currentPage", responsePage.getNumber());
        response.put("totalItems", responsePage.getTotalElements());
        response.put("totalPages", responsePage.getTotalPages());

        return ResponseEntity.ok(response);
    }

    private AuditLogResponse mapToResponse(AuditLog auditLog) {
        return AuditLogResponse.builder()
            .id(auditLog.getId())
            .auditHash(auditLog.getAuditHash())
            .action(auditLog.getAction())
            .entityType(auditLog.getEntityType())
            .entityId(auditLog.getEntityId())
            .userId(auditLog.getUserId())
            .username(auditLog.getUsername())
            .details(auditLog.getDetails())
            .metadata(auditLog.getMetadata())
            .timestamp(auditLog.getTimestamp())
            .ipAddress(auditLog.getIpAddress())
            .correlationId(auditLog.getCorrelationId())
            .build();
    }
}
