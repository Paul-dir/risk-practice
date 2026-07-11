package com.practice.risk.api.controller.portal;

import com.practice.risk.domain.model.RiskAssessment;
import com.practice.risk.domain.model.RiskExplanation;
import com.practice.risk.domain.service.RiskExplanationService;
import com.practice.risk.persistence.jpa.repository.RiskAssessmentRepository;
import com.practice.risk.domain.valueobject.RiskLevel;
import com.practice.risk.domain.valueobject.AssessmentStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Risk Explanation API Controller
 * 
 * Provides endpoints for retrieving transparent explanations of risk assessments.
 * CRITICAL for government transparency requirements.
 * 
 * Endpoints:
 * - GET /api/v1/risk/explain/{assessmentId} - Full explanation
 * - GET /api/v1/risk/explain/{assessmentId}/summary - Brief summary (taxpayer portal)
 * - GET /api/v1/risk/explain/{assessmentId}/detailed - Detailed report (auditors)
 */
@RestController
@RequestMapping("/api/v1/risk/explain")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Risk Explanation", description = "APIs for risk assessment transparency and explanation")
public class RiskExplanationController {
    
    private final RiskExplanationService explanationService;
    private final RiskAssessmentRepository assessmentRepository;
    
    /**
     * Get complete risk explanation
     * 
     * Returns full explanation including summary, detailed breakdown,
     * category analysis, and indicator details.
     * 
     * @param assessmentId UUID of the risk assessment
     * @return Complete risk explanation
     */
    @GetMapping("/{assessmentId}")
    @Operation(summary = "Get complete risk explanation", 
               description = "Returns full explanation of risk assessment for transparency")
    public ResponseEntity<RiskExplanation> getExplanation(@PathVariable UUID assessmentId) {
        log.info("[API] Fetching explanation for assessment: {}", assessmentId);
        
        RiskAssessment assessment = assessmentRepository.findByIdWithScores(assessmentId)
                .map(this::mapToDomain)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found: " + assessmentId));
        
        RiskExplanation explanation = explanationService.explain(assessment);
        
        return ResponseEntity.ok(explanation);
    }
    
    /**
     * Get brief summary for taxpayer portal
     * 
     * Returns citizen-friendly summary suitable for display in taxpayer self-service portal.
     * 
     * @param assessmentId UUID of the risk assessment
     * @return Brief summary text
     */
    @GetMapping("/{assessmentId}/summary")
    @Operation(summary = "Get brief summary for taxpayers",
               description = "Returns citizen-friendly summary for taxpayer portal")
    public ResponseEntity<SummaryResponse> getSummary(@PathVariable UUID assessmentId) {
        log.info("[API] Fetching summary for assessment: {}", assessmentId);
        
        RiskAssessment assessment = assessmentRepository.findByIdWithScores(assessmentId)
                .map(this::mapToDomain)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found: " + assessmentId));
        
        String summary = explanationService.generateOverallSummary(assessment);
        
        return ResponseEntity.ok(new SummaryResponse(summary));
    }
    
    /**
     * Get detailed explanation for auditors
     * 
     * Returns comprehensive technical explanation suitable for audit team review.
     * 
     * @param assessmentId UUID of the risk assessment
     * @return Detailed explanation text
     */
    @GetMapping("/{assessmentId}/detailed")
    @Operation(summary = "Get detailed explanation for auditors",
               description = "Returns comprehensive technical explanation for audit teams")
    public ResponseEntity<DetailedResponse> getDetailedExplanation(@PathVariable UUID assessmentId) {
        log.info("[API] Fetching detailed explanation for assessment: {}", assessmentId);
        
        RiskAssessment assessment = assessmentRepository.findByIdWithScores(assessmentId)
                .map(this::mapToDomain)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found: " + assessmentId));
        
        String detailed = explanationService.generateDetailedExplanation(assessment);
        
        return ResponseEntity.ok(new DetailedResponse(detailed));
    }
    
    /**
     * Map entity to domain model
     * TODO: Move to proper mapper service
     */
    private RiskAssessment mapToDomain(com.practice.risk.persistence.jpa.entity.RiskAssessmentEntity entity) {
        // Map category scores with explicit type
        java.util.List<com.practice.risk.domain.model.CategoryScore> categoryScores = 
            entity.getCategoryScores() != null 
            ? entity.getCategoryScores().stream()
                .map(cat -> com.practice.risk.domain.model.CategoryScore.builder()
                    .category(com.practice.risk.domain.valueobject.IndicatorCategory.valueOf(cat.getCategory()))
                    .score(cat.getScore())
                    .weight(cat.getWeight())
                    .contribution(cat.getContribution())
                    .build())
                .toList()
            : java.util.Collections.emptyList();
        
        // Map indicator scores with explicit type
        java.util.List<com.practice.risk.domain.model.IndicatorScore> indicatorScores = 
            entity.getIndicatorScores() != null
            ? entity.getIndicatorScores().stream()
                .map(ind -> com.practice.risk.domain.model.IndicatorScore.builder()
                    .indicatorCode(ind.getIndicatorCode())
                    .indicatorName(ind.getIndicatorName())
                    .category(com.practice.risk.domain.valueobject.IndicatorCategory.valueOf(ind.getCategory()))
                    .score(ind.getScore())
                    .weight(ind.getWeight())
                    .contribution(ind.getContribution())
                    .actualValue(ind.getActualValue())
                    .explanation(ind.getExplanation())
                    .build())
                .toList()
            : java.util.Collections.emptyList();
        
        return RiskAssessment.builder()
                .id(entity.getId())
                .taxpayerId(entity.getTaxpayerId())
                .tin(entity.getTin())
                .assessmentDate(entity.getAssessmentDate())
                .overallScore(entity.getOverallScore())
                .riskLevel(RiskLevel.valueOf(entity.getRiskLevel()))
                .priorityRank(entity.getPriorityRank())
                .status(AssessmentStatus.valueOf(entity.getStatus()))
                .configVersion(entity.getConfigVersion())
                .categoryScores(categoryScores)
                .indicatorScores(indicatorScores)
                .build();
    }
    
    // Response DTOs
    public record SummaryResponse(String summary) {}
    public record DetailedResponse(String detailedExplanation) {}
    
    // Exception
    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }
}
