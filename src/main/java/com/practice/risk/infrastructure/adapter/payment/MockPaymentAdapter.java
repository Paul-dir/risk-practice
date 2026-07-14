package com.practice.risk.infrastructure.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.practice.risk.application.port.outbound.PaymentPort;
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
public class MockPaymentAdapter implements PaymentPort {

    private final TestTaxpayerDataLoader testDataLoader;

    @Override
    public TaxpayerData.PaymentData getPaymentHistory(UUID taxpayerId) {
        JsonNode data = testDataLoader.getTaxpayerData(taxpayerId);
        
        if (data != null) {
            JsonNode paymentSummary = data.get("payment_summary");
            
            log.info("Using test payment data for taxpayer: {}", data.get("business_name").asText());
            
            BigDecimal outstanding = paymentSummary != null ? 
                    BigDecimal.valueOf(paymentSummary.get("total_outstanding").asDouble(0)) : BigDecimal.ZERO;
            
            return TaxpayerData.PaymentData.builder()
                    .latePaymentDays(paymentSummary != null ? paymentSummary.get("total_late_days").asInt(0) : 0)
                    .totalOwed(outstanding)
                    .totalPaid(BigDecimal.ZERO) // Will be calculated from history if needed
                    .numberOfLatePayments(paymentSummary != null ? paymentSummary.get("late_payment_count").asInt(0) : 0)
                    .build();
        }
        
        // Fallback to generic mock data
        log.warn("No test data found for taxpayer {}, using fallback data", taxpayerId);
        return TaxpayerData.PaymentData.builder()
                .latePaymentDays(30)
                .totalOwed(BigDecimal.valueOf(1_000_000))
                .totalPaid(BigDecimal.valueOf(750_000))
                .numberOfLatePayments(4)
                .build();
    }
}
