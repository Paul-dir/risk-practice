package com.practice.risk.infrastructure.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.practice.risk.application.port.outbound.IntegrationPort;
import com.practice.risk.domain.service.TaxpayerData;
import com.practice.risk.infrastructure.testdata.TestTaxpayerDataLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MockIntegrationAdapter implements IntegrationPort {

    private final TestTaxpayerDataLoader testDataLoader;

    @Override
    public TaxpayerData.ExternalData getExternalData(UUID taxpayerId) {
        JsonNode data = testDataLoader.getTaxpayerData(taxpayerId);
        
        if (data != null) {
            JsonNode vatInfo = data.get("vat_information");
            JsonNode customsData = data.get("customs_data");
            JsonNode relatedParties = data.get("related_parties");
            
            log.info("Using test external data for taxpayer: {}", data.get("business_name").asText());
            
            BigDecimal importsValue = BigDecimal.ZERO;
            BigDecimal domesticSales = BigDecimal.ZERO;
            if (customsData != null && customsData.has("declared_import_value")) {
                importsValue = BigDecimal.valueOf(customsData.get("declared_import_value").asDouble(0));
                domesticSales = BigDecimal.valueOf(customsData.get("matching_domestic_sales").asDouble(0));
            }
            
            BigDecimal inputVat = vatInfo != null ? 
                    BigDecimal.valueOf(vatInfo.get("input_vat").asDouble(0)) : BigDecimal.ZERO;
            BigDecimal outputVat = vatInfo != null ? 
                    BigDecimal.valueOf(vatInfo.get("output_vat").asDouble(0)) : BigDecimal.ZERO;
            
            BigDecimal relatedPartyPct = BigDecimal.ZERO;
            if (relatedParties != null && relatedParties.has("related_party_txn_pct_of_revenue")) {
                relatedPartyPct = BigDecimal.valueOf(relatedParties.get("related_party_txn_pct_of_revenue").asDouble(0));
            }
            
            return TaxpayerData.ExternalData.builder()
                    .importsValue(importsValue)
                    .domesticSalesValue(domesticSales)
                    .inputVat(inputVat)
                    .outputVat(outputVat)
                    .cashTransactionPercentage(BigDecimal.valueOf(10)) // Default, not in test data
                    .relatedPartyTransactionPercentage(relatedPartyPct)
                    .build();
        }
        
        // Fallback to generic mock data
        log.warn("No test data found for taxpayer {}, using fallback data", taxpayerId);
        return TaxpayerData.ExternalData.builder()
                .importsValue(BigDecimal.valueOf(2_000_000))
                .domesticSalesValue(BigDecimal.valueOf(4_000_000))
                .inputVat(BigDecimal.valueOf(400_000))
                .outputVat(BigDecimal.valueOf(800_000))
                .cashTransactionPercentage(BigDecimal.valueOf(10))
                .relatedPartyTransactionPercentage(BigDecimal.valueOf(45))
                .build();
    }
}
