package com.practice.risk.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Event published when a taxpayer's risk profile is updated.
 * 
 * This event tracks risk trend changes over time.
 * 
 * Consumers:
 * - Analytics Service: Track risk trends
 * - Dashboard Service: Update real-time displays
 * - Reporting Service: Generate trend reports
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskProfileUpdatedEvent {
    
    /**
     * Taxpayer whose profile was updated
     */
    private UUID taxpayerId;
    private String tin;
    
    /**
     * When this update occurred
     */
    private Instant updateDate;
    
    /**
     * Previous risk score
     */
    private BigDecimal previousScore;
    
    /**
     * Current risk score
     */
    private BigDecimal currentScore;
    
    /**
     * Score change (positive = deteriorating, negative = improving)
     */
    private BigDecimal scoreChange;
    
    /**
     * Previous risk level
     */
    private String previousLevel;
    
    /**
     * Current risk level
     */
    private String currentLevel;
    
    /**
     * Risk trend: IMPROVING, STABLE, DETERIORATING
     */
    private String trend;
    
    /**
     * Number of consecutive high-risk assessments
     * Useful for escalation rules
     */
    private Integer consecutiveHighRiskCount;
}
