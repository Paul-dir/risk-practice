package com.practice.risk.domain.event;

import com.practice.risk.domain.model.CategoryScore;
import com.practice.risk.domain.model.ConfidenceFactor;
import com.practice.risk.domain.model.IndicatorScore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Event published when a risk assessment is completed.
 * 
 * This event provides RICH DATA for consumers (Audit Module, Workflow Engine, etc.)
 * to make their own decisions. The Risk Engine does NOT prescribe what to do with this data.
 * 
 * BOUNDARY: Contains only risk assessment results, NO audit decisions or workflow routing.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskAssessmentCompletedEvent {
    
    /**
     * Unique identifier for this assessment
     */
    private UUID assessmentId;
    
    /**
     * Taxpayer being assessed
     */
    private UUID taxpayerId;
    private String tin;
    
    /**
     * When this assessment was completed
     */
    private Instant assessmentDate;
    
    /**
     * Overall risk score (0-100)
     * Higher score = higher risk
     */
    private BigDecimal overallScore;
    
    /**
     * Risk classification (LOW, MEDIUM, HIGH, CRITICAL)
     */
    private String riskLevel;
    
    /**
     * Priority ranking among all assessments
     * Lower number = higher priority (1 is highest)
     * 
     * NOTE: This is a SUGGESTION only - consumers may re-prioritize based on
     * their own business rules (e.g., VIP taxpayers, strategic industries)
     */
    private Integer priorityRank;
    
    /**
     * How confident we are in this assessment (0.0-1.0)
     * 
     * Consumers should use this to decide if manual review is needed.
     * Low confidence + high risk = strong candidate for human review
     */
    private ConfidenceFactor confidenceFactor;
    
    /**
     * Detailed breakdown by risk category
     * 
     * RICH DATA: Consumers can use these to make intelligent decisions.
     * Example: If FILING category is high but PAYMENT is low, maybe it's
     * just administrative delay, not intentional non-compliance.
     */
    private List<CategoryScore> categoryScores;
    
    /**
     * Individual risk indicators that contributed to the score
     * 
     * RICH DATA: Consumers can examine specific indicators to decide:
     * - Audit type (e.g., if RELATED_PARTY_TRANSACTIONS is high → Transfer Pricing audit)
     * - Investigation depth (e.g., if PREVIOUS_FRAUD is present → comprehensive audit)
     * - Resource allocation (e.g., if complex indicators → assign senior auditor)
     */
    private List<IndicatorScore> indicatorScores;
    
    /**
     * Configuration version used for this assessment
     * Enables tracking when scoring rules change
     */
    private Integer configVersion;
    
    // ============================================================================
    // WHAT THIS EVENT DOES NOT CONTAIN (By Design)
    // ============================================================================
    
    // ❌ NO recommendedAuditType - That's the Audit Module's decision
    //    The Audit Module should examine categoryScores and indicatorScores
    //    to make its own intelligent decision based on business rules.
    
    // ❌ NO assignedAuditorId - That's the Audit Module's responsibility
    
    // ❌ NO workflowState - That's the Workflow Engine's responsibility
    
    // ❌ NO caatEligibility - That's the Rule Engine's responsibility
    
    // ============================================================================
    // HOW CONSUMERS SHOULD USE THIS EVENT
    // ============================================================================
    
    /**
     * AUDIT MODULE Example:
     * 
     * <pre>
     * {@code
     * @EventListener
     * public void onRiskAssessment(RiskAssessmentCompletedEvent event) {
     *     // Make YOUR OWN decision based on rich data
     *     AuditType type = determineAuditType(
     *         event.getOverallScore(),
     *         event.getCategoryScores(),
     *         event.getIndicatorScores()
     *     );
     *     
     *     // Check specific indicators for special handling
     *     boolean hasTransferPricingRisk = event.getIndicatorScores().stream()
     *         .anyMatch(i -> "RELATED_PARTY_TRANSACTIONS".equals(i.getIndicatorCode()) 
     *                     && i.getScore().compareTo(BigDecimal.valueOf(50)) > 0);
     *     
     *     if (hasTransferPricingRisk) {
     *         createSpecializedAudit(event.getTaxpayerId(), AuditType.TRANSFER_PRICING);
     *     }
     * }
     * }
     * </pre>
     * 
     * WORKFLOW ENGINE Example:
     * 
     * <pre>
     * {@code
     * @EventListener
     * public void onRiskAssessment(RiskAssessmentCompletedEvent event) {
     *     // Route based on risk level AND confidence
     *     if (event.getRiskLevel().equals("CRITICAL") && 
     *         event.getConfidenceFactor().getTotal().compareTo(BigDecimal.valueOf(0.7)) >= 0) {
     *         escalateToSeniorManagement(event);
     *     } else if (event.getRiskLevel().equals("HIGH") &&
     *                event.getConfidenceFactor().getTotal().compareTo(BigDecimal.valueOf(0.5)) < 0) {
     *         routeForManualReview(event);  // High risk but low confidence
     *     }
     * }
     * }
     * </pre>
     */
}
