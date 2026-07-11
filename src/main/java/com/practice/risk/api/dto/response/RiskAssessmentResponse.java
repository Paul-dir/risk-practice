package com.practice.risk.api.dto.response;

import com.practice.risk.domain.model.CategoryScore;
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
 * Risk Assessment Response DTO
 * 
 * API response containing ONLY risk assessment data.
 * NO audit decisions (no recommendedAuditType).
 * 
 * Consumers should use categoryScores and indicatorScores to make their own decisions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskAssessmentResponse {
    private UUID assessmentId;
    private UUID taxpayerId;
    private String tin;
    private BigDecimal overallScore;
    private String riskLevel;
    private BigDecimal confidenceFactor;
    private Integer priorityRank;
    private List<CategoryScore> categoryScores;
    private List<IndicatorScore> indicatorScores;
    private Instant assessmentDate;
    
    // ❌ REMOVED: recommendedAuditType
    // Consumers should examine categoryScores and indicatorScores to decide audit type
}
