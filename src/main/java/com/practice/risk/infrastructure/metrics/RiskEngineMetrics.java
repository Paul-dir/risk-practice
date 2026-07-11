package com.practice.risk.infrastructure.metrics;

import io.micrometer.core.instrument.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Risk Engine Metrics for Prometheus
 * 
 * Provides comprehensive metrics for monitoring Risk Engine performance,
 * health, and business KPIs.
 * 
 * Metric Categories:
 * 1. Assessment Performance (timing, throughput)
 * 2. Risk Distribution (score distribution, risk levels)
 * 3. Data Quality (confidence, completeness)
 * 4. System Health (errors, cache hits)
 * 5. Business KPIs (high-risk cases, trends)
 * 
 * Metrics are automatically exposed via /actuator/prometheus endpoint
 */
@Component
@Slf4j
public class RiskEngineMetrics {
    
    private final MeterRegistry registry;
    
    // Counters
    private final Counter assessmentCompletedCounter;
    private final Counter assessmentFailedCounter;
    private final Counter eventPublishedCounter;
    private final Counter cacheHitCounter;
    private final Counter cacheMissCounter;
    
    // Timers
    private final Timer assessmentTimer;
    private final Timer dataCollectionTimer;
    private final Timer scoringTimer;
    private final Timer eventPublishingTimer;
    
    // Gauges
    private final AtomicInteger activeAssessments;
    private final AtomicInteger criticalRiskCount;
    private final AtomicInteger highRiskCount;
    
    // Distribution Summaries
    private final DistributionSummary riskScoreDistribution;
    private final DistributionSummary confidenceDistribution;
    
    public RiskEngineMetrics(MeterRegistry registry) {
        this.registry = registry;
        
        // ============================================================================
        // COUNTERS: Track cumulative events
        // ============================================================================
        
        this.assessmentCompletedCounter = Counter.builder("risk.assessment.completed")
                .description("Total number of completed risk assessments")
                .tag("component", "risk-engine")
                .register(registry);
        
        this.assessmentFailedCounter = Counter.builder("risk.assessment.failed")
                .description("Total number of failed risk assessments")
                .tag("component", "risk-engine")
                .register(registry);
        
        this.eventPublishedCounter = Counter.builder("risk.event.published")
                .description("Total number of events published")
                .tag("component", "risk-engine")
                .register(registry);
        
        this.cacheHitCounter = Counter.builder("risk.cache.hit")
                .description("Total number of cache hits")
                .tag("component", "risk-engine")
                .register(registry);
        
        this.cacheMissCounter = Counter.builder("risk.cache.miss")
                .description("Total number of cache misses")
                .tag("component", "risk-engine")
                .register(registry);
        
        // ============================================================================
        // TIMERS: Measure duration of operations
        // ============================================================================
        
        this.assessmentTimer = Timer.builder("risk.assessment.duration")
                .description("Time taken to complete full risk assessment")
                .tag("component", "risk-engine")
                .publishPercentiles(0.5, 0.95, 0.99)  // p50, p95, p99
                .register(registry);
        
        this.dataCollectionTimer = Timer.builder("risk.data.collection.duration")
                .description("Time taken to collect taxpayer data")
                .tag("component", "risk-engine")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
        
        this.scoringTimer = Timer.builder("risk.scoring.duration")
                .description("Time taken to calculate risk score")
                .tag("component", "risk-engine")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
        
        this.eventPublishingTimer = Timer.builder("risk.event.publish.duration")
                .description("Time taken to publish event to Kafka")
                .tag("component", "risk-engine")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
        
        // ============================================================================
        // GAUGES: Track current state
        // ============================================================================
        
        this.activeAssessments = registry.gauge(
                "risk.assessment.active",
                Tags.of("component", "risk-engine"),
                new AtomicInteger(0)
        );
        
        this.criticalRiskCount = registry.gauge(
                "risk.critical.count",
                Tags.of("component", "risk-engine"),
                new AtomicInteger(0)
        );
        
        this.highRiskCount = registry.gauge(
                "risk.high.count",
                Tags.of("component", "risk-engine"),
                new AtomicInteger(0)
        );
        
        // ============================================================================
        // DISTRIBUTION SUMMARIES: Track value distributions
        // ============================================================================
        
        this.riskScoreDistribution = DistributionSummary.builder("risk.score.distribution")
                .description("Distribution of risk scores")
                .tag("component", "risk-engine")
                .publishPercentiles(0.5, 0.75, 0.90, 0.95, 0.99)
                .register(registry);
        
        this.confidenceDistribution = DistributionSummary.builder("risk.confidence.distribution")
                .description("Distribution of confidence factors")
                .tag("component", "risk-engine")
                .publishPercentiles(0.5, 0.75, 0.90, 0.95, 0.99)
                .register(registry);
        
        log.info("[METRICS] Risk Engine metrics initialized and registered with Prometheus");
    }
    
    // ============================================================================
    // PUBLIC API: Record Metrics
    // ============================================================================
    
    /**
     * Record a completed assessment
     * 
     * @param durationMs Assessment duration in milliseconds
     * @param riskScore Final risk score (0-100)
     * @param riskLevel Risk level (LOW, MEDIUM, HIGH, CRITICAL)
     * @param confidence Confidence factor (0.0-1.0)
     */
    public void recordAssessmentCompleted(long durationMs, double riskScore, String riskLevel, double confidence) {
        assessmentCompletedCounter.increment();
        assessmentTimer.record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);
        riskScoreDistribution.record(riskScore);
        confidenceDistribution.record(confidence);
        
        // Track by risk level
        Counter.builder("risk.assessment.by_level")
                .tag("level", riskLevel)
                .tag("component", "risk-engine")
                .register(registry)
                .increment();
        
