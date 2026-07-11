package com.practice.risk.domain.service;

import com.practice.risk.domain.model.*;
import com.practice.risk.domain.valueobject.IndicatorCategory;
import com.practice.risk.domain.valueobject.RiskLevel;
import com.practice.risk.domain.valueobject.AssessmentStatus;
import com.practice.risk.infrastructure.config.IndicatorConfigurationService;
import com.practice.risk.persistence.jpa.entity.RiskIndicatorConfigEntity;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@Slf4j
public class RiskScoringService {

    // Default weights if config is missing (fallback from TDD)
    private static final double DEFAULT_FILING_WEIGHT = 0.25;
    private static final double DEFAULT_PAYMENT_WEIGHT = 0.25;
    private static final double DEFAULT_FINANCIAL_WEIGHT = 0.20;
    private static final double DEFAULT_TRANSACTION_WEIGHT = 0.15;
    private static final double DEFAULT_BEHAVIORAL_WEIGHT = 0.10;
    private static final double DEFAULT_INDUSTRY_WEIGHT = 0.05;

    public RiskAssessment assess(UUID taxpayerId, String tin, TaxpayerData data,
                                 IndicatorConfigurationService configService) {
        // 1. Load configurations
        List<RiskIndicatorConfigEntity> configs = configService.getAllActiveIndicators();
        Map<String, RiskIndicatorConfigEntity> configMap = configs.stream()
                .collect(Collectors.toMap(RiskIndicatorConfigEntity::getIndicatorCode, c -> c));

        // 2. Evaluate each indicator using config
        List<IndicatorScore> indicators = evaluateIndicators(data, configMap);

        // 3. Aggregate categories
        List<CategoryScore> categories = aggregateCategories(indicators);

        // 4. Overall score
        BigDecimal overall = computeOverallScore(categories);

        // 5. Risk level
        RiskLevel level = RiskLevel.fromScore(overall.doubleValue());

        // 6. Confidence
        ConfidenceFactor confidence = computeConfidence(data);

        // Build assessment with ONLY risk data
        return RiskAssessment.builder()
                .id(UUID.randomUUID())
                .taxpayerId(taxpayerId)
                .tin(tin)
                .assessmentDate(Instant.now())
                .overallScore(overall)
                .riskLevel(level)
                .confidenceFactor(confidence)
                .priorityRank(0)  // Will be set by prioritization service
                .categoryScores(categories)
                .indicatorScores(indicators)
                .status(AssessmentStatus.COMPLETED)
                .configVersion(1)
                .build();
    }

