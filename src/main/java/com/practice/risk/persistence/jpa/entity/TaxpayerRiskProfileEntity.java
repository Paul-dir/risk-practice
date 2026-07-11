package com.practice.risk.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "risk_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxpayerRiskProfileEntity {

    @Id
    private UUID taxpayerId;

    @Column(nullable = false, length = 50)
    private String tin;

    @Column(precision = 5, scale = 2)
    private BigDecimal currentRiskScore;

    @Column(length = 20)
    private String currentRiskLevel;

    private Instant lastAssessmentDate;

    @Column(length = 20)
    private String riskTrend;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSONB")
    private String previousScores;

    private Integer configVersion;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
