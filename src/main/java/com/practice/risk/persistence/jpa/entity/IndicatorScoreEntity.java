package com.practice.risk.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "assessment_indicator_scores")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndicatorScoreEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false)
    private RiskAssessmentEntity assessment;
    
    @Column(nullable = false, length = 50)
    private String indicatorCode;
    
    @Column(nullable = false, length = 200)
    private String indicatorName;
    
    @Column(nullable = false, length = 50)
    private String category;
    
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal score;
    
    @Column(nullable = false, precision = 4, scale = 3)
    private BigDecimal weight;
    
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal contribution;
    
    @Column(length = 200)
    private String actualValue;
    
    @Column(columnDefinition = "TEXT")
    private String explanation;
    
    private Instant createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
