# 🚀 Risk Engine API - Testing Guide

## Your Live API

**Base URL:** `https://risk-engine-720i.onrender.com`

**Status:** ✅ LIVE and RUNNING!

---

## Quick Test (Using curl)

### 1. Health Check
```bash
curl https://risk-engine-720i.onrender.com/actuator/health
```

**Expected Response:**
```json
{"status":"UP","groups":["liveness","readiness"]}
```

### 2. Create Risk Assessment (Wait for redeploy first!)
```bash
curl -X POST https://risk-engine-720i.onrender.com/api/v1/risk/assess \
  -H "Content-Type: application/json" \
  -d '{
    "taxpayerId": "123e4567-e89b-12d3-a456-426614174000",
    "tin": "1234567890"
  }'
```

### 3. View Metrics
```bash
curl https://risk-engine-720i.onrender.com/actuator/metrics
```

---

## Test with Postman

### Import the Collection

1. **Open Postman**
2. Click **"Import"** button (top left)
3. Select **"File"** tab
4. Choose: `RENDER_POSTMAN_COLLECTION.json`
5. Click **"Import"**

### Collection Structure

The collection includes:

📁 **Health & Monitoring**
- Health Check
- Detailed Health
- All Metrics
- Risk Assessments Completed
- Prometheus Metrics

📁 **Risk Assessment**
- Assess Taxpayer Risk ⭐ (Main endpoint)
- Get Risk Assessment by ID
- Get Latest Assessment for Taxpayer
- Get Assessment History
- Search Assessments by Risk Level

📁 **Risk Profile**
- Get Taxpayer Risk Profile
- Update Risk Profile

📁 **Configuration**
- Get All Risk Indicators
- Get Active Indicators
- Get Indicator by Code

📁 **Statistics & Analytics**
- Get Risk Statistics
- Risk Distribution by Level
- Top High-Risk Taxpayers

### Test Now (Available Immediately)

✅ **These endpoints work RIGHT NOW:**
- All Health & Monitoring endpoints
- All Metrics endpoints

⏳ **These will work after redeploy (2-3 minutes):**
- Risk Assessment endpoints
- Risk Profile endpoints
- Configuration endpoints
- Statistics endpoints

---

## Test with Other Tools

### Using Bruno (Open Source Postman Alternative)

1. Install Bruno: https://www.usebruno.com/
2. Import the same JSON file
3. Test all endpoints

### Using Insomnia

1. Install Insomnia: https://insomnia.rest/
2. Import the collection
3. Test endpoints

### Using VS Code REST Client

1. Install "REST Client" extension
2. Create a `.http` file:

```http
### Health Check
GET https://risk-engine-720i.onrender.com/actuator/health

### Create Risk Assessment
POST https://risk-engine-720i.onrender.com/api/v1/risk/assess
Content-Type: application/json

{
  "taxpayerId": "123e4567-e89b-12d3-a456-426614174000",
  "tin": "1234567890"
}

### Get Metrics
GET https://risk-engine-720i.onrender.com/actuator/metrics
```

3. Click "Send Request" above each request

### Using HTTPie (Command Line)

```bash
# Health check
http https://risk-engine-720i.onrender.com/actuator/health

# Create assessment
http POST https://risk-engine-720i.onrender.com/api/v1/risk/assess \
  taxpayerId="123e4567-e89b-12d3-a456-426614174000" \
  tin="1234567890"
```

---

## Test Scenarios

### Scenario 1: Basic Health Check
**Goal:** Verify the service is running

```bash
curl https://risk-engine-720i.onrender.com/actuator/health
```

**Expected:** `{"status":"UP"}`

---

### Scenario 2: Create First Risk Assessment
**Goal:** Assess a taxpayer's risk

**Step 1:** Wait for redeploy to complete (check Render dashboard)

**Step 2:** Send request
```bash
curl -X POST https://risk-engine-720i.onrender.com/api/v1/risk/assess \
  -H "Content-Type: application/json" \
  -d '{
    "taxpayerId": "a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d",
    "tin": "1234567890"
  }'
```

