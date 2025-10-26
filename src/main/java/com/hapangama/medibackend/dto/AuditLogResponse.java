package com.hapangama.medibackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {
    private Long id;
    private String auditHash;
    private String action;
    private String entityType;
    private String entityId;
    private Long userId;
    private String username;
    private String details;
    private String metadata;
    private LocalDateTime timestamp;
    private String ipAddress;
    private String correlationId;
}
