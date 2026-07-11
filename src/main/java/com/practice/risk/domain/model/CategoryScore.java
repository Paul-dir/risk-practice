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
public class CategoryScore {
    private IndicatorCategory category;
    private BigDecimal score;
    private BigDecimal weight;
    private BigDecimal contribution;
}
