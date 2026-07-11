package com.practice.risk.infrastructure.adapter;

import com.practice.risk.application.port.outbound.IntegrationPort;
import com.practice.risk.domain.service.TaxpayerData;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class MockIntegrationAdapter implements IntegrationPort {

    @Override
    public TaxpayerData.ExternalData getExternalData(UUID taxpayerId) {
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
