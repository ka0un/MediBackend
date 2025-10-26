package com.hapangama.medibackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_hash", columnList = "auditHash"),
    @Index(name = "idx_audit_user", columnList = "userId"),
    @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
    @Index(name = "idx_audit_action", columnList = "action"),
    @Index(name = "idx_audit_entity", columnList = "entityType,entityId")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 6)
    private String auditHash;

    @Column(nullable = false)
    private String action;

    // Entity information
    private String entityType;
    
    private String entityId;

    // User/Actor information
    private Long userId;
    
    private String username;

    // Legacy field for backward compatibility
    private Long patientId;

    @Column(length = 5000)
    private String details;

    @Column(length = 2000)
    private String metadata;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    private String ipAddress;

    private String correlationId;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}
