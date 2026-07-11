package com.practice.risk.infrastructure.config;

import com.practice.risk.persistence.jpa.entity.RiskIndicatorConfigEntity;
import com.practice.risk.persistence.jpa.repository.RiskIndicatorConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for managing risk indicator configurations
 * 
 * Provides configuration data to the domain scoring service.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class IndicatorConfigurationService {
    
    private final RiskIndicatorConfigRepository repository;
    
    /**
     * Get all active indicator configurations
     * 
     * @return List of active indicators
     */
    public List<RiskIndicatorConfigEntity> getAllActiveIndicators() {
        return repository.findByIsActiveTrue();
    }
    
    /**
     * Get indicator configuration by code
     * 
     * @param indicatorCode Indicator code
     * @return Indicator configuration
     */
    public RiskIndicatorConfigEntity getIndicatorByCode(String indicatorCode) {
        return repository.findByIndicatorCode(indicatorCode)
                .orElseThrow(() -> new IllegalArgumentException("Indicator not found: " + indicatorCode));
    }
}