    private List<IndicatorScore> evaluateIndicators(TaxpayerData d, Map<String, RiskIndicatorConfigEntity> configMap) {
        List<IndicatorScore> list = new ArrayList<>();

        // Helper: get config or use default
        java.util.function.Function<String, RiskIndicatorConfigEntity> getConfig = (code) ->
                configMap.getOrDefault(code, RiskIndicatorConfigEntity.builder()
                        .weight(BigDecimal.valueOf(0.10))
                        .minThreshold(BigDecimal.ZERO)
                        .maxThreshold(BigDecimal.valueOf(100))
                        .scoringFormula("LINEAR")
                        .build());

        // ---- LATE_FILING ----
        RiskIndicatorConfigEntity cfg = getConfig.apply("LATE_FILING");
        int days = d.getLateFilingDays();
        double score = 0;
        if (days > 180) score = 50;
        else if (days > 90) score = 30;
        else if (days > 30) score = 15;
        else if (days > 0) score = 5;
        list.add(buildIndicator("LATE_FILING", "Late Filing", IndicatorCategory.FILING, score, cfg.getWeight(), days + " days",
                "Filing was " + days + " days late."));

        // ---- MULTIPLE_AMENDMENTS ----
        cfg = getConfig.apply("MULTIPLE_AMENDMENTS");
        int amd = d.getNumberOfAmendments();
        double amdScore = 0;
        if (amd > 3) amdScore = 30;
        else if (amd > 2) amdScore = 15;
        else if (amd > 0) amdScore = 5;
        list.add(buildIndicator("MULTIPLE_AMENDMENTS", "Multiple Amendments", IndicatorCategory.FILING, amdScore, cfg.getWeight(),
                amd + " amendments", "Return amended " + amd + " times."));

        // ---- NON_FILING ----
        cfg = getConfig.apply("NON_FILING");
        int nf = d.getNumberOfNonFilingPeriods();
        double nfScore = Math.min(nf * 20, 100);
        list.add(buildIndicator("NON_FILING", "Non-Filing", IndicatorCategory.FILING, nfScore, cfg.getWeight(),
                nf + " periods", "Missing filings for " + nf + " periods."));

        // ---- LATE_PAYMENT ----
        cfg = getConfig.apply("LATE_PAYMENT");
        int payDays = d.getLatePaymentDays();
        double payScore = 0;
        if (payDays > 180) payScore = 50;
        else if (payDays > 90) payScore = 30;
        else if (payDays > 30) payScore = 15;
        else if (payDays > 0) payScore = 5;
        list.add(buildIndicator("LATE_PAYMENT", "Late Payment", IndicatorCategory.PAYMENT, payScore, cfg.getWeight(),
                payDays + " days", "Payment " + payDays + " days late."));

        // ---- PARTIAL_PAYMENT ----
        cfg = getConfig.apply("PARTIAL_PAYMENT");
        BigDecimal owed = d.getTotalOwed();
        BigDecimal paid = d.getTotalPaid();
        double partialScore = 0;
        if (owed != null && owed.compareTo(BigDecimal.ZERO) > 0 && paid != null) {
            double pct = paid.divide(owed, 4, RoundingMode.HALF_UP).doubleValue() * 100;
            if (pct < 50) partialScore = 40;
            else if (pct < 75) partialScore = 25;
            else if (pct < 99) partialScore = 10;
            list.add(buildIndicator("PARTIAL_PAYMENT", "Partial Payment", IndicatorCategory.PAYMENT, partialScore, cfg.getWeight(),
                    String.format("%.1f%% paid", pct), "Only " + String.format("%.1f", pct) + "% paid."));
        }

        // ---- CONTINUOUS_LOSSES ----
        cfg = getConfig.apply("CONTINUOUS_LOSSES");
        int losses = d.getConsecutiveLossYears();
        double lossScore = 0;
        if (losses >= 5) lossScore = 45;
        else if (losses >= 3) lossScore = 25;
        else if (losses >= 1) lossScore = 10;
        list.add(buildIndicator("CONTINUOUS_LOSSES", "Continuous Losses", IndicatorCategory.FINANCIAL, lossScore, cfg.getWeight(),
                losses + " years", losses + " consecutive loss years."));

        // ---- RAPID_REVENUE_DECLINE ----
        cfg = getConfig.apply("RAPID_REVENUE_DECLINE");
        double revChg = d.getRevenueYearOverYearChange() != null ? d.getRevenueYearOverYearChange().doubleValue() : 0;
        double revScore = 0;
        if (revChg < -50) revScore = 35;
        else if (revChg < -20) revScore = 15;
        list.add(buildIndicator("RAPID_REVENUE_DECLINE", "Rapid Revenue Decline", IndicatorCategory.FINANCIAL, revScore, cfg.getWeight(),
                String.format("%.1f%%", revChg), "Revenue change: " + String.format("%.1f", revChg) + "%."));

        // ---- IMPORT_SALES_MISMATCH ----
        cfg = getConfig.apply("IMPORT_SALES_MISMATCH");
        double impSales = 0;
        if (d.getImportsValue() != null && d.getImportsValue().compareTo(BigDecimal.ZERO) > 0 &&
            d.getDomesticSalesValue() != null && d.getDomesticSalesValue().compareTo(BigDecimal.ZERO) > 0) {
            impSales = d.getImportsValue().divide(d.getDomesticSalesValue(), 4, RoundingMode.HALF_UP).doubleValue() * 100;
        }
        double impScore = 0;
        if (impSales > 20) impScore = 30;
        else if (impSales > 5) impScore = 10;
        list.add(buildIndicator("IMPORT_SALES_MISMATCH", "Import vs Sales Mismatch", IndicatorCategory.TRANSACTION, impScore, cfg.getWeight(),
                String.format("%.1f%%", impSales), "Imports are " + String.format("%.1f", impSales) + "% of sales."));

        // ---- RELATED_PARTY_TRANSACTIONS ----
        cfg = getConfig.apply("RELATED_PARTY_TRANSACTIONS");
        double rp = d.getRelatedPartyTransactionPercentage() != null ? d.getRelatedPartyTransactionPercentage().doubleValue() : 0;
        double rpScore = 0;
        if (rp > 50) rpScore = 35;
        else if (rp > 30) rpScore = 20;
        else if (rp > 10) rpScore = 10;
        list.add(buildIndicator("RELATED_PARTY_TRANSACTIONS", "Related-Party Transactions", IndicatorCategory.TRANSACTION, rpScore, cfg.getWeight(),
                String.format("%.1f%%", rp), rp + "% related-party transactions."));

        // ---- PREVIOUS_FRAUD ----
        if (d.isHasFraudHistory()) {
            cfg = getConfig.apply("PREVIOUS_FRAUD");
            list.add(buildIndicator("PREVIOUS_FRAUD", "Previous Fraud History", IndicatorCategory.BEHAVIORAL, 75, cfg.getWeight(),
                    "Yes", "Taxpayer has prior fraud history."));
        }

        // ---- SHORT_BUSINESS_LIFE ----
        if (d.getRegistrationDate() != null) {
            cfg = getConfig.apply("SHORT_BUSINESS_LIFE");
            long years = java.time.temporal.ChronoUnit.YEARS.between(d.getRegistrationDate(), java.time.LocalDate.now());
            double shortScore = years < 2 ? 15 : (years < 5 ? 5 : 0);
            list.add(buildIndicator("SHORT_BUSINESS_LIFE", "Short Business Life", IndicatorCategory.BEHAVIORAL, shortScore, cfg.getWeight(),
                    years + " years", "Business age: " + years + " years."));
        }

        // ---- SECTOR_SPECIFIC_RISK ----
        if (d.getIndustryRiskSectorScore() > 0) {
            cfg = getConfig.apply("SECTOR_SPECIFIC_RISK");
            list.add(buildIndicator("SECTOR_SPECIFIC_RISK", "Sector-Specific Risk", IndicatorCategory.INDUSTRY,
                    Math.min(d.getIndustryRiskSectorScore(), 15), cfg.getWeight(),
                    "Score: " + d.getIndustryRiskSectorScore(), "High-risk sector."));
        }

        // ---- INDUSTRY_DEVIATION ----
        if (d.getIndustryAverageProfitMargin() != null && d.getProfitMargin() != null &&
            d.getIndustryAverageProfitMargin().compareTo(BigDecimal.ZERO) > 0) {
            cfg = getConfig.apply("INDUSTRY_DEVIATION");
            double dev = d.getProfitMargin().divide(d.getIndustryAverageProfitMargin(), 4, RoundingMode.HALF_UP).doubleValue() * 100;
            double devScore = 0;
            if (dev > 300) devScore = 35;
            else if (dev > 200) devScore = 20;
            list.add(buildIndicator("INDUSTRY_DEVIATION", "Industry Deviation", IndicatorCategory.INDUSTRY, devScore, cfg.getWeight(),
                    String.format("%.1f%% of avg", dev), "Margin deviates from industry norm."));
        }

        return list;
    }

