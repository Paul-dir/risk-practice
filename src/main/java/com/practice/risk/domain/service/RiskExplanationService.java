package com.practice.risk.domain.service;

import com.practice.risk.domain.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Risk Explanation Service
 * 
 * Generates transparent, human-readable explanations for risk assessments.
 * This is CRITICAL for government systems where every decision must be defensible.
 * 
 * Responsibilities:
 * 1. Generate overall summary (for taxpayer portal)
 * 2. Generate detailed explanation (for auditors)
 * 3. Identify top contributing factors
 * 4. Explain category breakdowns
 * 5. Provide indicator-level details
 * 6. Explain confidence levels
 */
@Service
@Slf4j
public class RiskExplanationService {
    
    /**
     * Generate complete risk explanation
     */
    public RiskExplanation explain(RiskAssessment assessment) {
        log.debug("Generating explanation for assessment: {}", assessment.getId());
        
        return RiskExplanation.builder()
                .overallSummary(generateOverallSummary(assessment))
                .detailedExplanation(generateDetailedExplanation(assessment))
                .topContributingFactors(identifyTopFactors(assessment))
                .categoryBreakdown(explainCategories(assessment))
                .indicatorExplanations(explainIndicators(assessment))
                .dataSources(listDataSources())
                .confidenceRationale(explainConfidence(assessment))
                .industryComparison(generateIndustryComparison(assessment))
                .historicalContext(generateHistoricalContext(assessment))
                .build();
    }
    
    /**
     * Generate brief summary for taxpayer portal
     */
    public String generateOverallSummary(RiskAssessment assessment) {
        return String.format(
                "Your risk assessment score is %.2f (%s risk level) based on analysis of %d risk indicators across %d categories. " +
                "This score is calculated using objective, data-driven criteria established by the tax authority.",
                assessment.getOverallScore(),
                assessment.getRiskLevel(),
                assessment.getIndicatorScores().size(),
                assessment.getCategoryScores().size()
        );
    }
    
    /**
     * Generate detailed explanation for auditors
     */
    public String generateDetailedExplanation(RiskAssessment assessment) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("=== RISK ASSESSMENT DETAILED REPORT ===\n\n");
        
        // Assessment metadata
        sb.append("Assessment ID: ").append(assessment.getId()).append("\n");
        sb.append("Taxpayer ID: ").append(assessment.getTaxpayerId()).append("\n");
        sb.append("TIN: ").append(assessment.getTin()).append("\n");
        sb.append("Assessment Date: ").append(assessment.getAssessmentDate()).append("\n");
        sb.append("Configuration Version: ").append(assessment.getConfigVersion()).append("\n\n");
        
        // Overall scoring
        sb.append("OVERALL RISK SCORE: ").append(assessment.getOverallScore()).append("/100\n");
        sb.append("RISK LEVEL: ").append(assessment.getRiskLevel()).append("\n");
        sb.append("CONFIDENCE: ").append(assessment.getConfidenceFactor().getTotal()).append("\n\n");
        
        // Scoring methodology
        sb.append("SCORING METHODOLOGY:\n");
        sb.append("This assessment uses a weighted scoring model that evaluates multiple risk categories.\n");
        sb.append("Each category contains specific indicators that are scored based on configured thresholds.\n");
        sb.append("Scores are aggregated using predefined weights to produce the overall risk score.\n\n");
        
        // Category breakdown
        sb.append("CATEGORY SCORES:\n");
        for (CategoryScore cat : assessment.getCategoryScores()) {
            sb.append(String.format("  %s: %.2f/100 (weight: %.0f%%) → contribution: %.2f\n",
                    cat.getCategory(),
                    cat.getScore(),
                    cat.getWeight().multiply(BigDecimal.valueOf(100)),
                    cat.getContribution()
            ));
        }
        sb.append("\n");
        
        // Top indicators
        sb.append("TOP RISK INDICATORS:\n");
        assessment.getTopIndicators(5).forEach(ind -> {
            sb.append(String.format("  • %s: %.2f (actual: %s)\n",
                    ind.getIndicatorName(),
                    ind.getScore(),
                    ind.getActualValue()
            ));
            sb.append(String.format("    Explanation: %s\n", ind.getExplanation()));
        });
        
