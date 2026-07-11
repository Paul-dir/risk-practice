package com.practice.risk.infrastructure.adapter;

import com.practice.risk.application.port.outbound.IndustryBenchmarkPort;
import com.practice.risk.domain.service.TaxpayerData;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class MockIndustryBenchmarkAdapter implements IndustryBenchmarkPort {

    @Override
    public TaxpayerData.BenchmarkData getBenchmarks(String industryCode) {
        return TaxpayerData.BenchmarkData.builder()
                .industryAverageProfitMargin(BigDecimal.valueOf(18))
                .industryAverageRevenueGrowth(BigDecimal.valueOf(5))
                .industryVatRatio(BigDecimal.valueOf(0.20))
                .industryRiskSectorScore(10)
                .build();
    }
}
