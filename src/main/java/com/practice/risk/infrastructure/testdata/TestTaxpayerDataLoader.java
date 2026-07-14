package com.practice.risk.infrastructure.testdata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Loads test taxpayer data from JSON file for mock adapters
 */
@Slf4j
@Component
public class TestTaxpayerDataLoader {

    private final Map<UUID, JsonNode> taxpayerDataMap = new HashMap<>();
    private final Map<String, JsonNode> taxpayerDataByTin = new HashMap<>();
    private final ObjectMapper objectMapper;

    public TestTaxpayerDataLoader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        loadTestData();
    }

    private void loadTestData() {
        try {
            ClassPathResource resource = new ClassPathResource("test-taxpayers.json");
            JsonNode root = objectMapper.readTree(resource.getInputStream());
            JsonNode taxpayers = root.get("taxpayers");

            if (taxpayers != null && taxpayers.isArray()) {
                for (JsonNode taxpayer : taxpayers) {
                    String taxpayerId = taxpayer.get("taxpayer_id").asText();
                    String tin = taxpayer.get("tin").asText();
                    
                    UUID uuid = UUID.fromString(taxpayerId);
                    taxpayerDataMap.put(uuid, taxpayer);
                    taxpayerDataByTin.put(tin, taxpayer);
                    
                    log.info("Loaded test data for taxpayer: {} (TIN: {})", 
                            taxpayer.get("business_name").asText(), tin);
                }
                log.info("Successfully loaded {} test taxpayers", taxpayerDataMap.size());
            }
        } catch (IOException e) {
            log.error("Failed to load test taxpayer data", e);
        }
    }

    public JsonNode getTaxpayerData(UUID taxpayerId) {
        return taxpayerDataMap.get(taxpayerId);
    }

    public JsonNode getTaxpayerDataByTin(String tin) {
        return taxpayerDataByTin.get(tin);
    }

    public boolean hasTestData(UUID taxpayerId) {
        return taxpayerDataMap.containsKey(taxpayerId);
    }

    public Map<UUID, JsonNode> getAllTaxpayers() {
        return new HashMap<>(taxpayerDataMap);
    }
}