        return sb.toString();
    }
    
    /**
     * Identify top 3-5 contributing factors
     */
    private List<String> identifyTopFactors(RiskAssessment assessment) {
        return assessment.getTopIndicators(5).stream()
                .filter(ind -> ind.getScore().compareTo(BigDecimal.TEN) > 0)  // Only significant contributors
                .map(ind -> String.format("%s (score: %.1f) - %s",
                        ind.getIndicatorName(),
                        ind.getScore(),
                        ind.getExplanation()))
                .collect(Collectors.toList());
    }
    
    /**
     * Explain each category's contribution
     */
    private Map<String, RiskExplanation.CategoryExplanation> explainCategories(RiskAssessment assessment) {
        Map<String, RiskExplanation.CategoryExplanation> breakdown = new HashMap<>();
        
        for (CategoryScore cat : assessment.getCategoryScores()) {
            // Get indicators for this category
            List<String> keyFindings = assessment.getIndicatorScores().stream()
                    .filter(ind -> ind.getCategory() == cat.getCategory())
                    .filter(ind -> ind.getScore().compareTo(BigDecimal.ZERO) > 0)
                    .map(ind -> String.format("%s: %.1f", ind.getIndicatorName(), ind.getScore()))
                    .collect(Collectors.toList());
            
            String explanation = generateCategoryExplanation(cat, keyFindings);
            
            breakdown.put(cat.getCategory().name(), RiskExplanation.CategoryExplanation.builder()
                    .categoryName(cat.getCategory().getDisplayName())
                    .score(cat.getScore().doubleValue())
                    .weight(cat.getWeight().doubleValue())
                    .contribution(cat.getContribution().doubleValue())
                    .explanation(explanation)
                    .keyFindings(keyFindings)
                    .build());
        }
        
        return breakdown;
    }
    
    /**
     * Generate category-specific explanation
     */
    private String generateCategoryExplanation(CategoryScore cat, List<String> keyFindings) {
        String categoryName = cat.getCategory().getDisplayName();
        double score = cat.getScore().doubleValue();
        
        if (score > 70) {
            return String.format("%s shows HIGH risk with a score of %.1f. " +
                    "Multiple indicators in this category exceeded normal thresholds.", 
                    categoryName, score);
        } else if (score > 40) {
            return String.format("%s shows MODERATE risk with a score of %.1f. " +
                    "Some indicators in this category require attention.", 
                    categoryName, score);
        } else {
            return String.format("%s shows LOW risk with a score of %.1f. " +
                    "Indicators in this category are within acceptable ranges.", 
                    categoryName, score);
        }
    }
    
    /**
     * Explain individual indicators
     */
    private List<RiskExplanation.IndicatorExplanation> explainIndicators(RiskAssessment assessment) {
        return assessment.getIndicatorScores().stream()
                .map(ind -> RiskExplanation.IndicatorExplanation.builder()
                        .indicatorCode(ind.getIndicatorCode())
                        .indicatorName(ind.getIndicatorName())
                        .score(ind.getScore().doubleValue())
                        .actualValue(ind.getActualValue())
                        .threshold(determineThreshold(ind))
                        .explanation(ind.getExplanation())
                        .impact(determineImpact(ind.getScore().doubleValue()))
                        .build())
                .collect(Collectors.toList());
    }
    
    /**
     * Determine threshold description for indicator
     */
    private String determineThreshold(IndicatorScore indicator) {
        // TODO: Get actual threshold from configuration
        return "Standard threshold applied based on tax authority regulations";
    }
    
    /**
     * Determine impact level
     */
    private String determineImpact(double score) {
        if (score > 40) return "HIGH";
        if (score > 15) return "MEDIUM";
        return "LOW";
    }
    
    /**
     * List data sources used
     */
    private List<String> listDataSources() {
        return List.of(
                "Taxpayer Registration Database",
                "Tax Return Filing System",
                "Payment Processing System",
                "E-Invoicing Platform",
                "Customs Import/Export Database",
                "Industry Benchmark Database",
                "Previous Audit Records"
        );
    }
    
    /**
     * Explain confidence level
     */
    private String explainConfidence(RiskAssessment assessment) {
        ConfidenceFactor conf = assessment.getConfidenceFactor();
        if (conf == null) {
            return "Confidence data not available for this assessment.";
        }
        
        double total = conf.getTotal().doubleValue();
        
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Confidence Level: %.1f%%\n\n", total * 100));
        sb.append("This confidence score reflects:\n");
        sb.append(String.format("• Data Availability (%.1f%%): Completeness of required data fields\n", 
                conf.getDataAvailability().doubleValue() * 100));
        sb.append(String.format("• Data Quality (%.1f%%): Accuracy and consistency of data\n", 
                conf.getDataQuality().doubleValue() * 100));
        sb.append(String.format("• Source Diversity (%.1f%%): Number of independent data sources\n", 
                conf.getSourceCount().doubleValue() * 100));
        sb.append(String.format("• Temporal Consistency (%.1f%%): Historical data availability\n", 
                conf.getTemporalConsistency().doubleValue() * 100));
        
        if (total > 0.7) {
            sb.append("\nHigh confidence - Assessment is based on comprehensive, quality data.");
        } else if (total > 0.5) {
            sb.append("\nModerate confidence - Some data limitations exist but core indicators are reliable.");
        } else {
            sb.append("\nLow confidence - Significant data gaps exist. Assessment should be reviewed carefully.");
        }
        
        return sb.toString();
    }
    
    /**
     * Generate industry comparison context
     */
    private String generateIndustryComparison(RiskAssessment assessment) {
        // TODO: Implement actual industry comparison when benchmark data is available
        return "Industry comparison: This taxpayer's risk profile is being evaluated against sector-specific benchmarks. " +
                "Detailed industry comparison will be available once benchmark analysis is complete.";
    }
    
    /**
     * Generate historical context
     */
    private String generateHistoricalContext(RiskAssessment assessment) {
        // TODO: Implement actual historical comparison when profile history is available
        return "Historical context: This is a point-in-time assessment. " +
                "Historical trend analysis will be available after multiple assessments have been performed.";
    }
}
