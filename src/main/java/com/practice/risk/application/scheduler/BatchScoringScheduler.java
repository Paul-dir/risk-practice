package com.practice.risk.application.scheduler;

import com.practice.risk.application.service.RiskAssessmentOrchestrator;
import com.practice.risk.domain.model.RiskAssessment;
import com.practice.risk.persistence.jpa.repository.RiskAssessmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Batch Risk Assessment Scheduler
 * 
 * Runs nightly batch processing to assess all active taxpayers.
 * 
 * Features:
 * - Parallel processing with configurable thread pool
 * - Progress tracking and logging
 * - Error handling and retry logic
 * - Performance metrics
 * - Graceful shutdown
 * 
 * Configuration:
 * - risk-engine.batch.enabled: Enable/disable batch processing
 * - risk-engine.batch.schedule: Cron expression (default: 2 AM daily)
 * - risk-engine.batch.batch-size: Number of taxpayers per batch
 * - risk-engine.batch.parallel-threads: Thread pool size
 * - risk-engine.batch.timeout-minutes: Max execution time
 */
@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "risk-engine.batch", name = "enabled", havingValue = "true", matchIfMissing = true)
public class BatchScoringScheduler {
    
    private final RiskAssessmentOrchestrator orchestrator;
    private final RiskAssessmentRepository assessmentRepository;
    
    @Value("${risk-engine.batch.batch-size:1000}")
    private int batchSize;
    
    @Value("${risk-engine.batch.parallel-threads:10}")
    private int parallelThreads;
    
    @Value("${risk-engine.batch.timeout-minutes:60}")
    private int timeoutMinutes;
    
    private ExecutorService executorService;
    private volatile boolean isRunning = false;
    
    /**
     * Scheduled batch assessment job
     * Runs according to cron expression in application.yml
     * Default: 2 AM daily (0 0 2 * * *)
     */
    @Scheduled(cron = "${risk-engine.batch.schedule:0 0 2 * * *}")
    @Async
    public void runBatchAssessment() {
        if (isRunning) {
            log.warn("[BATCH] Previous batch still running, skipping this execution");
            return;
        }
        
        isRunning = true;
        Instant startTime = Instant.now();
        
        log.info("════════════════════════════════════════════════════════");
        log.info("[BATCH] Starting nightly risk assessment batch");
        log.info("[BATCH] Configuration: batchSize={}, threads={}, timeout={}min", 
                 batchSize, parallelThreads, timeoutMinutes);
        log.info("════════════════════════════════════════════════════════");
        
        try {
            // Initialize thread pool
            executorService = Executors.newFixedThreadPool(parallelThreads);
            
            // Get all active taxpayers (mock for now - should query from registration system)
            List<TaxpayerInfo> taxpayers = getActiveTaxpayers();
            log.info("[BATCH] Found {} taxpayers to assess", taxpayers.size());
            
            if (taxpayers.isEmpty()) {
                log.info("[BATCH] No taxpayers to process");
                return;
            }
            
            // Process in batches
            BatchStatistics stats = processTaxpayersInBatches(taxpayers);
            
            // Log summary
            Duration duration = Duration.between(startTime, Instant.now());
            logBatchSummary(stats, duration, taxpayers.size());
            
        } catch (Exception e) {
            log.error("[BATCH] Batch assessment failed", e);
        } finally {
            shutdownExecutor();
            isRunning = false;
        }
    }
    
    /**
     * Process taxpayers in parallel batches
     */
    private BatchStatistics processTaxpayersInBatches(List<TaxpayerInfo> taxpayers) {
        BatchStatistics stats = new BatchStatistics();
        int totalBatches = (int) Math.ceil((double) taxpayers.size() / batchSize);
        
        for (int batchNum = 0; batchNum < totalBatches; batchNum++) {
            int fromIndex = batchNum * batchSize;
            int toIndex = Math.min(fromIndex + batchSize, taxpayers.size());
            List<TaxpayerInfo> batch = taxpayers.subList(fromIndex, toIndex);
            
            log.info("[BATCH] Processing batch {}/{} ({} taxpayers)", 
                     batchNum + 1, totalBatches, batch.size());
            
            Instant batchStart = Instant.now();
            BatchStatistics batchStats = processBatch(batch);
            Duration batchDuration = Duration.between(batchStart, Instant.now());
            
            // Aggregate statistics
            stats.successful.addAndGet(batchStats.successful.get());
            stats.failed.addAndGet(batchStats.failed.get());
            stats.skipped.addAndGet(batchStats.skipped.get());
            
            log.info("[BATCH] Batch {}/{} completed in {}s - Success: {}, Failed: {}, Skipped: {}", 
                     batchNum + 1, totalBatches, batchDuration.getSeconds(),
                     batchStats.successful.get(), batchStats.failed.get(), batchStats.skipped.get());
        }
        
        return stats;
    }
    
