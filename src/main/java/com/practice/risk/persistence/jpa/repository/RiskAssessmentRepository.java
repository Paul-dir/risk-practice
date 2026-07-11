package com.practice.risk.persistence.jpa.repository;

import com.practice.risk.persistence.jpa.entity.RiskAssessmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RiskAssessmentRepository extends JpaRepository<RiskAssessmentEntity, UUID> {
    List<RiskAssessmentEntity> findByTaxpayerIdOrderByAssessmentDateDesc(UUID taxpayerId);

    @Query("SELECT r FROM RiskAssessmentEntity r ORDER BY r.overallScore DESC")
    List<RiskAssessmentEntity> findAllByOrderByOverallScoreDesc();

    @Query(value = "SELECT * FROM risk_assessments ORDER BY overall_score DESC LIMIT :limit", nativeQuery = true)
    List<RiskAssessmentEntity> findTopNByOverallScore(int limit);
    
    @Query("SELECT r FROM RiskAssessmentEntity r " +
           "LEFT JOIN FETCH r.categoryScores " +
           "LEFT JOIN FETCH r.indicatorScores " +
           "WHERE r.id = :id")
    Optional<RiskAssessmentEntity> findByIdWithScores(@Param("id") UUID id);
}
