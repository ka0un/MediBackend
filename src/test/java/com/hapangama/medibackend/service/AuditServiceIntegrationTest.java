package com.hapangama.medibackend.service;

import com.hapangama.medibackend.model.AuditLog;
import com.hapangama.medibackend.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for the AuditService
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuditServiceIntegrationTest {

    @Autowired
    private AuditService auditService;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @BeforeEach
    void setUp() {
        auditLogRepository.deleteAll();
    }

    @Test
    void testLogSync_SavesAuditLogWithHash() {
        // Given
        AuditService.AuditLogBuilder builder = AuditService.builder()
            .action("TEST_ACTION")
            .entityType("TestEntity")
            .entityId("123")
            .userId(1L)
            .username("testuser")
            .details("Test audit log entry")
            .metadata("{\"test\":\"data\"}")
            .ipAddress("127.0.0.1");

        // When
        AuditLog savedLog = auditService.logSync(builder);

        // Then
        assertNotNull(savedLog);
        assertNotNull(savedLog.getId());
        assertNotNull(savedLog.getAuditHash());
        assertEquals(6, savedLog.getAuditHash().length());
        assertEquals("TEST_ACTION", savedLog.getAction());
        assertEquals("TestEntity", savedLog.getEntityType());
        assertEquals("123", savedLog.getEntityId());
        assertEquals(1L, savedLog.getUserId());
        assertEquals("testuser", savedLog.getUsername());
        assertEquals("Test audit log entry", savedLog.getDetails());
        assertEquals("{\"test\":\"data\"}", savedLog.getMetadata());
        assertEquals("127.0.0.1", savedLog.getIpAddress());
        assertNotNull(savedLog.getTimestamp());
    }

    @Test
    void testLogSync_GeneratesUniqueHashes() {
        // Given
        AuditService.AuditLogBuilder builder1 = AuditService.builder()
            .action("TEST_ACTION_1")
            .entityType("TestEntity")
            .details("First entry");

        AuditService.AuditLogBuilder builder2 = AuditService.builder()
            .action("TEST_ACTION_2")
            .entityType("TestEntity")
            .details("Second entry");

        // When
        AuditLog log1 = auditService.logSync(builder1);
        AuditLog log2 = auditService.logSync(builder2);

        // Then
        assertNotNull(log1.getAuditHash());
        assertNotNull(log2.getAuditHash());
        assertNotEquals(log1.getAuditHash(), log2.getAuditHash());
    }

    @Test
    void testFindByAuditHash() {
        // Given
        AuditService.AuditLogBuilder builder = AuditService.builder()
            .action("FINDABLE_ACTION")
            .entityType("TestEntity")
            .details("Test finding by hash");

        AuditLog savedLog = auditService.logSync(builder);
        String hash = savedLog.getAuditHash();

        // When
        AuditLog foundLog = auditLogRepository.findByAuditHash(hash).orElse(null);

        // Then
        assertNotNull(foundLog);
        assertEquals(savedLog.getId(), foundLog.getId());
        assertEquals(hash, foundLog.getAuditHash());
        assertEquals("FINDABLE_ACTION", foundLog.getAction());
    }

    @Test
    void testExistsByAuditHash() {
        // Given
        AuditService.AuditLogBuilder builder = AuditService.builder()
            .action("EXISTS_TEST")
            .entityType("TestEntity")
            .details("Test hash existence check");

        AuditLog savedLog = auditService.logSync(builder);
        String hash = savedLog.getAuditHash();

        // When/Then
        assertTrue(auditLogRepository.existsByAuditHash(hash));
        assertFalse(auditLogRepository.existsByAuditHash("XXXXXX"));
    }

    @Test
    void testBuilder_RequiresAction() {
        // Given
        AuditService.AuditLogBuilder builder = AuditService.builder()
            .entityType("TestEntity")
            .details("Missing action");

        // When/Then
        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    void testLogSync_WithBackwardCompatibilityFields() {
        // Given - test backward compatibility with patientId
        AuditService.AuditLogBuilder builder = AuditService.builder()
            .action("PATIENT_TEST")
            .entityType("Patient")
            .entityId("456")
            .patientId(456L)
            .userId(1L)
            .username("testuser")
            .details("Testing backward compatibility");

        // When
        AuditLog savedLog = auditService.logSync(builder);

        // Then
        assertNotNull(savedLog);
        assertEquals(456L, savedLog.getPatientId());
        assertEquals("456", savedLog.getEntityId());
        assertEquals("Patient", savedLog.getEntityType());
    }

    @Test
    void testLogSync_WithCorrelationId() {
        // Given
        String correlationId = "req-123-456-789";
        AuditService.AuditLogBuilder builder = AuditService.builder()
            .action("CORRELATED_ACTION")
            .entityType("TestEntity")
            .correlationId(correlationId)
            .details("Test with correlation ID");

        // When
        AuditLog savedLog = auditService.logSync(builder);

        // Then
        assertNotNull(savedLog);
        assertEquals(correlationId, savedLog.getCorrelationId());
    }
}
