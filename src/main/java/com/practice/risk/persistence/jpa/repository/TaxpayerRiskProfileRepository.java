package com.practice.risk.persistence.jpa.repository;

import com.practice.risk.persistence.jpa.entity.TaxpayerRiskProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaxpayerRiskProfileRepository extends JpaRepository<TaxpayerRiskProfileEntity, UUID> {
    Optional<TaxpayerRiskProfileEntity> findByTin(String tin);
    List<TaxpayerRiskProfileEntity> findAllByOrderByCurrentRiskScoreDesc();
}
