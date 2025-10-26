package com.hapangama.medibackend.service;

import com.hapangama.medibackend.model.AuditLog;
import com.hapangama.medibackend.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

/**
 * Centralized service for audit logging across all service operations
 * Uses async processing to minimize performance impact on business operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final SecureRandom secureRandom = new SecureRandom();
    
    private static final String BASE62_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    /**
     * Generate a unique 6-character hash for audit entry
     */
    private String generateAuditHash() {
        // Generate 6 random bytes
        byte[] randomBytes = new byte[6];
        secureRandom.nextBytes(randomBytes);
        
        // Convert to Base62 for URL-safe alphanumeric hash
        StringBuilder hash = new StringBuilder(6);
        for (byte b : randomBytes) {
            int index = Math.abs(b) % BASE62_CHARS.length();
            hash.append(BASE62_CHARS.charAt(index));
        }
        
        return hash.toString();
    }

    /**
     * Ensure hash uniqueness by checking database and regenerating if needed
     */
    private String generateUniqueHash() {
        String hash = generateAuditHash();
        int attempts = 0;
        
        // Unlikely collision, but check anyway
        while (attempts < 10 && auditLogRepository.existsByAuditHash(hash)) {
            hash = generateAuditHash();
            attempts++;
        }
        
        if (attempts == 10) {
            // Fallback: use timestamp-based hash with random suffix
            hash = String.format("%06d", System.currentTimeMillis() % 1000000);
        }
        
        return hash;
    }

    /**
     * Log an audit event asynchronously
     * Uses REQUIRES_NEW propagation to ensure audit is saved even if parent transaction fails
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAsync(AuditLogBuilder builder) {
        try {
            AuditLog auditLog = builder.build();
            auditLog.setAuditHash(generateUniqueHash());
            auditLog.setTimestamp(LocalDateTime.now());
            auditLogRepository.save(auditLog);
            log.debug("Audit log saved: {} - {} - {}", auditLog.getAuditHash(), auditLog.getAction(), auditLog.getEntityType());
        } catch (Exception e) {
            // Log error but don't fail the business operation
            log.error("Failed to save audit log: {}", e.getMessage(), e);
        }
    }

    /**
     * Log an audit event synchronously (for critical operations requiring immediate persistence)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuditLog logSync(AuditLogBuilder builder) {
        AuditLog auditLog = builder.build();
        auditLog.setAuditHash(generateUniqueHash());
        auditLog.setTimestamp(LocalDateTime.now());
        return auditLogRepository.save(auditLog);
    }

    /**
     * Builder class for constructing audit log entries
     */
    public static class AuditLogBuilder {
        private final AuditLog auditLog;

        public AuditLogBuilder() {
            this.auditLog = new AuditLog();
        }

        public AuditLogBuilder action(String action) {
            auditLog.setAction(action);
            return this;
        }

        public AuditLogBuilder entityType(String entityType) {
            auditLog.setEntityType(entityType);
            return this;
        }

        public AuditLogBuilder entityId(String entityId) {
            auditLog.setEntityId(entityId);
            return this;
        }

        public AuditLogBuilder userId(Long userId) {
            auditLog.setUserId(userId);
            return this;
        }

        public AuditLogBuilder username(String username) {
            auditLog.setUsername(username);
            return this;
        }

        public AuditLogBuilder patientId(Long patientId) {
            auditLog.setPatientId(patientId);
            return this;
        }

        public AuditLogBuilder details(String details) {
            auditLog.setDetails(details);
            return this;
        }

        public AuditLogBuilder metadata(String metadata) {
            auditLog.setMetadata(metadata);
            return this;
        }

        public AuditLogBuilder ipAddress(String ipAddress) {
            auditLog.setIpAddress(ipAddress);
            return this;
        }

        public AuditLogBuilder correlationId(String correlationId) {
            auditLog.setCorrelationId(correlationId);
            return this;
        }

        public AuditLog build() {
            if (auditLog.getAction() == null || auditLog.getAction().trim().isEmpty()) {
                throw new IllegalArgumentException("Action is required for audit log");
            }
            return auditLog;
        }
    }

    /**
     * Create a new builder instance
     */
    public static AuditLogBuilder builder() {
        return new AuditLogBuilder();
    }
}
