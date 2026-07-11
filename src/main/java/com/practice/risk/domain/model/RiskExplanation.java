package com.practice.risk.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Risk Explanation Domain Model
 * 
 * Provides transparent, human-readable explanations for risk assessments.
 * CRITICAL for government systems - every decision must be explainable and defensible.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskExplanation {
    
    /**
     * High-level summary suitable for taxpayer portal
     * Example: "Your risk score is 67 (MEDIUM) based on analysis of 12 risk indicators."
     */
    private String overallSummary;
    
    /**
     * Detailed explanation for auditors
     * Includes methodology, data sources, and calculation logic
     */
    private String detailedExplanation;
    
    /**
     * Top 3 factors contributing to the risk score
     * Example: ["Late filing (3 periods)", "Continuous losses (4 years)", "High related-party transactions (45%)"]
     */
    private List<String> topContributingFactors;
    
    /**
     * Breakdown by category with explanations
     * Key: Category name (e.g., "Filing Compliance")
     * Value: Human-readable explanation
     */
    private Map<String, CategoryExplanation> categoryBreakdown;
    
    /**
     * Individual indicator explanations
     */
    private List<IndicatorExplanation> indicatorExplanations;
    
    /**
     * Data sources used in this assessment
     */
    private List<String> dataSources;
    
    /**
     * Confidence explanation
     * Why we are X% confident in this assessment
     */
    private String confidenceRationale;
    
    /**
     * Comparison with industry peers
     */
    private String industryComparison;
    
    /**
     * Historical context (if available)
     */
    private String historicalContext;
    
    /**
     * Explanation of category contribution
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryExplanation {
        private String categoryName;
        private Double score;
        private Double weight;
        private Double contribution;
        private String explanation;
        private List<String> keyFindings;
    }
    
    /**
     * Explanation of individual indicator
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IndicatorExplanation {
        private String indicatorCode;
        private String indicatorName;
        private Double score;
        private String actualValue;
        private String threshold;
        private String explanation;
        private String impact;  // "HIGH", "MEDIUM", "LOW"
    }
}
