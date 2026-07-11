package com.practice.risk.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.risk.domain.event.CriticalRiskAlertEvent;
import com.practice.risk.domain.event.RiskAssessmentCompletedEvent;
import com.practice.risk.domain.event.RiskProfileUpdatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Risk Event Producer
 * 
 * Publishes Risk Engine events to Kafka for consumption by other modules.
 * Only active when Kafka is enabled in configuration.
 * 
 * Events Published:
 * 1. RiskAssessmentCompletedEvent - When assessment finishes
 * 2. RiskProfileUpdatedEvent - When profile trend changes
 * 3. CriticalRiskAlertEvent - When critical risk is identified
 * 
 * Topic Configuration:
 * Topics are defined in application.yml under risk-engine.events.topics
 */
@Component
@Slf4j
@ConditionalOnProperty(name = "risk-engine.events.kafka-enabled", havingValue = "true", matchIfMissing = true)
public class RiskEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${risk-engine.events.topics.assessment-completed:tax-audit.risk-assessment-completed}")
    private String assessmentCompletedTopic;
    
    @Value("${risk-engine.events.topics.profile-updated:tax-audit.risk-profile-updated}")
    private String profileUpdatedTopic;
    
    @Value("${risk-engine.events.topics.critical-risk-alert:tax-audit.critical-risk-alert}")
    private String criticalRiskAlertTopic;
    
    public RiskEventProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        log.info("[EVENT PRODUCER] Kafka event producer initialized");
    }

    /**
     * Publish risk assessment completed event
     * 
     * @param event Assessment completed event
     */
    public void publishAssessmentCompleted(RiskAssessmentCompletedEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(
                assessmentCompletedTopic, 
                event.getTaxpayerId().toString(), 
                payload
            );
            
            log.info("[EVENT] Published RiskAssessmentCompletedEvent: taxpayer={}, score={}, level={}",
                    event.getTaxpayerId(), event.getOverallScore(), event.getRiskLevel());
                    
        } catch (Exception e) {
            log.error("[EVENT ERROR] Failed to publish RiskAssessmentCompletedEvent for taxpayer: {}", 
                     event.getTaxpayerId(), e);
            // TODO: Add dead letter queue handling
        }
    }
    
    /**
     * Publish risk profile updated event
     * 
     * @param event Profile updated event
     */
    public void publishProfileUpdated(RiskProfileUpdatedEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(
                profileUpdatedTopic,
                event.getTaxpayerId().toString(),
                payload
            );
            
            log.info("[EVENT] Published RiskProfileUpdatedEvent: taxpayer={}, trend={}, change={}",
                    event.getTaxpayerId(), event.getTrend(), event.getScoreChange());
                    
        } catch (Exception e) {
            log.error("[EVENT ERROR] Failed to publish RiskProfileUpdatedEvent for taxpayer: {}", 
                     event.getTaxpayerId(), e);
        }
    }
    
    /**
     * Publish critical risk alert event
     * 
     * @param event Critical risk alert event
     */
    public void publishCriticalRiskAlert(CriticalRiskAlertEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(
                criticalRiskAlertTopic,
                event.getTaxpayerId().toString(),
                payload
            );
            
            log.warn("[EVENT] ⚠️  Published CriticalRiskAlertEvent: taxpayer={}, score={}, priority={}",
                    event.getTaxpayerId(), event.getRiskScore(), event.getAlertPriority());
                    
        } catch (Exception e) {
            log.error("[EVENT ERROR] Failed to publish CriticalRiskAlertEvent for taxpayer: {}", 
                     event.getTaxpayerId(), e);
            // Critical alerts should have retry mechanism
        }
    }
}
