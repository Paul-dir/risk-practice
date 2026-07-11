package com.practice.risk.domain.valueobject;

public enum RiskLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL;

    public static RiskLevel fromScore(double score) {
        if (score >= 80.0) return CRITICAL;
        if (score >= 65.0) return HIGH;
        if (score >= 40.0) return MEDIUM;
        return LOW;
    }
}
