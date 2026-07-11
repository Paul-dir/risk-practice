package com.practice.risk.infrastructure.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.risk.domain.model.RiskAssessment;
import com.practice.risk.domain.model.TaxpayerRiskProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

/**
 * Redis Cache Service for Risk Engine
 * 
 * Caching Strategy:
 * - Risk Profiles: 15 minutes (frequently accessed, moderate change rate)
 * - Assessments: 1 hour (less frequently accessed, low change rate)
 * - Configuration: 5 minutes (rarely changes, needs quick propagation)
 * - Benchmarks: 1 hour (static data, updated daily)
 * 
 * Cache Invalidation:
 * - Automatic TTL expiration
 * - Manual invalidation on updates
 * - Batch invalidation for configuration changes
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RiskCacheService {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    
    // Cache key prefixes
    private static final String PROFILE_PREFIX = "risk:profile:";
    private static final String ASSESSMENT_PREFIX = "risk:assessment:";
    private static final String CONFIG_PREFIX = "risk:config:";
    private static final String BENCHMARK_PREFIX = "risk:benchmark:";
    
    // TTL durations (from application.yml)
    private static final Duration PROFILE_TTL = Duration.ofMinutes(15);
    private static final Duration ASSESSMENT_TTL = Duration.ofHours(1);
    private static final Duration CONFIG_TTL = Duration.ofMinutes(5);
    private static final Duration BENCHMARK_TTL = Duration.ofHours(1);
    
    // ============================================================================
    // Risk Profile Caching
    // ============================================================================
    
    /**
     * Get cached risk profile for a taxpayer
     * 
     * @param taxpayerId Taxpayer UUID
     * @return Optional containing cached profile if present
     */
    public Optional<TaxpayerRiskProfile> getRiskProfile(UUID taxpayerId) {
        try {
            String key = PROFILE_PREFIX + taxpayerId.toString();
            String cached = redisTemplate.opsForValue().get(key);
            
            if (cached != null) {
                log.debug("[CACHE HIT] Risk profile for taxpayer: {}", taxpayerId);
                TaxpayerRiskProfile profile = objectMapper.readValue(cached, TaxpayerRiskProfile.class);
                return Optional.of(profile);
            }
            
            log.debug("[CACHE MISS] Risk profile for taxpayer: {}", taxpayerId);
            return Optional.empty();
            
        } catch (Exception e) {
            log.error("[CACHE ERROR] Failed to retrieve profile for taxpayer: {}", taxpayerId, e);
            return Optional.empty();
        }
    }
    
    /**
     * Cache risk profile for a taxpayer
     * 
     * @param taxpayerId Taxpayer UUID
     * @param profile Risk profile to cache
     */
    public void cacheRiskProfile(UUID taxpayerId, TaxpayerRiskProfile profile) {
        try {
            String key = PROFILE_PREFIX + taxpayerId.toString();
            String value = objectMapper.writeValueAsString(profile);
            redisTemplate.opsForValue().set(key, value, PROFILE_TTL);
            
            log.debug("[CACHE SET] Risk profile for taxpayer: {}", taxpayerId);
            
        } catch (Exception e) {
            log.error("[CACHE ERROR] Failed to cache profile for taxpayer: {}", taxpayerId, e);
        }
    }
    
    /**
     * Invalidate cached risk profile
     * 
     * @param taxpayerId Taxpayer UUID
     */
    public void invalidateRiskProfile(UUID taxpayerId) {
        try {
            String key = PROFILE_PREFIX + taxpayerId.toString();
            redisTemplate.delete(key);
            log.debug("[CACHE INVALIDATE] Risk profile for taxpayer: {}", taxpayerId);
        } catch (Exception e) {
            log.error("[CACHE ERROR] Failed to invalidate profile for taxpayer: {}", taxpayerId, e);
        }
    }
    
    // ============================================================================
    // Risk Assessment Caching
    // ============================================================================
    
    /**
     * Get cached risk assessment
     * 
     * @param assessmentId Assessment UUID
     * @return Optional containing cached assessment if present
     */
    public Optional<RiskAssessment> getAssessment(UUID assessmentId) {
        try {
            String key = ASSESSMENT_PREFIX + assessmentId.toString();
            String cached = redisTemplate.opsForValue().get(key);
            
            if (cached != null) {
                log.debug("[CACHE HIT] Assessment: {}", assessmentId);
                RiskAssessment assessment = objectMapper.readValue(cached, RiskAssessment.class);
                return Optional.of(assessment);
            }
            
            log.debug("[CACHE MISS] Assessment: {}", assessmentId);
            return Optional.empty();
            
        } catch (Exception e) {
            log.error("[CACHE ERROR] Failed to retrieve assessment: {}", assessmentId, e);
            return Optional.empty();
        }
    }
    
    /**
     * Cache risk assessment
     * 
     * @param assessment Risk assessment to cache
     */
    public void cacheAssessment(RiskAssessment assessment) {
        try {
            String key = ASSESSMENT_PREFIX + assessment.getId().toString();
            String value = objectMapper.writeValueAsString(assessment);
            redisTemplate.opsForValue().set(key, value, ASSESSMENT_TTL);
            
            log.debug("[CACHE SET] Assessment: {}", assessment.getId());
            
        } catch (Exception e) {
            log.error("[CACHE ERROR] Failed to cache assessment: {}", assessment.getId(), e);
        }
    }
    
    /**
     * Get cached latest assessment for taxpayer
     * 
     * @param taxpayerId Taxpayer UUID
     * @return Optional containing cached assessment if present
     */
    public Optional<RiskAssessment> getLatestAssessment(UUID taxpayerId) {
        try {
            String key = ASSESSMENT_PREFIX + "latest:" + taxpayerId.toString();
            String cached = redisTemplate.opsForValue().get(key);
            
            if (cached != null) {
                log.debug("[CACHE HIT] Latest assessment for taxpayer: {}", taxpayerId);
                RiskAssessment assessment = objectMapper.readValue(cached, RiskAssessment.class);
                return Optional.of(assessment);
            }
            
            log.debug("[CACHE MISS] Latest assessment for taxpayer: {}", taxpayerId);
            return Optional.empty();
            
        } catch (Exception e) {
            log.error("[CACHE ERROR] Failed to retrieve latest assessment for taxpayer: {}", taxpayerId, e);
            return Optional.empty();
        }
    }
    
    /**
     * Cache latest assessment for taxpayer
     * 
     * @param taxpayerId Taxpayer UUID
     * @param assessment Latest risk assessment
     */
    public void cacheLatestAssessment(UUID taxpayerId, RiskAssessment assessment) {
        try {
            String key = ASSESSMENT_PREFIX + "latest:" + taxpayerId.toString();
            String value = objectMapper.writeValueAsString(assessment);
            redisTemplate.opsForValue().set(key, value, ASSESSMENT_TTL);
            
            log.debug("[CACHE SET] Latest assessment for taxpayer: {}", taxpayerId);
            
        } catch (Exception e) {
            log.error("[CACHE ERROR] Failed to cache latest assessment for taxpayer: {}", taxpayerId, e);
        }
    }
    
    // ============================================================================
    // Configuration Caching
    // ============================================================================
    
    /**
     * Get cached indicator configuration
     * 
     * @param indicatorCode Indicator code
     * @return Optional containing cached configuration if present
     */
    public Optional<String> getIndicatorConfig(String indicatorCode) {
        try {
            String key = CONFIG_PREFIX + indicatorCode;
            String cached = redisTemplate.opsForValue().get(key);
            
            if (cached != null) {
                log.debug("[CACHE HIT] Indicator config: {}", indicatorCode);
                return Optional.of(cached);
            }
            
            log.debug("[CACHE MISS] Indicator config: {}", indicatorCode);
            return Optional.empty();
            
        } catch (Exception e) {
            log.error("[CACHE ERROR] Failed to retrieve config for indicator: {}", indicatorCode, e);
            return Optional.empty();
        }
    }
    
    /**
     * Cache indicator configuration
     * 
     * @param indicatorCode Indicator code
     * @param config Configuration JSON
     */
    public void cacheIndicatorConfig(String indicatorCode, String config) {
        try {
            String key = CONFIG_PREFIX + indicatorCode;
            redisTemplate.opsForValue().set(key, config, CONFIG_TTL);
            
            log.debug("[CACHE SET] Indicator config: {}", indicatorCode);
            
        } catch (Exception e) {
            log.error("[CACHE ERROR] Failed to cache config for indicator: {}", indicatorCode, e);
        }
    }
    
    /**
     * Invalidate all indicator configurations
     * (Use when configuration changes globally)
     */
    public void invalidateAllConfigs() {
        try {
            var keys = redisTemplate.keys(CONFIG_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("[CACHE INVALIDATE] All indicator configs ({} keys)", keys.size());
            }
        } catch (Exception e) {
            log.error("[CACHE ERROR] Failed to invalidate all configs", e);
        }
    }
    
    // ============================================================================
    // Benchmark Caching
    // ============================================================================
    
    /**
     * Get cached industry benchmark
     * 
     * @param industryCode Industry code
     * @return Optional containing cached benchmark if present
     */
    public Optional<String> getIndustryBenchmark(String industryCode) {
        try {
            String key = BENCHMARK_PREFIX + industryCode;
            String cached = redisTemplate.opsForValue().get(key);
            
            if (cached != null) {
                log.debug("[CACHE HIT] Industry benchmark: {}", industryCode);
                return Optional.of(cached);
            }
            
            log.debug("[CACHE MISS] Industry benchmark: {}", industryCode);
            return Optional.empty();
            
        } catch (Exception e) {
            log.error("[CACHE ERROR] Failed to retrieve benchmark for industry: {}", industryCode, e);
            return Optional.empty();
        }
    }
    
    /**
     * Cache industry benchmark
     * 
     * @param industryCode Industry code
     * @param benchmark Benchmark data JSON
     */
    public void cacheIndustryBenchmark(String industryCode, String benchmark) {
        try {
            String key = BENCHMARK_PREFIX + industryCode;
            redisTemplate.opsForValue().set(key, benchmark, BENCHMARK_TTL);
            
            log.debug("[CACHE SET] Industry benchmark: {}", industryCode);
            
        } catch (Exception e) {
            log.error("[CACHE ERROR] Failed to cache benchmark for industry: {}", industryCode, e);
        }
    }
    
    // ============================================================================
    // Cache Management
    // ============================================================================
    
    /**
     * Clear all risk engine caches
     * (Use with caution - performance impact)
     */
    public void clearAllCaches() {
        try {
            var keys = redisTemplate.keys("risk:*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.warn("[CACHE CLEAR] All risk engine caches cleared ({} keys)", keys.size());
            }
        } catch (Exception e) {
            log.error("[CACHE ERROR] Failed to clear all caches", e);
        }
    }
    
    /**
     * Get cache statistics
     * 
     * @return Cache stats as formatted string
     */
    public String getCacheStats() {
        try {
            var profileKeys = redisTemplate.keys(PROFILE_PREFIX + "*");
            var assessmentKeys = redisTemplate.keys(ASSESSMENT_PREFIX + "*");
            var configKeys = redisTemplate.keys(CONFIG_PREFIX + "*");
            var benchmarkKeys = redisTemplate.keys(BENCHMARK_PREFIX + "*");
            
            return String.format(
                "Cache Statistics:\n" +
                "  Profiles: %d\n" +
                "  Assessments: %d\n" +
                "  Configs: %d\n" +
                "  Benchmarks: %d\n" +
                "  Total: %d",
                profileKeys != null ? profileKeys.size() : 0,
                assessmentKeys != null ? assessmentKeys.size() : 0,
                configKeys != null ? configKeys.size() : 0,
                benchmarkKeys != null ? benchmarkKeys.size() : 0,
                (profileKeys != null ? profileKeys.size() : 0) +
                (assessmentKeys != null ? assessmentKeys.size() : 0) +
                (configKeys != null ? configKeys.size() : 0) +
                (benchmarkKeys != null ? benchmarkKeys.size() : 0)
            );
        } catch (Exception e) {
            log.error("[CACHE ERROR] Failed to get cache stats", e);
            return "Cache statistics unavailable";
        }
    }
}