        log.debug("[METRICS] Recorded assessment: score={}, level={}, confidence={}, duration={}ms",
                 riskScore, riskLevel, confidence, durationMs);
    }
    
    /**
     * Record a failed assessment
     * 
     * @param errorType Type of error (e.g., DATA_UNAVAILABLE, TIMEOUT)
     */
    public void recordAssessmentFailed(String errorType) {
        assessmentFailedCounter.increment();
        
        Counter.builder("risk.assessment.error")
                .tag("error_type", errorType)
                .tag("component", "risk-engine")
                .register(registry)
                .increment();
        
        log.debug("[METRICS] Recorded assessment failure: errorType={}", errorType);
    }
    
    /**
     * Record data collection timing
     * 
     * @param durationMs Data collection duration in milliseconds
     * @param source Data source (e.g., REGISTRATION, TAX_RETURN, PAYMENT)
     */
    public void recordDataCollection(long durationMs, String source) {
        dataCollectionTimer.record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);
        
        Timer.builder("risk.data.collection.by_source")
                .tag("source", source)
                .tag("component", "risk-engine")
                .register(registry)
                .record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);
        
        log.debug("[METRICS] Recorded data collection: source={}, duration={}ms", source, durationMs);
    }
    
    /**
     * Record scoring timing
     * 
     * @param durationMs Scoring duration in milliseconds
     */
    public void recordScoring(long durationMs) {
        scoringTimer.record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);
        log.debug("[METRICS] Recorded scoring: duration={}ms", durationMs);
    }
    
    /**
     * Record event publishing
     * 
     * @param durationMs Publishing duration in milliseconds
     * @param eventType Event type (e.g., ASSESSMENT_COMPLETED, CRITICAL_ALERT)
     * @param success Whether publishing succeeded
     */
    public void recordEventPublished(long durationMs, String eventType, boolean success) {
        if (success) {
            eventPublishedCounter.increment();
            eventPublishingTimer.record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);
        }
        
        Counter.builder("risk.event.by_type")
                .tag("event_type", eventType)
                .tag("status", success ? "success" : "failed")
                .tag("component", "risk-engine")
                .register(registry)
                .increment();
        
        log.debug("[METRICS] Recorded event: type={}, success={}, duration={}ms", 
                 eventType, success, durationMs);
    }
    
    /**
     * Record cache hit
     * 
     * @param cacheType Cache type (e.g., PROFILE, ASSESSMENT, CONFIG)
     */
    public void recordCacheHit(String cacheType) {
        cacheHitCounter.increment();
        
        Counter.builder("risk.cache.hit.by_type")
                .tag("cache_type", cacheType)
                .tag("component", "risk-engine")
                .register(registry)
                .increment();
        
        log.debug("[METRICS] Cache hit: type={}", cacheType);
    }
    
    /**
     * Record cache miss
     * 
     * @param cacheType Cache type (e.g., PROFILE, ASSESSMENT, CONFIG)
     */
    public void recordCacheMiss(String cacheType) {
        cacheMissCounter.increment();
        
        Counter.builder("risk.cache.miss.by_type")
                .tag("cache_type", cacheType)
                .tag("component", "risk-engine")
                .register(registry)
                .increment();
        
        log.debug("[METRICS] Cache miss: type={}", cacheType);
    }
    
    /**
     * Update active assessment count
     * 
     * @param count Current number of active assessments
     */
    public void setActiveAssessments(int count) {
        activeAssessments.set(count);
        log.debug("[METRICS] Active assessments: {}", count);
    }
    
    /**
     * Increment active assessment count
     */
    public void incrementActiveAssessments() {
        activeAssessments.incrementAndGet();
    }
    
    /**
     * Decrement active assessment count
     */
    public void decrementActiveAssessments() {
        activeAssessments.decrementAndGet();
    }
    
    /**
     * Update critical risk count
     * 
     * @param count Current number of critical risk cases
     */
    public void setCriticalRiskCount(int count) {
        criticalRiskCount.set(count);
        log.debug("[METRICS] Critical risk count: {}", count);
    }
    
    /**
     * Update high risk count
     * 
     * @param count Current number of high risk cases
     */
    public void setHighRiskCount(int count) {
        highRiskCount.set(count);
        log.debug("[METRICS] High risk count: {}", count);
    }
    
    /**
     * Record indicator score
     * 
     * @param indicatorCode Indicator code
     * @param score Indicator score
     */
    public void recordIndicatorScore(String indicatorCode, double score) {
        DistributionSummary.builder("risk.indicator.score")
                .tag("indicator", indicatorCode)
                .tag("component", "risk-engine")
                .register(registry)
                .record(score);
    }
    
    /**
     * Record batch processing statistics
     * 
     * @param totalProcessed Total taxpayers processed
     * @param successful Successful assessments
     * @param failed Failed assessments
     * @param durationSeconds Duration in seconds
     */
    public void recordBatchCompletion(int totalProcessed, int successful, int failed, long durationSeconds) {
        Counter.builder("risk.batch.processed")
                .tag("component", "risk-engine")
                .register(registry)
                .increment(totalProcessed);
        
        Counter.builder("risk.batch.successful")
                .tag("component", "risk-engine")
                .register(registry)
                .increment(successful);
        
        Counter.builder("risk.batch.failed")
                .tag("component", "risk-engine")
                .register(registry)
                .increment(failed);
        
        Gauge.builder("risk.batch.duration.seconds", () -> durationSeconds)
                .tag("component", "risk-engine")
                .register(registry);
        
        log.info("[METRICS] Batch completed: processed={}, successful={}, failed={}, duration={}s",
                totalProcessed, successful, failed, durationSeconds);
    }
}
