package com.practice.risk.infrastructure.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.practice.risk.application.port.outbound.RegistrationPort;
import com.practice.risk.domain.service.TaxpayerData;
import com.practice.risk.infrastructure.testdata.TestTaxpayerDataLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MockRegistrationAdapter implements RegistrationPort {

    private final TestTaxpayerDataLoader testDataLoader;

    @Override
    public TaxpayerData.RegistrationData getRegistration(UUID taxpayerId) {
        JsonNode data = testDataLoader.getTaxpayerData(taxpayerId);
        
        if (data != null) {
            log.info("Using test data for taxpayer registration: {}", data.get("business_name").asText());
            return TaxpayerData.RegistrationData.builder()
                    .tin(data.get("tin").asText())
                    .businessType(data.get("business_type").asText())
                    .industryCode(data.get("industry_sector").asText())
                    .location(data.get("city").asText() + ", " + data.get("region").asText())
                    .registrationDate(LocalDate.parse(data.get("registration_date").asText()))
                    .build();
        }
        
        // Fallback to generic mock data
        log.warn("No test data found for taxpayer {}, using fallback data", taxpayerId);
        return TaxpayerData.RegistrationData.builder()
                .tin("TIN-UNKNOWN")
                .businessType("Manufacturing")
                .industryCode("C10")
                .location("Addis Ababa")
                .registrationDate(LocalDate.now().minusYears(3))
                .build();
    }
}
