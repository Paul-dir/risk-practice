package com.practice.risk.infrastructure.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.practice.risk.application.port.outbound.TaxReturnPort;
import com.practice.risk.domain.service.TaxpayerData;
import com.practice.risk.infrastructure.testdata.TestTaxpayerDataLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MockTaxReturnAdapter implements TaxReturnPort {

    private final TestTaxpayerDataLoader testDataLoader;

    @Override
    public TaxpayerData.ReturnData getReturns(UUID taxpayerId, int taxYear) {
        JsonNode data = testDataLoader.getTaxpayerData(taxpayerId);
        
        if (data != null) {
            JsonNode filingSummary = data.get("filing_summary");
            JsonNode financialData = data.get("financial_data");
            
            log.info("Using test filing data for taxpayer: {}", data.get("business_name").asText());
            
            Map<String, BigDecimal> revenues = new HashMap<>();
            if (financialData != null && financialData.has("revenue_2025")) {
                revenues.put("VAT", BigDecimal.valueOf(financialData.get("revenue_2025").asDouble()));
            }
            
            return TaxpayerData.ReturnData.builder()
                    .lateFilingDays(filingSummary != null ? filingSummary.get("total_late_days").asInt(0) : 0)
                    .numberOfAmendments(filingSummary != null ? filingSummary.get("amendment_count").asInt(0) : 0)
                    .numberOfNonFilingPeriods(filingSummary != null ? filingSummary.get("missing_count").asInt(0) : 0)
                    .declaredRevenues(revenues)
                    .build();
        }
        
        // Fallback to generic mock data
        log.warn("No test data found for taxpayer {}, using fallback data", taxpayerId);
        return TaxpayerData.ReturnData.builder()
                .lateFilingDays(45)
                .numberOfAmendments(2)
                .numberOfNonFilingPeriods(0)
                .declaredRevenues(Map.of("VAT", BigDecimal.valueOf(5_000_000)))
                .build();
    }
}
