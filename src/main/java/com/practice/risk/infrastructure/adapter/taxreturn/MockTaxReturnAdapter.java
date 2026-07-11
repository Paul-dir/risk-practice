package com.practice.risk.infrastructure.adapter;

import com.practice.risk.application.port.outbound.TaxReturnPort;
import com.practice.risk.domain.service.TaxpayerData;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Component
public class MockTaxReturnAdapter implements TaxReturnPort {

    @Override
    public TaxpayerData.ReturnData getReturns(UUID taxpayerId, int taxYear) {
        // Return realistic mock data
        return TaxpayerData.ReturnData.builder()
                .lateFilingDays(45)
                .numberOfAmendments(2)
                .numberOfNonFilingPeriods(0)
                .declaredRevenues(Map.of("VAT", BigDecimal.valueOf(5_000_000)))
                .build();
    }
}
