package com.practice.risk.persistence.jpa.repository;

import com.practice.risk.persistence.jpa.entity.RiskAuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RiskAuditLogRepository extends JpaRepository<RiskAuditLogEntity, UUID> {
}