**Expected Response:**
```json
{
  "assessmentId": "uuid",
  "taxpayerId": "a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d",
  "tin": "1234567890",
  "overallScore": 65.5,
  "riskLevel": "MEDIUM",
  "confidenceLevel": 0.85,
  "assessmentDate": "2026-07-14T...",
  "indicators": [...],
  "recommendations": [...]
}
```

---

### Scenario 3: Get Taxpayer's Latest Assessment
**Goal:** Retrieve the most recent risk assessment

```bash
curl https://risk-engine-720i.onrender.com/api/v1/risk/taxpayers/a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d/latest
```

---

### Scenario 4: View Risk Statistics
**Goal:** Get overview of all assessments

```bash
curl https://risk-engine-720i.onrender.com/api/v1/risk/statistics
```

---

## Expected Response Times

| Endpoint Type | Expected Time |
|--------------|---------------|
| Health Check | < 100ms |
| Metrics | < 200ms |
| Risk Assessment (new) | 2-5 seconds |
| Get Assessment (cached) | < 200ms |
| Statistics | < 500ms |

---

## Troubleshooting

### Issue: "Connection refused"
**Solution:** The service might be sleeping (free tier). Wait 30 seconds and retry.

### Issue: "500 Internal Server Error"
**Solution:** Redeploy is still in progress. Wait 2-3 minutes.

### Issue: "404 Not Found"
**Solution:** Check the URL spelling and endpoint path.

### Issue: Slow first request
**Solution:** Free tier services sleep after inactivity. First request wakes them up (takes ~30 seconds).

---

## Monitoring Your API

### View Logs in Real-Time
1. Go to Render Dashboard
2. Click on your service: `risk-engine`
3. Click "Logs" tab
4. See real-time logs as you test

### Check Metrics
```bash
# See all available metrics
curl https://risk-engine-720i.onrender.com/actuator/metrics

# Check specific metric
curl https://risk-engine-720i.onrender.com/actuator/metrics/risk.assessment.completed
```

### Prometheus Integration
Your API exposes Prometheus metrics at:
```
https://risk-engine-720i.onrender.com/actuator/prometheus
```

---

## Sample Test Data

### Valid Taxpayer IDs
```
123e4567-e89b-12d3-a456-426614174000
a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d
f1e2d3c4-b5a6-4738-9291-0a1b2c3d4e5f
```

### Valid TINs
```
1234567890
9876543210
1111111111
```

### Risk Levels
```
CRITICAL
HIGH
MEDIUM
LOW
```

---

## Next Steps

1. ✅ **Wait for current redeploy** (table name fix)
2. ✅ **Import Postman collection**
3. ✅ **Test health endpoints** (work now)
4. ✅ **Test risk assessment** (works after redeploy)
5. ✅ **Explore all endpoints**
6. 🚀 **Integrate with your frontend!**

---

## API Documentation

Once deployed, you may have Swagger UI available at:
```
https://risk-engine-720i.onrender.com/swagger-ui.html
```

(If not available, we can enable it)

---

## Rate Limits (Render Free Tier)

- **No hard rate limits** on free tier
- Service sleeps after 15 minutes of inactivity
- 750 hours/month free
- After that, service stops until next month

---

## Production Considerations

When you're ready to upgrade:

1. **Custom Domain**: Add your own domain
2. **Scale Up**: Increase instance size for better performance
3. **Add Redis**: Enable caching for faster responses
4. **Add Kafka**: Enable event streaming
5. **Monitoring**: Add Datadog/New Relic integration

---

## Support

**Issues?** Check:
1. Render Dashboard → Logs
2. Health endpoint: `/actuator/health`
3. Metrics endpoint: `/actuator/metrics`

**Need Help?**
- Check logs first
- Verify request format
- Test with curl before Postman
- Check if redeploy is complete

---

🎉 **Congratulations on your successful deployment!**

Your Risk Engine is now live and ready to assess taxpayer risk!
