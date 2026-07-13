# ✅ Tax Risk Engine - Testing Guide

## System Status

**✅ Application is RUNNING and WORKING!**

- **Health Status**: UP
- **Database**: Connected (PostgreSQL)
- **Port**: 8080
- **Profile**: dev
- **Mock Data**: Active (returns test data)

## Quick Test Results

I've already tested the system - here's what works:

```bash
✅ Health Check: http://localhost:8080/actuator/health
   Response: {"status":"UP"}

✅ Risk Assessment: POST http://localhost:8080/api/v1/risk/assess
   Input: {"taxpayerId":"550e8400-e29b-41d4-a716-446655440000","tin":"TEST123456789"}
   Response: Complete risk assessment with scores and indicators
```

## Testing Methods

You can test in two ways:
1. **Command Line (curl)** - Quick tests
2. **Postman** - Full testing with collection

---

## Method 1: Command Line Testing (curl)

### Test 1: Health Check
```bash
curl http://localhost:8080/actuator/health
```

**Expected Response:**
```json
{"status":"UP","components":{"db":{"status":"UP"},"ping":{"status":"UP"}}}
```

### Test 2: Risk Assessment
```bash
curl -X POST http://localhost:8080/api/v1/risk/assess \
  -H "Content-Type: application/json" \
  -d '{
    "taxpayerId": "550e8400-e29b-41d4-a716-446655440000",
    "tin": "TEST123456789"
  }'
```

**Expected Response:**
```json
{
  "assessmentId": "uuid-here",
  "taxpayerId": "550e8400-e29b-41d4-a716-446655440000",
  "tin": "TEST123456789",
  "overallScore": 0.00,
  "riskLevel": "LOW",
  "confidenceFactor": 1.00,
  "priorityRank": 0,
  "categoryScores": [
    {
      "category": "FILING",
      "score": 0.10,
      "weight": 0.25,
      "contribution": 0.00
    },
    ...
  ],
  "indicatorScores": [
    {
      "indicatorCode": "LATE_FILING",
      "indicatorName": "Late Filing",
      "score": 15.00,
      "actualValue": "45 days",
      "explanation": "Filing was 45 days late."
    },
    ...
  ],
  "assessmentDate": "2026-07-11T..."
}
```

### Test 3: Different Taxpayers
```bash
# Test taxpayer 2
curl -X POST http://localhost:8080/api/v1/risk/assess \
  -H "Content-Type: application/json" \
  -d '{
    "taxpayerId": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
    "tin": "1234567890"
  }'

# Test taxpayer 3
curl -X POST http://localhost:8080/api/v1/risk/assess \
  -H "Content-Type: application/json" \
  -d '{
    "taxpayerId": "b1ffcd00-0d1c-5fg9-cc7e-7cc0ce491b22",
    "tin": "2345678901"
  }'
```

---

## Method 2: Postman Testing

### Step 1: Import Collection

1. Open Postman
2. Click **Import** button (top left)
3. Select **File** → Browse to: `/home/paul/Desktop/risk-practice/POSTMAN_COLLECTION_UPDATED.json`
4. Click **Import**

You'll see a collection called: **"Tax Risk Assessment Engine - Complete API"**

### Step 2: Test Health Check

1. Expand collection → **Health & Monitoring** → **Health Check**
2. Click **Send**
3. Verify response shows `"status":"UP"`

### Step 3: Test Risk Assessment

1. Expand **Risk Assessment** → **Assess Risk - Simple Test**
2. Click **Send**
3. Verify response contains:
   - ✅ `assessmentId` (UUID)
   - ✅ `overallScore` (number)
   - ✅ `riskLevel` (LOW/MEDIUM/HIGH/CRITICAL)
   - ✅ `categoryScores` (array of 6 categories)
   - ✅ `indicatorScores` (array of 11 indicators)

### Step 4: Copy Assessment ID

From the response, copy the `assessmentId` value (UUID).

Example: `"assessmentId": "1efdfc64-2dd7-4a64-b9bb-4b4869d93746"`

### Step 5: Get Risk Explanation

1. Expand **Risk Explanation** → **Get Risk Explanation**
2. In the URL, replace `{{assessmentId}}` with the UUID you copied
3. Click **Send**
4. Verify you get detailed explanation of the assessment

---

## Understanding the Response

### Risk Assessment Response Structure

```json
{
  "assessmentId": "uuid",           // Unique ID for this assessment
  "taxpayerId": "uuid",              // Taxpayer being assessed
  "tin": "string",                   // Tax Identification Number
  "overallScore": 0.00,              // Overall risk score (0-100)
  "riskLevel": "LOW",                // LOW, MEDIUM, HIGH, or CRITICAL
  "confidenceFactor": 1.00,          // Confidence in assessment (0-1)
  "priorityRank": 0,                 // Priority ranking (lower = higher priority)
  "categoryScores": [...],           // 6 category breakdowns
  "indicatorScores": [...],          // 11 detailed indicators
  "assessmentDate": "2026-07-11..."  // When assessment was performed
}
```

### Category Scores (6 Categories)

