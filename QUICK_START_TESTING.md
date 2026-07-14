# 🚀 Quick Start - Testing Your Risk Engine

## What You Have Now

Your risk engine is ready to test with **4 realistic taxpayer scenarios**:
1. ✅ **Perfect Taxpayer** - Clean record (LOW risk)
2. ⚠️ **Late Filer** - Filing delays only (LOW risk)
3. ⚠️ **Late Payer** - Payment delays only (LOW risk)
4. 🔴 **Multiple Amendments** - Suspicious activity (MEDIUM risk)

---

## 3-Step Quick Start

### Step 1: Start Your Application (2 minutes)
```bash
cd /home/paul/Desktop/risk-practice
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

**Look for this in logs**:
```
✓ Loaded test data for taxpayer: Meridian Office Supplies PLC
✓ Loaded test data for taxpayer: Coastal Fisheries Cooperative
✓ Loaded test data for taxpayer: Harborview Auto Repair Ltd
✓ Loaded test data for taxpayer: Zenith Business Consulting Ltd
✓ Successfully loaded 4 test taxpayers
```

### Step 2: Run All Tests (1 minute)
```bash
# In a new terminal
cd /home/paul/Desktop/risk-practice
./test-all-taxpayers.sh
```

### Step 3: Compare Results
Use `TEST_RESULTS_TEMPLATE.md` to record and compare:
- Expected scores vs Actual scores
- Risk levels match?
- Recommendations appropriate?

---

## Test Data Structure

### Raw Input Fields (What Goes IN)
```javascript
{
  "taxpayer_id": "UUID",
  "tin": "TIN-XXXXXXXXX",
  "business_name": "Company Name",
  "industry_sector": "Industry",
  
  // Filing data
  "filing_summary": {
    "total_late_days": 14,
    "amendment_count": 0,
    "missing_count": 0
  },
  
  // Payment data
  "payment_summary": {
    "total_outstanding": 0.0,
    "total_late_days": 3,
    "late_payment_count": 1
  },
  
  // Financial data
  "financial_data": {
    "revenue_2025": 8309345.23,
    "profit_margin_2025": 12.57,
    "revenue_growth_pct": 9.47
  },
  
  // VAT data
  "vat_information": {
    "output_vat": 1138611.46,
    "input_vat": 927968.34,
    "vat_ratio": 0.82
  },
  
  // Benchmarks
  "industry_benchmark": {
    "average_profit_margin_pct": 12.83,
    "average_vat_ratio": 0.84,
    "sector_risk_classification": "Standard"
  }
}
```

### Expected Output (What Comes OUT)
```javascript
{
  "overall_score": 2.97,        // Your engine calculates this
  "risk_level": "LOW",           // Your engine determines this
  "recommended_audit_type": "Monitor; eligible for random selection"
}
```

---

## How It Works

### Data Flow
```
1. You call: POST /api/v1/risk/score
   Body: { "taxpayerId": "UUID", "taxYear": 2025 }

2. System loads raw data from test-taxpayers.json

3. Mock Adapters transform JSON → Domain Objects
   - MockRegistrationAdapter → Business info
   - MockTaxReturnAdapter → Filing data
   - MockPaymentAdapter → Payment data
   - MockIntegrationAdapter → VAT data
   - MockIndustryBenchmarkAdapter → Benchmarks

4. YOUR RISK ENGINE processes the data

5. Returns risk score and classification
```

### Mock Adapters Logic
```java
// Example: MockTaxReturnAdapter
JsonNode data = testDataLoader.getTaxpayerData(taxpayerId);
return TaxpayerData.ReturnData.builder()
    .lateFilingDays(data.get("filing_summary").get("total_late_days").asInt())
    .numberOfAmendments(data.get("filing_summary").get("amendment_count").asInt())
    // ... etc
    .build();
