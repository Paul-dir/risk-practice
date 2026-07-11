package com.practice.risk.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "risk_assessments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskAssessmentEntity {
    @Id
    private UUID id;
    private UUID taxpayerId;
    private String tin;
    private Instant assessmentDate;
    private BigDecimal overallScore;
    private String riskLevel;
    private BigDecimal confidenceFactor;
    // ❌ REMOVED: recommendedAuditType - This is Audit Module's responsibility, not Risk Engine's
    private Integer priorityRank;
    private String status;
    private Integer configVersion;  // Renamed from 'version' for clarity
    @Column(columnDefinition = "TEXT")
    private String overrideJustification;
    private UUID overriddenBy;
    private Instant overriddenAt;
    
    @OneToMany(mappedBy = "assessment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CategoryScoreEntity> categoryScores = new ArrayList<>();
    
    @OneToMany(mappedBy = "assessment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<IndicatorScoreEntity> indicatorScores = new ArrayList<>();
}
