# Test Taxpayers Guide

## Overview
This guide explains how to use the 4 realistic test taxpayer scenarios loaded into the system for testing the risk scoring engine.

## Test Data Location
- **JSON File**: `src/main/resources/test-taxpayers.json`
- **Loader Component**: `TestTaxpayerDataLoader.java`
- **Mock Adapters**: Updated to use test data automatically

## Test Taxpayer Scenarios

### 1. Perfect Taxpayer (LOW RISK - Score: 2.97)
**Scenario**: Fully compliant taxpayer with minimal issues

- **Taxpayer ID**: `73f6813e-4bea-4eb6-8fc8-df9ea8888f2e`
- **TIN**: `TIN-523938499`
- **Business Name**: Meridian Office Supplies PLC
- **Industry**: Wholesale Trade
- **Key Characteristics**:
  - 4 late filings (14 days total)
  - 1 late payment (3 days)
  - No outstanding balance
  - Stable financials
  - Previous audit resolved
- **Expected Result**: LOW risk, Monitor/QA

### 2. Late Filing Only (LOW RISK - Score: 7.84)
**Scenario**: Clean taxpayer with occasional filing delays

- **Taxpayer ID**: `965c6a52-5863-491b-927f-e4949eff49b7`
- **TIN**: `TIN-434848879`
- **Business Name**: Coastal Fisheries Cooperative
- **Industry**: Fishing
- **Key Characteristics**:
  - 5 late filings (177 days total)
  - 2 late payments (11 days)
  - All payments eventually made
  - No amendments
- **Expected Result**: LOW risk, Monitor/QA

### 3. Late Payment Only (LOW RISK - Score: 10.56)
**Scenario**: On-time filer but pays late due to cash-flow

- **Taxpayer ID**: `efb9ba6b-c0e6-452f-8712-0310fea39f09`
- **TIN**: `TIN-439280725`
- **Business Name**: Harborview Auto Repair Ltd
- **Industry**: Vehicle Repair Services
- **Key Characteristics**:
  - Only 1 late filing (6 days)
  - 4 late payments (111 days total)
  - No outstanding balance
  - All obligations met
- **Expected Result**: LOW risk, Monitor/QA

### 4. Multiple Amendments (MEDIUM RISK - Score: 40.83)
**Scenario**: Multiple VAT amendments shifting liability downward

- **Taxpayer ID**: `8646ee0e-54e5-4ce8-a2fb-c60b6fe42a71`
- **TIN**: `TIN-668212944`
- **Business Name**: Zenith Business Consulting Ltd
- **Industry**: Professional Services
- **Key Characteristics**:
  - 7 late filings (91 days)
  - 5 amendments (red flag)
  - High VAT ratio (0.99 - suspicious)
  - 18% import/sales mismatch
  - 37.7% related party transactions
- **Expected Result**: MEDIUM risk, Desk Audit within 60 days

## How to Test

### Using cURL

```bash
# Test Perfect Taxpayer
curl -X POST http://localhost:8080/api/v1/risk/score \
  -H "Content-Type: application/json" \
  -d '{
    "taxpayerId": "73f6813e-4bea-4eb6-8fc8-df9ea8888f2e",
    "taxYear": 2025
  }'

# Test Late Filing
curl -X POST http://localhost:8080/api/v1/risk/score \
  -H "Content-Type: application/json" \
  -d '{
    "taxpayerId": "965c6a52-5863-491b-927f-e4949eff49b7",
    "taxYear": 2025
  }'

# Test Late Payment
curl -X POST http://localhost:8080/api/v1/risk/score \
  -H "Content-Type: application/json" \
  -d '{
    "taxpayerId": "efb9ba6b-c0e6-452f-8712-0310fea39f09",
    "taxYear": 2025
  }'

# Test Multiple Amendments
curl -X POST http://localhost:8080/api/v1/risk/score \
  -H "Content-Type: application/json" \
  -d '{
    "taxpayerId": "8646ee0e-54e5-4ce8-a2fb-c60b6fe42a71",
    "taxYear": 2025
  }'
```

### Using Postman

Import the `RENDER_POSTMAN_COLLECTION.json` file and use the pre-configured requests for each test taxpayer.

## Validation Steps

1. **Start the Application**
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```

2. **Check Logs**
   - Look for: `"Loaded test data for taxpayer: [Business Name]"`
   - Should see: `"Successfully loaded 4 test taxpayers"`

3. **Test Each Scenario**
   - Submit risk scoring requests for each taxpayer
   - Compare actual scores with expected scores
   - Verify risk levels match expectations

4. **Expected Outputs**

   **Perfect Taxpayer**: 
   ```json
   {
     "riskScore": ~2.97,
     "riskLevel": "LOW",
     "recommendation": "Monitor; eligible for random selection"
   }
   ```

   **Multiple Amendments**: 
   ```json
   {
     "riskScore": ~40.83,
     "riskLevel": "MEDIUM",
     "recommendation": "Desk Audit or Issue Audit within 60 days"
   }
   ```

## Modifying Test Data

To add or modify test scenarios:

1. Edit `src/main/resources/test-taxpayers.json`
2. Follow the existing JSON structure
3. Restart the application
4. The `TestTaxpayerDataLoader` will automatically load the changes

## Data Mapping

### From JSON to Domain Model

| JSON Field | Domain Model Field | Adapter |
|------------|-------------------|---------|
| `tin` | `TaxpayerData.tin` | Registration |
| `business_type` | `TaxpayerData.businessType` | Registration |
| `industry_sector` | `TaxpayerData.industryCode` | Registration |
| `filing_summary.total_late_days` | `TaxpayerData.lateFilingDays` | TaxReturn |
| `filing_summary.amendment_count` | `TaxpayerData.numberOfAmendments` | TaxReturn |
| `payment_summary.total_late_days` | `TaxpayerData.latePaymentDays` | Payment |
| `vat_information.input_vat` | `TaxpayerData.inputVat` | Integration |
| `vat_information.output_vat` | `TaxpayerData.outputVat` | Integration |
| `industry_benchmark.*` | `TaxpayerData.BenchmarkData` | Benchmark |

## Troubleshooting

### No Test Data Loaded
- Check file exists: `src/main/resources/test-taxpayers.json`
- Check JSON is valid
- Check application logs for errors

### Wrong Risk Scores
- Verify the taxpayer ID is correct
- Check the risk scoring rules in your engine
- Review the calculation logic for each category

### Adapter Not Using Test Data
- Ensure `TestTaxpayerDataLoader` is being injected
- Check logs for "Using test data for taxpayer" messages
- Verify UUID format matches exactly

## Next Steps

1. ✅ Test data loaded into system
2. ⏳ Verify risk engine calculations
3. ⏳ Compare actual vs expected scores
4. ⏳ Fine-tune risk scoring algorithms
5. ⏳ Add more test scenarios as needed
