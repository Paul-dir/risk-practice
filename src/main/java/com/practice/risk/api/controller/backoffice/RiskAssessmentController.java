package com.practice.risk.api.controller.backoffice;

import com.practice.risk.api.dto.request.RiskAssessmentRequest;
import com.practice.risk.api.dto.response.RiskAssessmentResponse;
import com.practice.risk.application.service.RiskAssessmentOrchestrator;
import com.practice.risk.domain.model.RiskAssessment;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/risk")
@RequiredArgsConstructor
public class RiskAssessmentController {

    private final RiskAssessmentOrchestrator orchestrator;

    @PostMapping("/assess")
    public ResponseEntity<RiskAssessmentResponse> assess(@Valid @RequestBody RiskAssessmentRequest request) {
        RiskAssessment result = orchestrator.assessTaxpayer(request.getTaxpayerId(), request.getTin());
        return ResponseEntity.ok(mapToResponse(result));
    }

    private RiskAssessmentResponse mapToResponse(RiskAssessment domain) {
        return RiskAssessmentResponse.builder()
                .assessmentId(domain.getId())
                .taxpayerId(domain.getTaxpayerId())
                .tin(domain.getTin())
                .overallScore(domain.getOverallScore())
                .riskLevel(domain.getRiskLevel().name())
                .confidenceFactor(domain.getConfidenceFactor() != null ? domain.getConfidenceFactor().getTotal() : null)
                // ❌ REMOVED: recommendedAuditType - Not Risk Engine's responsibility
                .priorityRank(domain.getPriorityRank())
                .categoryScores(domain.getCategoryScores())
                .indicatorScores(domain.getIndicatorScores())
                .assessmentDate(domain.getAssessmentDate())
                .build();
    }
}
