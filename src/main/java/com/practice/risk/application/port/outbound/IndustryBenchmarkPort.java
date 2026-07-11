package com.practice.risk.application.port.outbound;

import com.practice.risk.domain.service.TaxpayerData;

public interface IndustryBenchmarkPort {
    TaxpayerData.BenchmarkData getBenchmarks(String industryCode);
}
