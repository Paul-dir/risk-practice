# Central Database Implementation Guide

## Complete Schema for Tax Audit System with Risk Engine

This schema stores ALL data in ONE central database that both Tax Audit and Risk Engine modules can access.

## Database Schema

### 1. Taxpayer Master Data

```sql
-- ============================================================================
-- TAXPAYER MASTER DATA
-- ============================================================================

CREATE TABLE taxpayers (
    taxpayer_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tin VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    
    -- Business Information
    business_type VARCHAR(50), -- COMPANY, PARTNERSHIP, SOLE_PROPRIETOR
    industry_code VARCHAR(50),
    industry_name VARCHAR(100),
    registration_date DATE NOT NULL,
    is_active BOOLEAN DEFAULT true,
    
    -- Contact Information
    email VARCHAR(255),
    phone VARCHAR(50),
    
    -- Address
    region VARCHAR(50),
    city VARCHAR(100),
    woreda VARCHAR(50),
    kebele VARCHAR(50),
    house_number VARCHAR(50),
    
    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

CREATE INDEX idx_taxpayers_tin ON taxpayers(tin);
CREATE INDEX idx_taxpayers_industry ON taxpayers(industry_code);
CREATE INDEX idx_taxpayers_active ON taxpayers(is_active);

COMMENT ON TABLE taxpayers IS 'Master taxpayer registry';
```

### 2. Tax Returns / Filing Data

```sql
-- ============================================================================
-- TAX RETURNS / FILING DATA
-- ============================================================================

CREATE TABLE tax_returns (
    return_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    taxpayer_id UUID NOT NULL REFERENCES taxpayers(taxpayer_id),
    
    -- Return Details
    return_type VARCHAR(50) NOT NULL, -- MONTHLY_VAT, ANNUAL_INCOME, WITHHOLDING
    tax_year INTEGER NOT NULL,
    tax_period VARCHAR(20), -- 2026-01, 2026-Q1, 2026
    
    -- Dates
    due_date DATE NOT NULL,
    filed_date DATE,
    submission_method VARCHAR(50), -- ONLINE, MANUAL, AGENT
    
    -- Status
    status VARCHAR(50) NOT NULL, -- FILED_ON_TIME, FILED_LATE, NOT_FILED, NIL_RETURN
    days_late INTEGER DEFAULT 0,
    
    -- Financial Data
    gross_revenue DECIMAL(15,2),
    taxable_income DECIMAL(15,2),
    tax_amount DECIMAL(15,2),
    is_nil_return BOOLEAN DEFAULT false,
    is_amended BOOLEAN DEFAULT false,
    original_return_id UUID REFERENCES tax_returns(return_id),
    
    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    filed_by UUID
);

CREATE INDEX idx_returns_taxpayer ON tax_returns(taxpayer_id);
CREATE INDEX idx_returns_period ON tax_returns(tax_year, tax_period);
CREATE INDEX idx_returns_status ON tax_returns(status);
CREATE INDEX idx_returns_late ON tax_returns(taxpayer_id, days_late) WHERE days_late > 0;

COMMENT ON TABLE tax_returns IS 'All tax return filings';
```

### 3. Payment History

```sql
-- ============================================================================
-- PAYMENT HISTORY
-- ============================================================================

CREATE TABLE payments (
    payment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    taxpayer_id UUID NOT NULL REFERENCES taxpayers(taxpayer_id),
    return_id UUID REFERENCES tax_returns(return_id),
    
    -- Payment Details
    amount DECIMAL(15,2) NOT NULL,
    payment_date DATE NOT NULL,
    payment_method VARCHAR(50), -- BANK_TRANSFER, CASH, CHECK, MOBILE_MONEY
    reference_number VARCHAR(100),
    
    -- Tax Period
    tax_period VARCHAR(20),
    tax_type VARCHAR(50), -- VAT, INCOME_TAX, WITHHOLDING
    
    -- Status
    status VARCHAR(50) NOT NULL, -- CONFIRMED, PENDING, REJECTED, REVERSED
    days_late INTEGER DEFAULT 0,
    penalty_amount DECIMAL(15,2) DEFAULT 0,
    interest_amount DECIMAL(15,2) DEFAULT 0,
    
    -- Bank Details
    bank_name VARCHAR(100),
    bank_branch VARCHAR(100),
    
    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    verified_by UUID,
    verified_at TIMESTAMP
);

CREATE INDEX idx_payments_taxpayer ON payments(taxpayer_id);
CREATE INDEX idx_payments_date ON payments(payment_date DESC);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_late ON payments(taxpayer_id, days_late) WHERE days_late > 0;

COMMENT ON TABLE payments IS 'All payment transactions';
```

