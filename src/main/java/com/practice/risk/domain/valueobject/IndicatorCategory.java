package com.practice.risk.domain.valueobject;

/**
 * Risk Indicator Categories
 * 
 * Groups risk indicators into logical categories for analysis and reporting.
 */
public enum IndicatorCategory {
    FILING("Filing Compliance"),
    PAYMENT("Payment Compliance"),
    FINANCIAL("Financial Health"),
    TRANSACTION("Transaction Analysis"),
    BEHAVIORAL("Behavioral Patterns"),
    INDUSTRY("Industry Context");
    
    private final String displayName;
    
    IndicatorCategory(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
