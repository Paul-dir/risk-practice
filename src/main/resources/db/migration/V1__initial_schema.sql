-- ============================================================================
-- Tax Audit Central Database - Initial Schema
-- ============================================================================
-- Purpose: Complete database schema for Tax Audit System with Risk Engine
-- Architecture: Centralized database shared by all modules
-- Version: 1.0.0
-- Date: 2026-07-10
-- ============================================================================

-- ============================================================================
-- PART 1: MASTER DATA - Taxpayer Information
-- ============================================================================

-- TABLE: taxpayers - Master taxpayer registry
CREATE TABLE taxpayers (
    taxpayer_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tin VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    
    -- Business Information
    business_type VARCHAR(50),
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

COMMENT ON TABLE taxpayers IS 'Master taxpayer registry - shared by all modules';

-- TABLE: tax_returns - All tax return filings
CREATE TABLE tax_returns (
    return_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    taxpayer_id UUID NOT NULL REFERENCES taxpayers(taxpayer_id),
    
    -- Return Details
    return_type VARCHAR(50) NOT NULL,
    tax_year INTEGER NOT NULL,
    tax_period VARCHAR(20),
    
    -- Dates
    due_date DATE NOT NULL,
    filed_date DATE,
    submission_method VARCHAR(50),
    
    -- Status
    status VARCHAR(50) NOT NULL,
    days_late INTEGER DEFAULT 0,
    
    -- Financial Data
    gross_revenue NUMERIC(15,2),
    taxable_income NUMERIC(15,2),
    tax_amount NUMERIC(15,2),
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

COMMENT ON TABLE tax_returns IS 'All tax return filings - filing history data';

-- TABLE: payments - Payment history
CREATE TABLE payments (
    payment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    taxpayer_id UUID NOT NULL REFERENCES taxpayers(taxpayer_id),
    return_id UUID REFERENCES tax_returns(return_id),
    
    -- Payment Details
    amount NUMERIC(15,2) NOT NULL,
    payment_date DATE NOT NULL,
    payment_method VARCHAR(50),
    reference_number VARCHAR(100),
    
    -- Tax Period
    tax_period VARCHAR(20),
    tax_type VARCHAR(50),
    
    -- Status
    status VARCHAR(50) NOT NULL,
    days_late INTEGER DEFAULT 0,
    penalty_amount NUMERIC(15,2) DEFAULT 0,
    interest_amount NUMERIC(15,2) DEFAULT 0,
    
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

COMMENT ON TABLE payments IS 'Payment transaction history';

-- TABLE: outstanding_balances - Outstanding tax liabilities
CREATE TABLE outstanding_balances (
    balance_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    taxpayer_id UUID NOT NULL REFERENCES taxpayers(taxpayer_id),
    return_id UUID REFERENCES tax_returns(return_id),
    
    -- Balance Details
    original_amount NUMERIC(15,2) NOT NULL,
    paid_amount NUMERIC(15,2) DEFAULT 0,
    outstanding_amount NUMERIC(15,2) NOT NULL,
    
    -- Tax Details
    tax_period VARCHAR(20),
    tax_type VARCHAR(50),
    assessment_date DATE,
    due_date DATE,
    
    -- Aging
    days_overdue INTEGER,
    aging_category VARCHAR(50),
    
    -- Status
    status VARCHAR(50),
    
    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_balances_taxpayer ON outstanding_balances(taxpayer_id);
CREATE INDEX idx_balances_overdue ON outstanding_balances(days_overdue) WHERE status = 'OUTSTANDING';

COMMENT ON TABLE outstanding_balances IS 'Outstanding tax liabilities and aging';

-- TABLE: external_data - External data from third-party sources
CREATE TABLE external_data (
    external_data_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    taxpayer_id UUID NOT NULL REFERENCES taxpayers(taxpayer_id),
    
    -- Credit Information
    credit_score INTEGER,
    credit_rating VARCHAR(50),
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

COMMENT ON TABLE external_data IS 'External data from credit bureaus, sanctions lists, etc.';

-- TABLE: audit_history - Historical audit records
CREATE TABLE audit_history (
    audit_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    taxpayer_id UUID NOT NULL REFERENCES taxpayers(taxpayer_id),
    
    -- Audit Details
    audit_type VARCHAR(50),
    audit_period_start DATE,
    audit_period_end DATE,
    
    -- Dates
    initiated_date DATE,
    completed_date DATE,
    
    -- Findings
    findings_category VARCHAR(50),
    findings_description TEXT,
    
    -- Financial Impact
    additional_tax_assessed NUMERIC(15,2),
    penalties_assessed NUMERIC(15,2),
    amount_collected NUMERIC(15,2),
    
    -- Resolution
    status VARCHAR(50),
    
    -- Auditor
    assigned_auditor UUID,
    auditor_name VARCHAR(255),
    
    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_history_taxpayer ON audit_history(taxpayer_id);
CREATE INDEX idx_audit_history_date ON audit_history(completed_date DESC);

COMMENT ON TABLE audit_history IS 'Historical audit records for compliance tracking';

-- ============================================================================
-- PART 2: RISK ENGINE TABLES
-- ============================================================================

-- TABLE: risk_assessments - Risk assessment results
CREATE TABLE risk_assessments (
    id UUID PRIMARY KEY,
    taxpayer_id UUID NOT NULL REFERENCES taxpayers(taxpayer_id),
    tin VARCHAR(20) NOT NULL,
    assessment_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    overall_score NUMERIC(5,2) NOT NULL CHECK (overall_score >= 0 AND overall_score <= 100),
    risk_level VARCHAR(20) NOT NULL CHECK (risk_level IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    confidence_factor NUMERIC(3,2) CHECK (confidence_factor >= 0 AND confidence_factor <= 1),
    priority_rank INTEGER,
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED' CHECK (status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'FAILED', 'OVERRIDDEN')),
    config_version INTEGER NOT NULL DEFAULT 1,
    
    -- Override tracking
    override_justification TEXT,
    overridden_by UUID,
    overridden_at TIMESTAMP,
    
    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Comments
COMMENT ON TABLE risk_assessments IS 'Stores completed risk assessments for taxpayers';
COMMENT ON COLUMN risk_assessments.overall_score IS 'Overall risk score (0-100), higher = higher risk';
COMMENT ON COLUMN risk_assessments.risk_level IS 'Risk classification: LOW, MEDIUM, HIGH, CRITICAL';
COMMENT ON COLUMN risk_assessments.confidence_factor IS 'Assessment confidence (0.0-1.0)';
COMMENT ON COLUMN risk_assessments.priority_rank IS 'Priority ranking (1=highest priority)';
COMMENT ON COLUMN risk_assessments.config_version IS 'Configuration version used for this assessment';

-- Indexes for risk_assessments
CREATE INDEX idx_risk_assessments_taxpayer ON risk_assessments(taxpayer_id);
CREATE INDEX idx_risk_assessments_tin ON risk_assessments(tin);
CREATE INDEX idx_risk_assessments_date ON risk_assessments(assessment_date DESC);
CREATE INDEX idx_risk_assessments_score ON risk_assessments(overall_score DESC);
CREATE INDEX idx_risk_assessments_level ON risk_assessments(risk_level);
CREATE INDEX idx_risk_assessments_taxpayer_date ON risk_assessments(taxpayer_id, assessment_date DESC);

-- TABLE: taxpayer_risk_profile - Historical risk profile
CREATE TABLE taxpayer_risk_profile (
    taxpayer_id UUID NOT NULL,
    tin VARCHAR(20) NOT NULL UNIQUE,
    
    -- Current state
    current_risk_score NUMERIC(5,2) CHECK (current_risk_score >= 0 AND current_risk_score <= 100),
    current_risk_level VARCHAR(20) CHECK (current_risk_level IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    last_assessment_date TIMESTAMP,
    
    -- Trend analysis
    risk_trend VARCHAR(20) CHECK (risk_trend IN ('IMPROVING', 'STABLE', 'DETERIORATING')),
    consecutive_high_risk_count INTEGER DEFAULT 0,
    first_high_risk_date TIMESTAMP,
    
    -- Historical data (JSONB for flexibility)
    previous_scores JSONB DEFAULT '[]',
    
    -- Metadata
    config_version INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT pk_taxpayer_risk_profile PRIMARY KEY (taxpayer_id)
);

-- Indexes for taxpayer_risk_profile
CREATE INDEX idx_taxpayer_profile_tin ON taxpayer_risk_profile(tin);
CREATE INDEX idx_taxpayer_profile_score ON taxpayer_risk_profile(current_risk_score DESC);
CREATE INDEX idx_taxpayer_profile_level ON taxpayer_risk_profile(current_risk_level);

-- Comments
COMMENT ON TABLE taxpayer_risk_profile IS 'Maintains historical risk profile for each taxpayer';
COMMENT ON COLUMN taxpayer_risk_profile.previous_scores IS 'Historical scores as JSON array: [{date, score, level}, ...]';
COMMENT ON COLUMN taxpayer_risk_profile.risk_trend IS 'Current risk trend direction';

-- TABLE: risk_indicator_config - Risk indicator definitions
CREATE TABLE risk_indicator_config (
    id UUID DEFAULT gen_random_uuid(),
    indicator_code VARCHAR(50) NOT NULL UNIQUE,
    indicator_name VARCHAR(200) NOT NULL,
    category VARCHAR(50) NOT NULL CHECK (category IN ('FILING', 'PAYMENT', 'FINANCIAL', 'TRANSACTION', 'BEHAVIORAL', 'INDUSTRY')),
    
    -- Scoring configuration
    weight NUMERIC(4,3) NOT NULL DEFAULT 0.100 CHECK (weight >= 0 AND weight <= 1),
    min_threshold NUMERIC(10,2) DEFAULT 0,
    max_threshold NUMERIC(10,2) DEFAULT 100,
    scoring_formula VARCHAR(20) DEFAULT 'LINEAR' CHECK (scoring_formula IN ('LINEAR', 'LOGARITHMIC', 'EXPONENTIAL', 'CUSTOM')),
    
    -- Metadata
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    version INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    
    CONSTRAINT pk_risk_indicator_config PRIMARY KEY (id)
);

-- Indexes for risk_indicator_config
CREATE INDEX idx_indicator_config_category ON risk_indicator_config(category);
CREATE INDEX idx_indicator_config_active ON risk_indicator_config(is_active, category);

-- Comments
COMMENT ON TABLE risk_indicator_config IS 'Configurable risk indicators with weights and thresholds';
COMMENT ON COLUMN risk_indicator_config.indicator_code IS 'Unique code like LATE_FILING, NON_PAYMENT, etc.';
COMMENT ON COLUMN risk_indicator_config.weight IS 'Relative weight in score calculation (0.0-1.0)';
COMMENT ON COLUMN risk_indicator_config.scoring_formula IS 'Algorithm to apply: LINEAR, LOGARITHMIC, EXPONENTIAL';

-- TABLE: assessment_category_scores - Category-level scores
CREATE TABLE assessment_category_scores (
    id UUID DEFAULT gen_random_uuid(),
    assessment_id UUID,
    category VARCHAR(50) NOT NULL CHECK (category IN ('FILING', 'PAYMENT', 'FINANCIAL', 'TRANSACTION', 'BEHAVIORAL', 'INDUSTRY')),
    score NUMERIC(5,2) NOT NULL CHECK (score >= 0 AND score <= 100),
    weight NUMERIC(4,3) NOT NULL CHECK (weight >= 0 AND weight <= 1),
    contribution NUMERIC(5,2) NOT NULL,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT pk_assessment_category_scores PRIMARY KEY (id),
    CONSTRAINT fk_category_assessment FOREIGN KEY (assessment_id) REFERENCES risk_assessments(id) ON DELETE CASCADE,
    CONSTRAINT uq_assessment_category UNIQUE (assessment_id, category)
);

COMMENT ON TABLE assessment_category_scores IS 'Category-level breakdown of risk scores';

-- TABLE: assessment_indicator_scores - Indicator-level scores
CREATE TABLE assessment_indicator_scores (
    id UUID DEFAULT gen_random_uuid(),
    assessment_id UUID ,
    indicator_code VARCHAR(50) NOT NULL,
    indicator_name VARCHAR(200) NOT NULL,
    category VARCHAR(50) NOT NULL,
    score NUMERIC(5,2) NOT NULL CHECK (score >= 0 AND score <= 100),
    weight NUMERIC(4,3) NOT NULL CHECK (weight >= 0 AND weight <= 1),
    contribution NUMERIC(5,2) NOT NULL,
    actual_value VARCHAR(200),
    explanation TEXT,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT pk_assessment_indicator_scores PRIMARY KEY (id),
    CONSTRAINT fk_indicator_assessment FOREIGN KEY (assessment_id) REFERENCES risk_assessments(id) ON DELETE CASCADE,
    CONSTRAINT uq_assessment_indicator UNIQUE (assessment_id, indicator_code)
);

COMMENT ON TABLE assessment_indicator_scores IS 'Indicator-level detailed breakdown of risk scores';
COMMENT ON COLUMN assessment_indicator_scores.actual_value IS 'Actual value observed (e.g., "95 days late")';
COMMENT ON COLUMN assessment_indicator_scores.explanation IS 'Human-readable explanation of this indicator';

-- TABLE: risk_audit_logs - Audit trail
CREATE TABLE risk_audit_logs (
    id UUID DEFAULT gen_random_uuid(),
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID NOT NULL,
    action VARCHAR(20) NOT NULL CHECK (action IN ('CREATE', 'UPDATE', 'DELETE', 'OVERRIDE')),
    performed_by VARCHAR(100) NOT NULL,
    performed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    old_value JSONB,
    new_value JSONB,
    justification TEXT,
    ip_address VARCHAR(45),
    
    CONSTRAINT pk_audit_log PRIMARY KEY (id)
);

CREATE INDEX idx_audit_log_entity ON risk_audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_log_date ON risk_audit_logs(performed_at DESC);

COMMENT ON TABLE risk_audit_logs IS 'Complete audit trail for compliance and traceability';

-- ============================================================================
-- PART 3: TAX AUDIT MODULE TABLES
-- ============================================================================

-- TABLE: audit_cases - Audit case management
CREATE TABLE audit_cases (
    case_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    taxpayer_id UUID NOT NULL REFERENCES taxpayers(taxpayer_id),
    assessment_id UUID REFERENCES risk_assessments(id),
    
    -- Case Details
    case_number VARCHAR(50) UNIQUE NOT NULL,
    audit_type VARCHAR(50) NOT NULL,
    priority VARCHAR(20),
    
    -- Period
    audit_period_start DATE,
    audit_period_end DATE,
    
    -- Status
    status VARCHAR(50) NOT NULL,
    
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
    additional_tax NUMERIC(15,2),
    penalties NUMERIC(15,2),
    
    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID
);

CREATE INDEX idx_audit_cases_taxpayer ON audit_cases(taxpayer_id);
CREATE INDEX idx_audit_cases_status ON audit_cases(status);
CREATE INDEX idx_audit_cases_assigned ON audit_cases(assigned_to, status);
CREATE INDEX idx_audit_cases_number ON audit_cases(case_number);

COMMENT ON TABLE audit_cases IS 'Audit case management for Tax Audit module';

-- ============================================================================
-- PART 4: PERFORMANCE INDEXES
-- ============================================================================
-- Already created inline with each table for better organization

-- ============================================================================
-- PART 5: SEED DATA
-- ============================================================================

INSERT INTO risk_indicator_config (indicator_code, indicator_name, category, weight, description) VALUES
-- Filing Compliance (25% total)
('LATE_FILING', 'Late Filing', 'FILING', 0.083, 'Filing submitted after deadline'),
('MULTIPLE_AMENDMENTS', 'Multiple Amendments', 'FILING', 0.083, 'Return amended multiple times'),
('NON_FILING', 'Non-Filing', 'FILING', 0.084, 'Missing required filings'),

-- Payment Compliance (25% total)
('LATE_PAYMENT', 'Late Payment', 'PAYMENT', 0.125, 'Payment made after deadline'),
('PARTIAL_PAYMENT', 'Partial Payment', 'PAYMENT', 0.125, 'Only partial tax liability paid'),

-- Financial Health (20% total)
('CONTINUOUS_LOSSES', 'Continuous Losses', 'FINANCIAL', 0.100, 'Consecutive years of reported losses'),
('RAPID_REVENUE_DECLINE', 'Rapid Revenue Decline', 'FINANCIAL', 0.100, 'Significant revenue decrease year-over-year'),

-- Transaction Analysis (15% total)
('IMPORT_SALES_MISMATCH', 'Import vs Sales Mismatch', 'TRANSACTION', 0.075, 'Imports disproportionate to domestic sales'),
('RELATED_PARTY_TRANSACTIONS', 'Related-Party Transactions', 'TRANSACTION', 0.075, 'High volume of related-party dealings'),

-- Behavioral Patterns (10% total)
('PREVIOUS_FRAUD', 'Previous Fraud History', 'BEHAVIORAL', 0.050, 'Prior fraud or criminal violations'),
('SHORT_BUSINESS_LIFE', 'Short Business Life', 'BEHAVIORAL', 0.050, 'Recently established business'),

-- Industry Context (5% total)
('SECTOR_SPECIFIC_RISK', 'Sector-Specific Risk', 'INDUSTRY', 0.025, 'High-risk industry classification'),
('INDUSTRY_DEVIATION', 'Industry Deviation', 'INDUSTRY', 0.025, 'Significant deviation from industry norms')
ON CONFLICT (indicator_code) DO NOTHING;

-- ============================================================================
-- PART 6: TRIGGERS
-- ============================================================================

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_risk_assessments_updated_at BEFORE UPDATE ON risk_assessments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_taxpayer_profile_updated_at BEFORE UPDATE ON taxpayer_risk_profile
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_indicator_config_updated_at BEFORE UPDATE ON risk_indicator_config
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_taxpayers_updated_at BEFORE UPDATE ON taxpayers
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_tax_returns_updated_at BEFORE UPDATE ON tax_returns
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_payments_updated_at BEFORE UPDATE ON payments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_audit_cases_updated_at BEFORE UPDATE ON audit_cases
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- PART 7: ANALYTICAL VIEWS
-- ============================================================================

-- View: Latest assessment per taxpayer
CREATE OR REPLACE VIEW v_latest_assessments AS
SELECT DISTINCT ON (taxpayer_id)
    taxpayer_id,
    tin,
    assessment_date,
    overall_score,
    risk_level,
    priority_rank
FROM risk_assessments
ORDER BY taxpayer_id, assessment_date DESC;

COMMENT ON VIEW v_latest_assessments IS 'Latest risk assessment for each taxpayer';

-- View: High risk taxpayers requiring attention
CREATE OR REPLACE VIEW v_high_risk_taxpayers AS
SELECT 
    p.taxpayer_id,
    p.tin,
    p.current_risk_score,
    p.current_risk_level,
    p.last_assessment_date,
    p.risk_trend,
    p.consecutive_high_risk_count
FROM taxpayer_risk_profile p
WHERE p.current_risk_level IN ('HIGH', 'CRITICAL')
ORDER BY p.current_risk_score DESC;

COMMENT ON VIEW v_high_risk_taxpayers IS 'Taxpayers with HIGH or CRITICAL risk levels';

-- ============================================================================
-- END OF MIGRATION
-- ============================================================================