    private IndicatorScore buildIndicator(String code, String name, IndicatorCategory cat,
                                          double score, BigDecimal weight, String actual, String exp) {
        double w = weight != null ? weight.doubleValue() : 0.10;
        return IndicatorScore.builder()
                .indicatorCode(code)
                .indicatorName(name)
                .category(cat)
                .score(BigDecimal.valueOf(Math.min(score, 100)).setScale(2, RoundingMode.HALF_UP))
                .weight(BigDecimal.valueOf(w))
                .contribution(BigDecimal.valueOf(Math.min(score, 100) * w / 100).setScale(2, RoundingMode.HALF_UP))
                .actualValue(actual)
                .explanation(exp)
                .build();
    }

    private List<CategoryScore> aggregateCategories(List<IndicatorScore> indicators) {
        Map<IndicatorCategory, List<IndicatorScore>> byCat = indicators.stream()
                .collect(Collectors.groupingBy(IndicatorScore::getCategory));

        Map<IndicatorCategory, Double> defaultWeights = Map.of(
                IndicatorCategory.FILING, DEFAULT_FILING_WEIGHT,
                IndicatorCategory.PAYMENT, DEFAULT_PAYMENT_WEIGHT,
                IndicatorCategory.FINANCIAL, DEFAULT_FINANCIAL_WEIGHT,
                IndicatorCategory.TRANSACTION, DEFAULT_TRANSACTION_WEIGHT,
                IndicatorCategory.BEHAVIORAL, DEFAULT_BEHAVIORAL_WEIGHT,
                IndicatorCategory.INDUSTRY, DEFAULT_INDUSTRY_WEIGHT
        );

        List<CategoryScore> result = new ArrayList<>();
        for (Map.Entry<IndicatorCategory, List<IndicatorScore>> entry : byCat.entrySet()) {
            IndicatorCategory cat = entry.getKey();
            List<IndicatorScore> items = entry.getValue();

            double totalContribution = items.stream().mapToDouble(i -> i.getContribution().doubleValue()).sum();
            double totalWeight = items.stream().mapToDouble(i -> i.getWeight().doubleValue()).sum();
            double normalizedScore = totalWeight > 0 ? (totalContribution / totalWeight) : 0;

            double catWeight = defaultWeights.getOrDefault(cat, 0.0);
            result.add(CategoryScore.builder()
                    .category(cat)
                    .score(BigDecimal.valueOf(Math.min(normalizedScore, 100)).setScale(2, RoundingMode.HALF_UP))
                    .weight(BigDecimal.valueOf(catWeight))
                    .contribution(BigDecimal.valueOf(normalizedScore * catWeight / 100).setScale(2, RoundingMode.HALF_UP))
                    .build());
        }
        return result;
    }

    private BigDecimal computeOverallScore(List<CategoryScore> categories) {
        double total = categories.stream().mapToDouble(c -> c.getContribution().doubleValue()).sum();
        return BigDecimal.valueOf(Math.min(total, 100)).setScale(2, RoundingMode.HALF_UP);
    }

    private ConfidenceFactor computeConfidence(TaxpayerData data) {
        // TODO: Implement proper confidence calculation based on:
        // - Data availability
        // - Data quality  
        // - Number of sources
        // - Temporal consistency
        return ConfidenceFactor.builder()
                .dataAvailability(BigDecimal.valueOf(0.3))
                .dataQuality(BigDecimal.valueOf(0.3))
                .sourceCount(BigDecimal.valueOf(0.2))
                .temporalConsistency(BigDecimal.valueOf(0.2))
                .build();
    }
}
