package com.practice.risk.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import com.practice.risk.domain.valueobject.AssessmentStatus;
import com.practice.risk.domain.valueobject.IndicatorCategory;
import com.practice.risk.domain.valueobject.RiskLevel;

/**
 * Risk Assessment Domain Model
 * 
 * Represents the result of a risk assessment for a taxpayer.
 * Contains ONLY risk-related information - NO audit decisions.
 * 
 * Boundary: This is PURE risk data. Consumers (Audit Module, Workflow Engine)
 * interpret this data to make their own decisions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskAssessment {
    /**
     * Unique identifier for this assessment
     */
    private UUID id;
    
    /**
     * Taxpayer being assessed
     */
    private UUID taxpayerId;
    private String tin;
    
    /**
     * When this assessment was performed
     */
    private Instant assessmentDate;
    
    /**
     * Overall risk score (0-100)
     * Higher score = higher risk
     */
    private BigDecimal overallScore;
    
    /**
     * Risk classification based on score
     */
    private RiskLevel riskLevel;
    
    /**
     * How confident we are in this assessment (0.0-1.0)
     */
    private ConfidenceFactor confidenceFactor;
    
    /**
     * Priority ranking among all assessments
     * Lower number = higher priority (1 is highest)
     */
    private Integer priorityRank;
    
    /**
     * Detailed breakdown by risk category
     */
    private List<CategoryScore> categoryScores;
    
    /**
     * Individual risk indicators that contributed to the score
     */
    private List<IndicatorScore> indicatorScores;
    
    /**
     * Current status of this assessment
     */
    private AssessmentStatus status;
    
    /**
     * Configuration version used for this assessment
     * Enables tracking when scoring rules change
     */
    private Integer configVersion;
    
    /**
     * If a human overrode the system score, explain why
     */
    private String overrideJustification;
    private UUID overriddenBy;
    private Instant overriddenAt;
    
    /**
     * Calculate the weighted contribution of a specific category
     */
    public BigDecimal getCategoryContribution(IndicatorCategory category) {
        return categoryScores.stream()
                .filter(cs -> cs.getCategory() == category)
                .findFirst()
                .map(CategoryScore::getContribution)
                .orElse(BigDecimal.ZERO);
    }
    
    /**
     * Get the top N risk indicators by score
     */
    public List<IndicatorScore> getTopIndicators(int n) {
        return indicatorScores.stream()
                .sorted((a, b) -> b.getScore().compareTo(a.getScore()))
                .limit(n)
                .toList();
    }
    
    /**
     * Check if this is a critical risk case
     */
    public boolean isCritical() {
        return riskLevel == RiskLevel.CRITICAL;
    }
    
    /**
     * Check if confidence is above threshold
     */
    public boolean isHighConfidence() {
        return confidenceFactor != null && 
               confidenceFactor.getTotal().compareTo(BigDecimal.valueOf(0.7)) >= 0;
    }
}