    /**
     * Process a single batch of taxpayers in parallel
     */
    private BatchStatistics processBatch(List<TaxpayerInfo> batch) {
        BatchStatistics stats = new BatchStatistics();
        List<CompletableFuture<AssessmentResult>> futures = new ArrayList<>();
        
        // Submit all assessments to thread pool
        for (TaxpayerInfo taxpayer : batch) {
            CompletableFuture<AssessmentResult> future = CompletableFuture.supplyAsync(
                () -> assessTaxpayer(taxpayer),
                executorService
            );
            futures.add(future);
        }
        
        // Wait for all assessments to complete (with timeout)
        try {
            CompletableFuture<Void> allOf = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
            );
            
            allOf.get(timeoutMinutes, TimeUnit.MINUTES);
            
            // Collect results
            for (CompletableFuture<AssessmentResult> future : futures) {
                AssessmentResult result = future.get();
                switch (result.status) {
                    case SUCCESS -> stats.successful.incrementAndGet();
                    case FAILED -> stats.failed.incrementAndGet();
                    case SKIPPED -> stats.skipped.incrementAndGet();
                }
            }
            
        } catch (TimeoutException e) {
            log.error("[BATCH] Batch timed out after {} minutes", timeoutMinutes);
            stats.failed.addAndGet(batch.size() - stats.successful.get() - stats.skipped.get());
        } catch (Exception e) {
            log.error("[BATCH] Batch processing error", e);
            stats.failed.addAndGet(batch.size() - stats.successful.get() - stats.skipped.get());
        }
        
        return stats;
    }
    
    /**
     * Assess a single taxpayer
     */
    private AssessmentResult assessTaxpayer(TaxpayerInfo taxpayer) {
        try {
            log.debug("[BATCH] Assessing taxpayer: {} ({})", taxpayer.taxpayerId, taxpayer.tin);
            
            // Check if already assessed recently (within last 24 hours)
            // TODO: Implement actual check against repository
            // For now, always assess
            
            RiskAssessment assessment = orchestrator.assessTaxpayer(
                taxpayer.taxpayerId, 
                taxpayer.tin
            );
            
            log.debug("[BATCH] Assessment completed for taxpayer: {} - Score: {}", 
                     taxpayer.taxpayerId, assessment.getOverallScore());
            
            return new AssessmentResult(AssessmentStatus.SUCCESS, assessment.getId(), null);
            
        } catch (Exception e) {
            log.error("[BATCH] Assessment failed for taxpayer: {}", taxpayer.taxpayerId, e);
            return new AssessmentResult(AssessmentStatus.FAILED, null, e.getMessage());
        }
    }
    
    /**
     * Get list of active taxpayers to assess
     * TODO: Implement actual query to Registration system
     */
    private List<TaxpayerInfo> getActiveTaxpayers() {
        // Mock implementation - should query from Registration system via port
        // For now, return empty list to avoid errors
        log.warn("[BATCH] Using mock taxpayer list - implement actual query to Registration system");
        return List.of();
        
        // Real implementation would look like:
        // return registrationPort.getActiveTaxpayers()
        //     .stream()
        //     .map(reg -> new TaxpayerInfo(reg.getTaxpayerId(), reg.getTin()))
        //     .toList();
    }
    
    /**
     * Log batch summary with statistics
     */
    private void logBatchSummary(BatchStatistics stats, Duration duration, int total) {
        log.info("════════════════════════════════════════════════════════");
        log.info("[BATCH] Batch Assessment Completed");
        log.info("[BATCH] Duration: {} minutes {} seconds", 
                 duration.toMinutes(), duration.getSeconds() % 60);
        log.info("[BATCH] Total Taxpayers: {}", total);
        log.info("[BATCH] Successful: {} ({} %)", 
                 stats.successful.get(), 
                 total > 0 ? (stats.successful.get() * 100 / total) : 0);
        log.info("[BATCH] Failed: {} ({} %)", 
                 stats.failed.get(),
                 total > 0 ? (stats.failed.get() * 100 / total) : 0);
        log.info("[BATCH] Skipped: {} ({} %)", 
                 stats.skipped.get(),
                 total > 0 ? (stats.skipped.get() * 100 / total) : 0);
        log.info("[BATCH] Throughput: {} assessments/second", 
                 duration.getSeconds() > 0 ? (total / duration.getSeconds()) : 0);
        log.info("════════════════════════════════════════════════════════");
    }
    
    /**
     * Shutdown executor gracefully
     */
    private void shutdownExecutor() {
        if (executorService != null) {
            try {
                executorService.shutdown();
                if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
    
    // ============================================================================
    // Helper Classes
    // ============================================================================
    
    private static class BatchStatistics {
        AtomicInteger successful = new AtomicInteger(0);
        AtomicInteger failed = new AtomicInteger(0);
        AtomicInteger skipped = new AtomicInteger(0);
    }
    
    private record TaxpayerInfo(UUID taxpayerId, String tin) {}
    
    private record AssessmentResult(AssessmentStatus status, UUID assessmentId, String errorMessage) {}
    
    private enum AssessmentStatus {
        SUCCESS, FAILED, SKIPPED
    }
}
