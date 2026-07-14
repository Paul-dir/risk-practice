package com.practice.risk.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "risk_indicator_config")  // Fixed: singular to match database table
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskIndicatorConfigEntity {

    @Id
    private UUID id;

    @Column(unique = true, nullable = false, length = 50)
    private String indicatorCode;

    @Column(nullable = false, length = 100)
    private String indicatorName;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal weight;

    @Column(precision = 10, scale = 2)
    private BigDecimal minThreshold;

    @Column(precision = 10, scale = 2)
    private BigDecimal maxThreshold;

    @Column(length = 20)
    private String scoringFormula;

    @Column(nullable = false)
    private Boolean isActive;

    @Column(length = 50)
    private String dataSource;

    @Column(nullable = false)
    private Integer configVersion;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
