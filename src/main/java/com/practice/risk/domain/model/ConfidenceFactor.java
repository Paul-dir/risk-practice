package com.practice.risk.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfidenceFactor {
    private BigDecimal dataAvailability;
    private BigDecimal dataQuality;
    private BigDecimal sourceCount;
    private BigDecimal temporalConsistency;

    public BigDecimal getTotal() {
        return dataAvailability.add(dataQuality)
                .add(sourceCount)
                .add(temporalConsistency)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
