package com.practice.risk.domain.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxpayerData {
    private String tin;
    private String businessType;
    private String industryCode;
    private String location;
    private LocalDate registrationDate;

    // Filing
    private int lateFilingDays;
    private int numberOfAmendments;
    private int numberOfNonFilingPeriods;
    private Map<String, BigDecimal> declaredRevenues;

    // Payment
    private int latePaymentDays;
    private BigDecimal totalOwed;
    private BigDecimal totalPaid;
    private int numberOfLatePayments;

    // Financial
    private BigDecimal profitMargin;
    private BigDecimal revenueYearOverYearChange;
    private int consecutiveLossYears;

    // Transaction / External
    private BigDecimal importsValue;
    private BigDecimal domesticSalesValue;
    private BigDecimal inputVat;
    private BigDecimal outputVat;
    private BigDecimal cashTransactionPercentage;
    private BigDecimal relatedPartyTransactionPercentage;

    // Benchmarks
    private BigDecimal industryAverageProfitMargin;
    private BigDecimal industryAverageRevenueGrowth;
    private BigDecimal industryVatRatio;
    private int industryRiskSectorScore;

    // History
    private int previousAuditFindings;
    private boolean hasFraudHistory;
    private boolean hasUnresolvedAuditIssues;

    // Helper to build from adapters
    public static TaxpayerData fromParts(RegistrationData reg, ReturnData ret, PaymentData pay, ExternalData ext, BenchmarkData bench) {
        return TaxpayerData.builder()
                .tin(reg.getTin())
                .businessType(reg.getBusinessType())
                .industryCode(reg.getIndustryCode())
                .location(reg.getLocation())
                .registrationDate(reg.getRegistrationDate())
                .lateFilingDays(ret.getLateFilingDays())
                .numberOfAmendments(ret.getNumberOfAmendments())
                .numberOfNonFilingPeriods(ret.getNumberOfNonFilingPeriods())
                .declaredRevenues(ret.getDeclaredRevenues())
                .latePaymentDays(pay.getLatePaymentDays())
                .totalOwed(pay.getTotalOwed())
                .totalPaid(pay.getTotalPaid())
                .numberOfLatePayments(pay.getNumberOfLatePayments())
                .importsValue(ext.getImportsValue())
                .domesticSalesValue(ext.getDomesticSalesValue())
                .inputVat(ext.getInputVat())
                .outputVat(ext.getOutputVat())
                .cashTransactionPercentage(ext.getCashTransactionPercentage())
                .relatedPartyTransactionPercentage(ext.getRelatedPartyTransactionPercentage())
                .industryAverageProfitMargin(bench.getIndustryAverageProfitMargin())
                .industryAverageRevenueGrowth(bench.getIndustryAverageRevenueGrowth())
                .industryVatRatio(bench.getIndustryVatRatio())
                .industryRiskSectorScore(bench.getIndustryRiskSectorScore())
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegistrationData {
        private String tin;
        private String businessType;
        private String industryCode;
        private String location;
        private LocalDate registrationDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReturnData {
        private int lateFilingDays;
        private int numberOfAmendments;
        private int numberOfNonFilingPeriods;
        private Map<String, BigDecimal> declaredRevenues;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentData {
        private int latePaymentDays;
        private BigDecimal totalOwed;
        private BigDecimal totalPaid;
        private int numberOfLatePayments;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExternalData {
        private BigDecimal importsValue;
        private BigDecimal domesticSalesValue;
        private BigDecimal inputVat;
        private BigDecimal outputVat;
        private BigDecimal cashTransactionPercentage;
        private BigDecimal relatedPartyTransactionPercentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BenchmarkData {
        private BigDecimal industryAverageProfitMargin;
        private BigDecimal industryAverageRevenueGrowth;
        private BigDecimal industryVatRatio;
        private int industryRiskSectorScore;
    }
}
