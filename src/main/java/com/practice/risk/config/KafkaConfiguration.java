package com.practice.risk.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

import jakarta.annotation.PostConstruct;

/**
 * Kafka Configuration
 * 
 * Conditionally enables Kafka based on configuration property.
 * If Kafka is not available, the application will still start.
 */
@Configuration
@EnableKafka
@ConditionalOnProperty(name = "risk-engine.events.kafka-enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class KafkaConfiguration {
    
    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;
    
    @PostConstruct
    public void init() {
        log.info("[KAFKA CONFIG] Kafka enabled - Bootstrap servers: {}", bootstrapServers);
        log.info("[KAFKA CONFIG] Event publishing is active");
    }
}
