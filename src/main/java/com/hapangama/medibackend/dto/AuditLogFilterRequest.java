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
public class AuditLogFilterRequest {
    private Long userId;
    private String username;
    private String action;
    private String entityType;
    private String entityId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String auditHash;
    
    // Pagination
    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDirection;
}
