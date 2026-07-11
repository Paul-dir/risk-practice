package com.practice.risk.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Event published when a critical risk case is identified.
 * 
 * This event triggers immediate alert/escalation workflows.
 * 
 * Consumers:
 * - Workflow Engine: Routes for senior management review
 * - Notification Service: Sends alerts to relevant parties
 * - Audit Module: Flags for priority investigation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CriticalRiskAlertEvent {
    
    /**
     * Assessment that triggered this alert
     */
    private UUID assessmentId;
    
    /**
     * Taxpayer identified as critical risk
     */
    private UUID taxpayerId;
    private String tin;
    
    /**
     * When this alert was generated
     */
    private Instant alertDate;
    
    /**
     * Risk score that triggered the alert
     */
    private BigDecimal riskScore;
    
    /**
     * Risk level (should be CRITICAL)
     */
    private String riskLevel;
    
    /**
     * Specific indicators that contributed to critical status
     * Helps consumers understand WHY this is critical
     */
    private List<String> criticalIndicators;
    
    /**
     * Alert priority (1 = highest)
     * Based on risk score magnitude
     */
    private Integer alertPriority;
    
    /**
     * Recommended response time (in hours)
     * Suggestion only - consumers decide actual SLA
     */
    private Integer suggestedResponseHours;
}
