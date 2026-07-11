package com.practice.risk.persistence.jpa.repository;

import com.practice.risk.persistence.jpa.entity.RiskIndicatorConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RiskIndicatorConfigRepository extends JpaRepository<RiskIndicatorConfigEntity, UUID> {
    List<RiskIndicatorConfigEntity> findByIsActiveTrue();
    
    Optional<RiskIndicatorConfigEntity> findByIndicatorCode(String indicatorCode);

    @Query("SELECT DISTINCT r.category FROM RiskIndicatorConfigEntity r WHERE r.isActive = true")
    List<String> findDistinctCategories();

    List<RiskIndicatorConfigEntity> findByCategoryAndIsActiveTrue(String category);
}
