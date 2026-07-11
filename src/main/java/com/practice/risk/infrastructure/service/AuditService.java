package com.practice.risk.infrastructure.service;

import com.practice.risk.persistence.jpa.entity.RiskAuditLogEntity;
import com.practice.risk.persistence.jpa.repository.RiskAuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Audit logging service
 * 
 * Records all important operations for compliance and traceability.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuditService {
    
    private final RiskAuditLogRepository repository;
    
    /**
     * Log an audit event
     * 
     * @param entityType Type of entity (e.g., "RISK_ASSESSMENT")
     * @param entityId Entity ID
     * @param action Action performed (e.g., "CREATE", "UPDATE")
     * @param performedBy User who performed the action
     * @param oldValue Old value (for updates)
     * @param newValue New value
     * @param justification Justification for the action
     */
    public void log(String entityType, UUID entityId, String action, String performedBy,
                    Object oldValue, Object newValue, String justification) {
        try {
            RiskAuditLogEntity auditLog = RiskAuditLogEntity.builder()
                    .id(UUID.randomUUID())
                    .entityType(entityType)
                    .entityId(entityId)
                    .action(action)
                    .actor(performedBy)
                    .reason(justification)
                    .build();
            
            repository.save(auditLog);
            
            log.info("[AUDIT] {} {} on {}: {}", action, entityType, entityId, justification);
        } catch (Exception e) {
            log.error("[AUDIT ERROR] Failed to log audit event", e);
            // Don't throw - audit logging should not break main flow
        }
    }
}
