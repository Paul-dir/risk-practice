package com.practice.risk.application.scheduler;

import com.practice.risk.application.service.RiskAssessmentOrchestrator;
import com.practice.risk.persistence.jpa.entity.RiskAssessmentEntity;
import com.practice.risk.persistence.jpa.repository.RiskAssessmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@RequiredArgsConstructor
public class BatchScoringService {

    private final RiskAssessmentOrchestrator orchestrator;
    private final RiskAssessmentRepository assessmentRepository;

    // Demo: score a fixed list of taxpayers every hour (for practice)
    private static final List<UUID> DEMO_TAXPAYERS = List.of(
            UUID.randomUUID(), // will be replaced with real TINs in production
            UUID.randomUUID()
    );

    @Scheduled(fixedDelay = 3600000) // every hour
    public void scoreAllTaxpayers() {
        log.info("Starting batch scoring for {} taxpayers", DEMO_TAXPAYERS.size());
        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger failed = new AtomicInteger(0);

        DEMO_TAXPAYERS.forEach(taxpayerId -> {
            try {
                // In production, you would look up the TIN from registration service
                String tin = "TIN-" + taxpayerId.toString().substring(0, 8);
                orchestrator.assessTaxpayer(taxpayerId, tin);
                success.incrementAndGet();
            } catch (Exception e) {
                log.error("Failed to score taxpayer: {}", taxpayerId, e);
                failed.incrementAndGet();
            }
        });

        // After scoring all, update priority ranks
        updatePriorityRanks();

        log.info("Batch scoring completed. Success: {}, Failed: {}", success.get(), failed.get());
    }

    @Transactional
    public void updatePriorityRanks() {
        List<RiskAssessmentEntity> assessments = assessmentRepository.findAllByOrderByOverallScoreDesc();
        AtomicInteger rank = new AtomicInteger(1);
        assessments.forEach(a -> {
            a.setPriorityRank(rank.getAndIncrement());
            assessmentRepository.save(a);
        });
        log.info("Priority ranks updated for {} assessments", assessments.size());
    }
}
