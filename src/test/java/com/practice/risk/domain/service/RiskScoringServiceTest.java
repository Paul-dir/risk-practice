package com.practice.risk.domain.service;

import com.practice.risk.domain.model.RiskAssessment;
import com.practice.risk.domain.valueobject.RiskLevel;
import com.practice.risk.infrastructure.config.IndicatorConfigurationService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

import org.mockito.Mockito;
import java.util.List;

class RiskScoringServiceTest {

    private final RiskScoringService service = new RiskScoringService();
    private final IndicatorConfigurationService configService = Mockito.mock(IndicatorConfigurationService.class);

    @Test
    void shouldReturnCriticalRiskForHighRiskTaxpayer() {
        TaxpayerData data = TaxpayerData.builder()
                .tin("TIN123")
                .lateFilingDays(200)
                .numberOfAmendments(5)
                .numberOfNonFilingPeriods(2)
                .latePaymentDays(200)
                .totalOwed(BigDecimal.valueOf(1_000_000))
                .totalPaid(BigDecimal.valueOf(100_000))
                .numberOfLatePayments(10)
                .consecutiveLossYears(6)
                .revenueYearOverYearChange(BigDecimal.valueOf(-60))
                .relatedPartyTransactionPercentage(BigDecimal.valueOf(60))
                .hasFraudHistory(true)
                .registrationDate(LocalDate.now().minusYears(1))
                .industryRiskSectorScore(15)
                .build();

        Mockito.when(configService.getAllActiveIndicators()).thenReturn(List.of());

        RiskAssessment result = service.assess(UUID.randomUUID(), "TIN123", data, configService);

        assertNotNull(result);
        assertEquals(RiskLevel.CRITICAL, result.getRiskLevel());
        assertTrue(result.getOverallScore().doubleValue() >= 80);
    }

    @Test
    void shouldReturnLowRiskForCompliantTaxpayer() {
        TaxpayerData data = TaxpayerData.builder()
                .tin("TIN456")
                .lateFilingDays(0)
                .numberOfAmendments(0)
                .numberOfNonFilingPeriods(0)
                .latePaymentDays(0)
                .totalOwed(BigDecimal.valueOf(100_000))
                .totalPaid(BigDecimal.valueOf(100_000))
                .numberOfLatePayments(0)
                .consecutiveLossYears(0)
                .revenueYearOverYearChange(BigDecimal.valueOf(10))
                .relatedPartyTransactionPercentage(BigDecimal.valueOf(5))
                .hasFraudHistory(false)
                .registrationDate(LocalDate.now().minusYears(10))
                .industryRiskSectorScore(0)
                .build();

        Mockito.when(configService.getAllActiveIndicators()).thenReturn(List.of());

        RiskAssessment result = service.assess(UUID.randomUUID(), "TIN456", data, configService);

        assertNotNull(result);
        assertEquals(RiskLevel.LOW, result.getRiskLevel());
        assertTrue(result.getOverallScore().doubleValue() < 40);
    }
}
