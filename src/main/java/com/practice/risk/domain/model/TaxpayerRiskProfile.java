package com.practice.risk.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import com.practice.risk.domain.valueobject.RiskLevel;
import com.practice.risk.domain.valueobject.RiskTrend;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxpayerRiskProfile {
    private UUID taxpayerId;
    private String tin;
    private BigDecimal currentRiskScore;
    private RiskLevel currentRiskLevel;
    private Instant lastAssessmentDate;
    private RiskTrend riskTrend;
    private List<HistoricalScore> previousScores;
    private Integer configVersion;
}