### 4. Outstanding Balances

```sql
-- ============================================================================
-- OUTSTANDING BALANCES
-- ============================================================================

CREATE TABLE outstanding_balances (
    balance_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    taxpayer_id UUID NOT NULL REFERENCES taxpayers(taxpayer_id),
    return_id UUID REFERENCES tax_returns(return_id),
    
    -- Balance Details
    original_amount DECIMAL(15,2) NOT NULL,
    paid_amount DECIMAL(15,2) DEFAULT 0,
    outstanding_amount DECIMAL(15,2) NOT NULL,
    
    -- Tax Details
    tax_period VARCHAR(20),
    tax_type VARCHAR(50),
    assessment_date DATE,
    due_date DATE,
    
    -- Aging
    days_overdue INTEGER,
    aging_category VARCHAR(50), -- CURRENT, 30_DAYS, 60_DAYS, 90_DAYS, 120_PLUS
    
    -- Status
    status VARCHAR(50), -- OUTSTANDING, PAID, WRITTEN_OFF, IN_DISPUTE
    
    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_balances_taxpayer ON outstanding_balances(taxpayer_id);
CREATE INDEX idx_balances_overdue ON outstanding_balances(days_overdue) WHERE status = 'OUTSTANDING';

COMMENT ON TABLE outstanding_balances IS 'Outstanding tax liabilities';
```

### 5. External Data

```sql
-- ============================================================================
-- EXTERNAL DATA (Credit Scores, Sanctions, etc.)
-- ============================================================================

CREATE TABLE external_data (
    external_data_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    taxpayer_id UUID NOT NULL REFERENCES taxpayers(taxpayer_id),
    
    -- Credit Information
    credit_score INTEGER,
    credit_rating VARCHAR(50), -- EXCELLENT, GOOD, FAIR, POOR
    credit_bureau_name VARCHAR(100),
    credit_check_date DATE,
    
    -- Sanctions & Watchlists
    sanctions_listed BOOLEAN DEFAULT false,
    sanctions_details TEXT,
    sanctions_check_date DATE,
    
    -- Business Licenses
    trade_license_number VARCHAR(100),
    trade_license_expiry DATE,
    trade_license_status VARCHAR(50),
    
    -- Other Registrations
    customs_registered BOOLEAN DEFAULT false,
    vat_registered BOOLEAN DEFAULT false,
    withholding_agent BOOLEAN DEFAULT false,
    
    -- Audit
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by UUID
);

CREATE INDEX idx_external_taxpayer ON external_data(taxpayer_id);

COMMENT ON TABLE external_data IS 'External data from third-party sources';
```

### 6. Previous Audit History

```sql
-- ============================================================================
-- PREVIOUS AUDIT HISTORY
-- ============================================================================

CREATE TABLE audit_history (
    audit_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    taxpayer_id UUID NOT NULL REFERENCES taxpayers(taxpayer_id),
    
    -- Audit Details
    audit_type VARCHAR(50), -- DESK, FIELD, COMPREHENSIVE, SPECIAL
    audit_period_start DATE,
    audit_period_end DATE,
    
    -- Dates
    initiated_date DATE,
    completed_date DATE,
    
    -- Findings
    findings_category VARCHAR(50), -- NO_ISSUES, MINOR_ISSUES, MAJOR_ISSUES, FRAUD
    findings_description TEXT,
    
    -- Financial Impact
    additional_tax_assessed DECIMAL(15,2),
    penalties_assessed DECIMAL(15,2),
    amount_collected DECIMAL(15,2),
    
    -- Resolution
    status VARCHAR(50), -- OPEN, CLOSED, IN_APPEAL, SETTLED
    
    -- Auditor
    assigned_auditor UUID,
    auditor_name VARCHAR(255),
    
    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_history_taxpayer ON audit_history(taxpayer_id);
CREATE INDEX idx_audit_history_date ON audit_history(completed_date DESC);

COMMENT ON TABLE audit_history IS 'Historical audit records';
```

### 7. Risk Assessments (Risk Engine Output)

