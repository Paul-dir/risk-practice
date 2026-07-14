package com.practice.risk.infrastructure.adapter;

import com.practice.risk.application.port.outbound.IndustryBenchmarkPort;
import com.practice.risk.domain.service.TaxpayerData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Component
public class MockIndustryBenchmarkAdapter implements IndustryBenchmarkPort {

    // Industry benchmarks mapped from test data
    private static final Map<String, TaxpayerData.BenchmarkData> INDUSTRY_BENCHMARKS = Map.of(
            "Wholesale Trade", TaxpayerData.BenchmarkData.builder()
                    .industryAverageProfitMargin(BigDecimal.valueOf(12.83))
                    .industryAverageRevenueGrowth(BigDecimal.valueOf(3.15))
                    .industryVatRatio(BigDecimal.valueOf(0.84))
                    .industryRiskSectorScore(10)
                    .build(),
            "Fishing", TaxpayerData.BenchmarkData.builder()
                    .industryAverageProfitMargin(BigDecimal.valueOf(16.61))
                    .industryAverageRevenueGrowth(BigDecimal.valueOf(6.48))
                    .industryVatRatio(BigDecimal.valueOf(0.70))
                    .industryRiskSectorScore(10)
                    .build(),
            "Vehicle Repair Services", TaxpayerData.BenchmarkData.builder()
                    .industryAverageProfitMargin(BigDecimal.valueOf(5.54))
                    .industryAverageRevenueGrowth(BigDecimal.valueOf(5.04))
                    .industryVatRatio(BigDecimal.valueOf(0.61))
                    .industryRiskSectorScore(10)
                    .build(),
            "Professional Services", TaxpayerData.BenchmarkData.builder()
                    .industryAverageProfitMargin(BigDecimal.valueOf(13.53))
                    .industryAverageRevenueGrowth(BigDecimal.valueOf(7.57))
                    .industryVatRatio(BigDecimal.valueOf(0.75))
                    .industryRiskSectorScore(15)
                    .build()
    );

    @Override
    public TaxpayerData.BenchmarkData getBenchmarks(String industryCode) {
        TaxpayerData.BenchmarkData benchmark = INDUSTRY_BENCHMARKS.get(industryCode);
        
        if (benchmark != null) {
            log.info("Using industry benchmark for: {}", industryCode);
            return benchmark;
        }
        
        // Fallback to default
        log.warn("No benchmark found for industry: {}, using default", industryCode);
        return TaxpayerData.BenchmarkData.builder()
                .industryAverageProfitMargin(BigDecimal.valueOf(18))
                .industryAverageRevenueGrowth(BigDecimal.valueOf(5))
                .industryVatRatio(BigDecimal.valueOf(0.20))
                .industryRiskSectorScore(10)
                .build();
    }
}
