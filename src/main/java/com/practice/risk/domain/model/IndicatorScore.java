package com.practice.risk.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import com.practice.risk.domain.valueobject.IndicatorCategory;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndicatorScore {
    private String indicatorCode;
    private String indicatorName;
    private IndicatorCategory category;
    private BigDecimal score;
    private BigDecimal weight;
    private BigDecimal contribution;
    private BigDecimal thresholdUsed;
    private String actualValue;
    private String explanation;
}