```

---

## The 4 Test Scenarios Explained

### Scenario 1: Perfect Taxpayer 🌟
**What makes them perfect?**
- Minimal late filings (14 days across 4 returns)
- Only 1 late payment (3 days)
- Stable, growing revenue
- Normal VAT ratio (0.82)
- No outstanding debt

**Expected Outcome**: Score ~2.97 (LOW risk)

**Why this matters**: Validates your engine doesn't flag good taxpayers

---

### Scenario 2: Late Filer 📄
**What's the issue?**
- 5 late filings (177 days total - significant!)
- BUT: All payments on time
- No amendments
- Stable business

**Expected Outcome**: Score ~7.84 (LOW risk)

**Why this matters**: Tests if your engine properly weighs filing vs payment issues

---

### Scenario 3: Late Payer 💰
**What's the issue?**
- Only 1 late filing (6 days - minimal)
- BUT: 4 late payments (111 days total)
- All debts eventually paid
- No other red flags

**Expected Outcome**: Score ~10.56 (LOW risk)

**Why this matters**: Tests if payment delays raise risk appropriately

---

### Scenario 4: Multiple Amendments 🚨
**What's suspicious?**
- 7 late filings (91 days)
- 5 amendments (repeatedly revising returns downward)
- VAT ratio 0.99 (almost claiming back everything - unusual!)
- 18% import/sales mismatch
- 37.7% related party transactions

**Expected Outcome**: Score ~40.83 (MEDIUM risk)

**Why this matters**: Tests if your engine catches potentially fraudulent behavior

---

## Expected Score Breakdown

### Category Contributions

| Category | Weight | Perfect | Late Filing | Late Payment | Amendments |
|----------|--------|---------|-------------|--------------|------------|
| **Filing** | 25% | 0.64 | **5.40** | 0.97 | **9.67** |
| **Payment** | 25% | 0.64 | 0.79 | **7.24** | **8.93** |
| **Financial** | 20% | 0.70 | 0.59 | 0.70 | **8.70** |
| **Transaction** | 15% | 0.60 | 0.49 | 0.77 | **9.16** |
| **Behavioral** | 10% | 0.12 | 0.24 | 0.52 | 2.64 |
| **Industry** | 5% | 0.26 | 0.32 | 0.36 | 1.74 |
| **TOTAL** | 100% | **2.97** | **7.84** | **10.56** | **40.83** |

---

## Validation Checklist

### ✅ Basic Tests
- [ ] Application starts without errors
- [ ] Test data loaded (4 taxpayers in logs)
- [ ] All 4 API calls return 200 OK
- [ ] Response includes `riskScore` field
- [ ] Response includes `riskLevel` field

### ✅ Score Accuracy (±5% tolerance)
- [ ] Perfect Taxpayer: 2.97 ± 0.15
- [ ] Late Filing: 7.84 ± 0.39
- [ ] Late Payment: 10.56 ± 0.53
- [ ] Multiple Amendments: 40.83 ± 2.04

### ✅ Risk Classification
- [ ] Perfect → LOW
- [ ] Late Filing → LOW
- [ ] Late Payment → LOW
- [ ] Multiple Amendments → MEDIUM or HIGH

### ✅ Recommendations
- [ ] LOW risk → Monitor or Random Audit
- [ ] MEDIUM risk → Desk Audit within 60 days
- [ ] HIGH risk → Field Audit immediately

---

## Troubleshooting

### ❌ "No test data loaded"
**Cause**: JSON file not found  
**Fix**: Check `src/main/resources/test-taxpayers.json` exists

### ❌ "Wrong risk scores"
**Cause**: Risk calculation formula different  
**Fix**: Review your scoring logic and weights

### ❌ "Using fallback data"
**Cause**: Taxpayer UUID doesn't match  
**Fix**: Copy UUID exactly from this guide

### ❌ Connection refused
**Cause**: Application not running  
**Fix**: Start with `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`

---

## What Makes This Approach Good?

✅ **Real Data**: From actual tax scenarios, not random  
✅ **Repeatable**: Same results every time  
✅ **Verifiable**: Clear expected outcomes  
✅ **Comprehensive**: Tests different risk profiles  
✅ **Documented**: You know why each score is expected  
✅ **Raw Input**: No pre-calculated scores, your engine does the work

---

## Files Reference

| File | Purpose |
|------|---------|
| `test-taxpayers.json` | Raw test data (4 taxpayers) |
| `TestTaxpayerDataLoader.java` | Loads JSON into memory |
| `Mock*Adapter.java` (5 files) | Transforms JSON → Domain objects |
| `test-all-taxpayers.sh` | Automated test script |
| `TEST_TAXPAYERS_GUIDE.md` | Detailed documentation |
| `TEST_RESULTS_TEMPLATE.md` | Record your results |
| `TEST_TAXPAYERS_POSTMAN.json` | Postman collection |

---

## Ready? Let's Test! 🎯

```bash
# Terminal 1: Start app
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Terminal 2: Run tests
./test-all-taxpayers.sh

# Or test manually:
curl -X POST http://localhost:8080/api/v1/risk/score \
  -H "Content-Type: application/json" \
  -d '{"taxpayerId": "73f6813e-4bea-4eb6-8fc8-df9ea8888f2e", "taxYear": 2025}'
```

**Good luck! 🚀**
