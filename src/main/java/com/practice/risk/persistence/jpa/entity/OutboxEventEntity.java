package com.practice.risk.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "risk_outbox")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEventEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 100)
    private String eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "JSONB")
    private String payload;

    @Column(nullable = false)
    private UUID aggregateId;

    @Column(nullable = false, length = 50)
    private String aggregateType;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false)
    private Integer retryCount;

    @CreationTimestamp
    private Instant createdAt;

    private Instant publishedAt;
}
