package com.practice.risk.persistence.jpa.repository;

import com.practice.risk.persistence.jpa.entity.OutboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity, UUID> {
    List<OutboxEventEntity> findTop100ByStatusOrderByCreatedAtAsc(String status);
}
