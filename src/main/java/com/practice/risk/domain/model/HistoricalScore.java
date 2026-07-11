package com.practice.risk.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.Instant;
import com.practice.risk.domain.valueobject.RiskLevel;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoricalScore {
    private Instant assessmentDate;
    private BigDecimal overallScore;
    private RiskLevel riskLevel;
}