```sql
-- ============================================================================
-- RISK ASSESSMENTS (Risk Engine owns this)
-- ============================================================================

CREATE TABLE risk_assessments (
    assessment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    taxpayer_id UUID NOT NULL REFERENCES taxpayers(taxpayer_id),
    tin VARCHAR(20) NOT NULL,
    
    -- Assessment Results
    overall_score DECIMAL(5,2) NOT NULL,
    risk_level VARCHAR(20) NOT NULL, -- LOW, MEDIUM, HIGH, CRITICAL
    confidence_factor DECIMAL(3,2),
    priority_rank INTEGER,
    
    -- Dates
    assessment_date TIMESTAMP NOT NULL,
    
    -- Status
    status VARCHAR(50) DEFAULT 'COMPLETED', -- PENDING, IN_PROGRESS, COMPLETED, OVERRIDDEN
    
    -- Configuration
    config_version INTEGER,
    
    -- Override
    override_justification TEXT,
    overridden_by UUID,
    overridden_at TIMESTAMP,
    
    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_assessments_taxpayer ON risk_assessments(taxpayer_id);
CREATE INDEX idx_assessments_level ON risk_assessments(risk_level, assessment_date DESC);
CREATE INDEX idx_assessments_score ON risk_assessments(overall_score DESC);
CREATE INDEX idx_assessments_date ON risk_assessments(assessment_date DESC);

COMMENT ON TABLE risk_assessments IS 'Risk Engine assessment results';
```

### 8. Assessment Details (Category & Indicator Scores)

```sql
-- ============================================================================
-- ASSESSMENT CATEGORY SCORES
-- ============================================================================

CREATE TABLE assessment_category_scores (
    score_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    assessment_id UUID NOT NULL REFERENCES risk_assessments(assessment_id) ON DELETE CASCADE,
    
    category VARCHAR(100) NOT NULL,
    score DECIMAL(5,2) NOT NULL,
    weight DECIMAL(3,2),
    contribution DECIMAL(5,2),
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_category_scores_assessment ON assessment_category_scores(assessment_id);

-- ============================================================================
-- ASSESSMENT INDICATOR SCORES
-- ============================================================================

CREATE TABLE assessment_indicator_scores (
    score_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    assessment_id UUID NOT NULL REFERENCES risk_assessments(assessment_id) ON DELETE CASCADE,
    
    indicator_code VARCHAR(100) NOT NULL,
    indicator_name VARCHAR(255),
    score DECIMAL(5,2) NOT NULL,
    data_value TEXT,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_indicator_scores_assessment ON assessment_indicator_scores(assessment_id);
CREATE INDEX idx_indicator_scores_code ON assessment_indicator_scores(indicator_code);
```

### 9. Audit Cases (Tax Audit Module owns this)

```sql
-- ============================================================================
-- AUDIT CASES (Tax Audit Module owns this)
-- ============================================================================

CREATE TABLE audit_cases (
    case_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    taxpayer_id UUID NOT NULL REFERENCES taxpayers(taxpayer_id),
    assessment_id UUID REFERENCES risk_assessments(assessment_id),
    
    -- Case Details
    case_number VARCHAR(50) UNIQUE NOT NULL,
    audit_type VARCHAR(50) NOT NULL, -- DESK, FIELD, COMPREHENSIVE
    priority VARCHAR(20), -- LOW, MEDIUM, HIGH, URGENT
    
    -- Period
    audit_period_start DATE,
    audit_period_end DATE,
    
    -- Status
    status VARCHAR(50) NOT NULL, -- OPEN, ASSIGNED, IN_PROGRESS, COMPLETED, CLOSED
    
    -- Assignment
    assigned_to UUID,
    assigned_date DATE,
    team_lead UUID,
    
    -- Dates
    initiated_date DATE,
    target_completion_date DATE,
    actual_completion_date DATE,
    
    -- Results
    findings TEXT,
    additional_tax DECIMAL(15,2),
    penalties DECIMAL(15,2),
    
    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID
);

CREATE INDEX idx_audit_cases_taxpayer ON audit_cases(taxpayer_id);
CREATE INDEX idx_audit_cases_status ON audit_cases(status);
CREATE INDEX idx_audit_cases_assigned ON audit_cases(assigned_to, status);
CREATE INDEX idx_audit_cases_number ON audit_cases(case_number);

COMMENT ON TABLE audit_cases IS 'Audit case management';
```

## Risk Engine Adapter Implementation

### Database-Based Adapters