1. **FILING** (25% weight) - Filing compliance
2. **PAYMENT** (25% weight) - Payment compliance
3. **FINANCIAL** (20% weight) - Financial health
4. **TRANSACTION** (15% weight) - Transaction patterns
5. **BEHAVIORAL** (10% weight) - Behavioral indicators
6. **INDUSTRY** (5% weight) - Industry benchmarks

### Indicator Scores (11 Indicators)

#### Filing (3 indicators):
- **LATE_FILING** - Late submission of returns
- **MULTIPLE_AMENDMENTS** - Frequent amendments
- **NON_FILING** - Missing required filings

#### Payment (2 indicators):
- **LATE_PAYMENT** - Late payment of taxes
- **PARTIAL_PAYMENT** - Partial payment of liabilities

#### Financial (2 indicators):
- **CONTINUOUS_LOSSES** - Consecutive years of losses
- **RAPID_REVENUE_DECLINE** - Significant revenue decrease

#### Transaction (2 indicators):
- **IMPORT_SALES_MISMATCH** - Import vs sales inconsistency
- **RELATED_PARTY_TRANSACTIONS** - High related-party dealings

#### Behavioral (1 indicator):
- **SHORT_BUSINESS_LIFE** - Recently established business

#### Industry (1 indicator):
- **SECTOR_SPECIFIC_RISK** - High-risk industry classification

---

## Current System Behavior

### ⚠️ Using Mock Data Adapters

The system currently uses **mock adapters** that return test data. This means:

✅ **Pros:**
- System works immediately without real data
- Can test all functionality
- All endpoints return realistic responses

⚠️ **Limitations:**
- All taxpayers return similar mock data
- Scores don't reflect real taxpayer behavior
- No actual database query for taxpayer data

### Mock Data Pattern

The mock adapters return:
- **Registration Data**: Generic business info
- **Filing Data**: 45 days late, 2 amendments
- **Payment Data**: 30 days late, 75% paid
- **Financial Data**: No losses, stable revenue
- **Transaction Data**: 50% import/sales ratio
- **Behavioral Data**: 3 years old business

---

## Deployment & Integration

### Q: Is the system deployed and ready for Tax Audit integration?

**A: NO - The system is currently running LOCALLY on your machine.**

Here's what's happening:

### Current State: LOCAL DEVELOPMENT

```
Your Machine (localhost:8080)
├── Risk Engine Application ✅ Running
├── PostgreSQL Database ✅ Connected
├── Mock Data Adapters ✅ Active
└── REST API ✅ Accessible at http://localhost:8080
```

**Accessible by:** Only your local machine
**Can Tax Audit use it?** NO - Not yet deployed

### What's Needed for Integration:

#### Option 1: Deploy to Server (Production)

1. **Build the application**
   ```bash
   ./mvnw clean package
   ```

2. **Deploy JAR to server**
   ```bash
   # Copy to server
   scp target/risk-engine-1.0.0-SNAPSHOT.jar user@server:/opt/risk-engine/
   ```

3. **Run on server**
   ```bash
   java -jar risk-engine-1.0.0-SNAPSHOT.jar --spring.profiles.active=prod
   ```

4. **Server URL** becomes: `http://your-server:8080`

5. **Tax Audit can integrate** using that server URL

#### Option 2: Docker Deployment

1. **Create Dockerfile** (we can create this)
2. **Build Docker image**
3. **Deploy to Docker registry**
4. **Run containers on server**
5. **Tax Audit connects to server**

#### Option 3: Keep Local + Network Access

If you want Tax Audit to use your local machine:

1. **Configure firewall** to allow port 8080
2. **Use your machine's IP** instead of localhost
3. **Keep application running**
4. **Tax Audit uses** `http://YOUR_IP:8080`

⚠️ **Not recommended for production!**

---

## Next Steps

### For Testing (Now):
✅ Use Postman or curl to test all endpoints
✅ Verify responses are complete
✅ Test different taxpayer IDs
✅ Verify database stores assessments

### For Integration (Later):
1. **Deploy to Server** - Move from localhost to production server
2. **Replace Mock Adapters** - Connect to real taxpayer data
3. **Configure Tax Audit** - Point Tax Audit to Risk Engine URL
4. **Test Integration** - End-to-end testing
5. **Go Live** - Production deployment

---

## Summary

### ✅ What's Working:
- Application running locally
- Health check responds
- Risk assessment API works
- Returns complete risk scores
- Database connected
- All 6 categories calculated
- All 11 indicators evaluated

### ⏳ What's Next:
- Deploy to production server
- Replace mock adapters with database adapters
- Load real taxpayer data
- Configure Tax Audit integration
- End-to-end testing
- Production launch

### 🎯 Current Status:
**READY FOR TESTING** ✅
**NOT YET DEPLOYED FOR INTEGRATION** ⏳

---

## Questions?

**Q: Can I test it now?**
A: YES! Use Postman or curl with the examples above.

**Q: Can Tax Audit use it now?**
A: NO - It needs to be deployed to a server first.

**Q: What's the difference between testing and integration?**
A: 
- **Testing** = You manually test the API (works now)
- **Integration** = Tax Audit system calls the API automatically (needs deployment)

**Q: How long to deploy?**
A: Depends on your infrastructure. Could be:
- Same day (if server ready)
- Few days (if need to set up server)
- Weeks (if procurement/approval needed)

---

**Ready to test? Start with the Postman collection!** 🚀
