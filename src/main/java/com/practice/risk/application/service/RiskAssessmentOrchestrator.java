package com.practice.risk.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.risk.application.port.outbound.*;
import com.practice.risk.domain.event.RiskAssessmentCompletedEvent;
import com.practice.risk.domain.model.RiskAssessment;
import com.practice.risk.domain.service.RiskScoringService;
import com.practice.risk.domain.service.TaxpayerData;
import com.practice.risk.infrastructure.config.IndicatorConfigurationService;
import com.practice.risk.infrastructure.messaging.RiskEventProducer;
import com.practice.risk.persistence.jpa.entity.RiskAssessmentEntity;
import com.practice.risk.persistence.jpa.entity.TaxpayerRiskProfileEntity;
import com.practice.risk.persistence.jpa.repository.RiskAssessmentRepository;
import com.practice.risk.persistence.jpa.repository.TaxpayerRiskProfileRepository;
import com.practice.risk.infrastructure.service.AuditService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class RiskAssessmentOrchestrator {

    private final RiskScoringService scoringService;
    private final RiskAssessmentRepository assessmentRepository;
    private final TaxpayerRiskProfileRepository profileRepository;

    // Ports (data collection)
    private final RegistrationPort registrationPort;
    private final TaxReturnPort taxReturnPort;
    private final PaymentPort paymentPort;
    private final IntegrationPort integrationPort;
    private final IndustryBenchmarkPort benchmarkPort;

    // Cross-cutting
    private final IndicatorConfigurationService configService;
    private final Optional<RiskEventProducer> eventProducer;  // Optional - may not be available if Kafka disabled
    private final AuditService auditService;
    private final ObjectMapper objectMapper;
    
    public RiskAssessmentOrchestrator(
            RiskScoringService scoringService,
            RiskAssessmentRepository assessmentRepository,
            TaxpayerRiskProfileRepository profileRepository,
            RegistrationPort registrationPort,
            TaxReturnPort taxReturnPort,
            PaymentPort paymentPort,
            IntegrationPort integrationPort,
            IndustryBenchmarkPort benchmarkPort,
            IndicatorConfigurationService configService,
            @Autowired(required = false) RiskEventProducer eventProducer,
            AuditService auditService,
            ObjectMapper objectMapper) {
        this.scoringService = scoringService;
        this.assessmentRepository = assessmentRepository;
        this.profileRepository = profileRepository;
        this.registrationPort = registrationPort;
        this.taxReturnPort = taxReturnPort;
        this.paymentPort = paymentPort;
        this.integrationPort = integrationPort;
        this.benchmarkPort = benchmarkPort;
        this.configService = configService;
        this.eventProducer = Optional.ofNullable(eventProducer);
        this.auditService = auditService;
        this.objectMapper = objectMapper;
        
        if (this.eventProducer.isPresent()) {
            log.info("[ORCHESTRATOR] Kafka event publishing is ENABLED");
        } else {
            log.warn("[ORCHESTRATOR] Kafka event publishing is DISABLED - events will be logged only");
        }
    }

    /**
     * Assess taxpayer risk - PURE Risk Engine responsibility
     * 
     * This method ONLY:
     * 1. Collects taxpayer data
     * 2. Calculates risk score
     * 3. Persists assessment
     * 4. Updates risk profile
     * 5. Publishes event for consumers
     * 
     * It does NOT:
     * - Assign audit cases
     * - Create audit workflows
     * - Make routing decisions
     * - Execute business rules
     */
    @Transactional
    public RiskAssessment assessTaxpayer(UUID taxpayerId, String tin) {
        log.info("[RISK ENGINE] Assessing taxpayer: {} ({})", taxpayerId, tin);

        // 1. DATA COLLECTION - gather from all sources
        TaxpayerData.RegistrationData reg = registrationPort.getRegistration(taxpayerId);
        TaxpayerData.ReturnData ret = taxReturnPort.getReturns(taxpayerId, java.time.Year.now().getValue() - 1);
        TaxpayerData.PaymentData pay = paymentPort.getPaymentHistory(taxpayerId);
        TaxpayerData.ExternalData ext = integrationPort.getExternalData(taxpayerId);
        TaxpayerData.BenchmarkData bench = benchmarkPort.getBenchmarks(reg.getIndustryCode());

        TaxpayerData data = TaxpayerData.fromParts(reg, ret, pay, ext, bench);

        // 2. RISK SCORING - pure calculation
        RiskAssessment assessment = scoringService.assess(taxpayerId, tin, data, configService);

        // 3. PERSISTENCE - save assessment
        RiskAssessmentEntity entity = mapToEntity(assessment);
        RiskAssessmentEntity saved = assessmentRepository.save(entity);
        assessment.setId(saved.getId());

        // 4. PROFILE UPDATE - maintain historical data
        updateProfile(taxpayerId, tin, assessment);

        // 5. AUDIT LOGGING - traceability (cross-cutting concern)
        auditService.log(
                "RISK_ASSESSMENT",
                assessment.getId(),
                "CREATE",
                "SYSTEM",
                null,
                assessment,
                "Automated risk assessment completed"
        );

        // 6. EVENT PUBLISHING - notify consumers (Audit Module, Workflow Engine, etc.)
        //    IMPORTANT: We provide RICH DATA, let consumers decide what to do
        RiskAssessmentCompletedEvent event = RiskAssessmentCompletedEvent.builder()
                .assessmentId(assessment.getId())
                .taxpayerId(assessment.getTaxpayerId())
                .tin(assessment.getTin())
                .overallScore(assessment.getOverallScore())
                .riskLevel(assessment.getRiskLevel().name())
                // ✅ Provide detailed scores - let consumer interpret
                .categoryScores(assessment.getCategoryScores())
                .indicatorScores(assessment.getIndicatorScores())
                .confidenceFactor(assessment.getConfidenceFactor())
                .assessmentDate(assessment.getAssessmentDate())
                .priorityRank(assessment.getPriorityRank())
                // ❌ REMOVED: recommendedAuditType - that's Audit Module's decision
                .build();
        
        if (eventProducer.isPresent()) {
            eventProducer.get().publishAssessmentCompleted(event);
        } else {
            log.info("[EVENT] Would publish RiskAssessmentCompletedEvent (Kafka disabled): {}", event);
        }

        log.info("[RISK ENGINE] Assessment completed - Score: {}, Level: {}", 
                assessment.getOverallScore(), assessment.getRiskLevel());

        return assessment;
    }

    private void updateProfile(UUID taxpayerId, String tin, RiskAssessment assessment) {
        TaxpayerRiskProfileEntity profile = profileRepository.findById(taxpayerId)
                .orElse(TaxpayerRiskProfileEntity.builder()
                        .taxpayerId(taxpayerId)
                        .tin(tin)
                        .previousScores("[]")
                        .configVersion(1)
                        .build());

        // Update with latest score
        profile.setCurrentRiskScore(assessment.getOverallScore());
        profile.setCurrentRiskLevel(assessment.getRiskLevel().name());
        profile.setLastAssessmentDate(assessment.getAssessmentDate());

        // Update trend (simplified: compare with previous score)
        BigDecimal previous = profile.getCurrentRiskScore() != null ? profile.getCurrentRiskScore() : assessment.getOverallScore();
        if (assessment.getOverallScore().compareTo(previous) > 0) {
            profile.setRiskTrend("DETERIORATING");
        } else if (assessment.getOverallScore().compareTo(previous) < 0) {
            profile.setRiskTrend("IMPROVING");
        } else {
            profile.setRiskTrend("STABLE");
        }

        profileRepository.save(profile);
    }

    private RiskAssessmentEntity mapToEntity(RiskAssessment domain) {
        return RiskAssessmentEntity.builder()
                .id(domain.getId())
                .taxpayerId(domain.getTaxpayerId())
                .tin(domain.getTin())
                .assessmentDate(domain.getAssessmentDate())
                .overallScore(domain.getOverallScore())
                .riskLevel(domain.getRiskLevel().name())
                .confidenceFactor(domain.getConfidenceFactor() != null ? domain.getConfidenceFactor().getTotal() : null)
                // ❌ REMOVED: recommendedAuditType - Not Risk Engine's responsibility
                .priorityRank(domain.getPriorityRank())
                .status(domain.getStatus().name())
                .configVersion(domain.getConfigVersion())
                .overrideJustification(domain.getOverrideJustification())
                .overriddenBy(domain.getOverriddenBy())
                .overriddenAt(domain.getOverriddenAt())
                .build();
    }
}