```java
// RegistrationPort implementation
@Component
@RequiredArgsConstructor
public class DatabaseRegistrationAdapter implements RegistrationPort {
    
    private final JdbcTemplate jdbcTemplate;
    
    @Override
    public TaxpayerData.RegistrationData getRegistration(UUID taxpayerId) {
        return jdbcTemplate.queryForObject(
            """
            SELECT 
                taxpayer_id,
                tin,
                name,
                business_type,
                industry_code,
                industry_name,
                registration_date,
                is_active,
                city as location
            FROM taxpayers
            WHERE taxpayer_id = ?
            """,
            (rs, rowNum) -> TaxpayerData.RegistrationData.builder()
                    .taxpayerId(UUID.fromString(rs.getString("taxpayer_id")))
                    .tin(rs.getString("tin"))
                    .name(rs.getString("name"))
                    .businessType(rs.getString("business_type"))
                    .industryCode(rs.getString("industry_code"))
                    .industryName(rs.getString("industry_name"))
                    .registrationDate(rs.getDate("registration_date").toLocalDate())
                    .isActive(rs.getBoolean("is_active"))
                    .location(rs.getString("location"))
                    .build(),
            taxpayerId
        );
    }
}

// TaxReturnPort implementation
@Component
@RequiredArgsConstructor
public class DatabaseTaxReturnAdapter implements TaxReturnPort {
    
    private final JdbcTemplate jdbcTemplate;
    
    @Override
    public TaxpayerData.ReturnData getReturns(UUID taxpayerId, int taxYear) {
        return jdbcTemplate.queryForObject(
            """
            SELECT 
                COUNT(*) as total_returns,
                COUNT(*) FILTER (WHERE status = 'FILED_ON_TIME') as on_time,
                COUNT(*) FILTER (WHERE status = 'FILED_LATE') as late,
                COUNT(*) FILTER (WHERE is_nil_return = true) as nil,
                COUNT(*) FILTER (WHERE is_amended = true) as amended,
                AVG(gross_revenue) as avg_revenue
            FROM tax_returns
            WHERE taxpayer_id = ? AND tax_year = ?
            """,
            (rs, rowNum) -> TaxpayerData.ReturnData.builder()
                    .taxpayerId(taxpayerId)
                    .taxYear(taxYear)
                    .totalReturns(rs.getInt("total_returns"))
                    .onTimeReturns(rs.getInt("on_time"))
                    .lateReturns(rs.getInt("late"))
                    .nilReturns(rs.getInt("nil"))
                    .amendedReturns(rs.getInt("amended"))
                    .averageRevenue(rs.getBigDecimal("avg_revenue"))
                    .build(),
            taxpayerId,
            taxYear
        );
    }
}

// PaymentPort implementation
@Component
@RequiredArgsConstructor
public class DatabasePaymentAdapter implements PaymentPort {
    
    private final JdbcTemplate jdbcTemplate;
    
    @Override
    public TaxpayerData.PaymentData getPaymentHistory(UUID taxpayerId) {
        return jdbcTemplate.queryForObject(
            """
            SELECT 
                COUNT(*) as total_payments,
                COUNT(*) FILTER (WHERE days_late = 0) as on_time,
                COUNT(*) FILTER (WHERE days_late > 0) as late,
                SUM(amount) as total_paid,
                COALESCE(
                    (SELECT SUM(outstanding_amount) 
                     FROM outstanding_balances 
                     WHERE taxpayer_id = ? AND status = 'OUTSTANDING'), 
                    0
                ) as outstanding
            FROM payments
            WHERE taxpayer_id = ? AND status = 'CONFIRMED'
            """,
            (rs, rowNum) -> TaxpayerData.PaymentData.builder()
                    .taxpayerId(taxpayerId)
                    .totalPayments(rs.getInt("total_payments"))
                    .onTimePayments(rs.getInt("on_time"))
                    .latePayments(rs.getInt("late"))
                    .totalPaid(rs.getBigDecimal("total_paid"))
                    .outstandingBalance(rs.getBigDecimal("outstanding"))
                    .build(),
            taxpayerId,
            taxpayerId
        );
    }
}
```

## Configuration

```yaml
# application.yml - Single database for everything

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/tax_audit_central_db
    username: tax_audit_user
    password: ${DB_PASSWORD:your-password}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
```

## Benefits of This Approach

1. **Single Query Performance**: All data in one transaction
2. **ACID Guarantees**: Consistent data across all modules
3. **Simple Deployment**: One database to manage
4. **Easy Joins**: Can query across all tables efficiently
5. **Lower Latency**: No network calls between services
6. **Easier Maintenance**: Single schema to evolve
7. **Simpler Testing**: One database to seed with test data

## Conclusion

**This centralized approach is perfect for:**
- ✅ Tax Audit systems (moderate scale)
- ✅ Government systems (single organization)
- ✅ Need for transactional consistency
- ✅ Fast query performance
- ✅ Simpler operations

**You can always split later if needed!**
