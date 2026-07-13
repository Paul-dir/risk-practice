# 🚀 Deploy to Koyeb + Neon - Complete Guide

## ✅ What We've Done So Far

1. ✅ Updated `application.yml` to use environment variables
2. ✅ Pushed changes to GitHub
3. ⏳ Ready to deploy!

---

## Phase 1: Create Neon PostgreSQL Database (5 minutes)

### Step 1: Sign Up for Neon

1. Go to: **https://neon.tech**
2. Click **"Sign Up"**
3. Choose **"Sign in with GitHub"** (easiest option)
4. Authorize Neon to access your GitHub account

### Step 2: Create Your Database Project

After login:

1. Click **"Create a project"** (or **"New Project"**)
2. Fill in the form:
   ```
   Project Name: risk-engine
   Database Name: risk_engine_db
   Region: Choose closest to you:
      - US East (Ohio) - for North America
      - Europe (Frankfurt) - for Europe
      - Asia Pacific (Singapore) - for Asia
   Postgres Version: 15 (or latest available)
   ```
3. Click **"Create Project"**

### Step 3: Copy Your Connection Details

After project creation, you'll see a screen with connection details:

#### Copy These Values:

```
Host: ep-xxxxx-xxxxx.xxx.aws.neon.tech
Database: risk_engine_db  
User: neondb_owner
Password: ******** (click to reveal and copy)
Port: 5432
```

#### Copy the JDBC Connection String:

You'll see something like:
```
postgresql://neondb_owner:npg_xxxxxxxxxxxx@ep-xxxxx-xxxxx.xxx.aws.neon.tech/risk_engine_db?sslmode=require
```

**For Spring Boot, convert it to JDBC format:**
```
jdbc:postgresql://ep-xxxxx-xxxxx.xxx.aws.neon.tech:5432/risk_engine_db?sslmode=require
```

📝 **Save these credentials somewhere safe - we'll need them in Phase 3!**

---

## Phase 2: Prepare Koyeb Account (2 minutes)

### Step 1: Sign Up for Koyeb

1. Go to: **https://app.koyeb.com**
2. Click **"Sign Up"**
3. Choose **"Continue with GitHub"** (easiest)
4. Authorize Koyeb

### Step 2: Verify Free Plan

Koyeb offers:
- ✅ 1 free web service
- ✅ $5.50/month worth of compute (enough for testing)
- ✅ Automatic HTTPS
- ✅ Auto-deploy from GitHub

You're now ready to deploy!

---

## Phase 3: Deploy Your Application on Koyeb (10 minutes)

### Step 1: Create New App

1. In Koyeb dashboard, click **"Create App"**
2. Choose **"GitHub"** as deployment method
3. Click **"Install Koyeb on GitHub"** if prompted
   - Select **"Only select repositories"**
   - Choose **"Paul-dir/risk-practice"**
   - Click **"Install & Authorize"**

### Step 2: Configure Build Settings

Back in Koyeb:

1. **Repository**: Select `Paul-dir/risk-practice`
2. **Branch**: `main`
3. **Builder**: Choose **"Buildpack"**
4. **Build command**: 
   ```
   ./mvnw clean package -DskipTests
   ```
5. **Run command**:
   ```
   java -jar target/risk-engine-1.0.0-SNAPSHOT.jar
   ```

### Step 3: Configure Environment Variables

Click **"Environment variables"** and add these:

| Variable Name | Value | Example |
|---------------|-------|---------|
| `SPRING_DATASOURCE_URL` | Your Neon JDBC URL | `jdbc:postgresql://ep-xxxxx.aws.neon.tech:5432/risk_engine_db?sslmode=require` |
| `SPRING_DATASOURCE_USERNAME` | Your Neon username | `neondb_owner` |
| `SPRING_DATASOURCE_PASSWORD` | Your Neon password | `npg_xxxxxxxxxxxx` |
| `SPRING_PROFILES_ACTIVE` | `prod` | `prod` |
| `HIBERNATE_DDL_AUTO` | `update` | `update` |
| `PORT` | `8080` | `8080` |

**Optional (if you have Redis/Kafka later):**
| Variable Name | Value |
|---------------|-------|
| `REDIS_HOST` | (your Redis host) |
| `REDIS_PORT` | `6379` |
| `KAFKA_BOOTSTRAP_SERVERS` | (your Kafka servers) |

### Step 4: Configure Service Settings

1. **Service name**: `risk-engine` (or your preferred name)
2. **Region**: Choose same region as your Neon database
3. **Instance type**: **Eco** (free tier)
4. **Scaling**: 
   - Min instances: `1`
   - Max instances: `1`

### Step 5: Advanced Settings (Optional but Recommended)

1. Click **"Advanced"**
2. **Health checks**:
   - Path: `/actuator/health`
   - Port: `8080`
   - Initial delay: `30` seconds
   - Period: `10` seconds
3. **Ports**:
   - Port: `8080`
   - Protocol: `HTTP`

### Step 6: Deploy!

1. Review all settings
2. Click **"Deploy"** button
3. Wait for deployment (5-10 minutes)

You'll see:
```
📦 Building...
⚙️ Starting...
✅ Healthy
```

### Step 7: Get Your Public URL

After successful deployment:

1. Go to your app dashboard
2. You'll see a URL like:
   ```
   https://risk-engine-paul-dir.koyeb.app
   ```
3. Copy this URL - this is your public API!

---

## Phase 4: Test Your Deployment (5 minutes)

### Test 1: Health Check

```bash
curl https://your-app.koyeb.app/actuator/health
```

**Expected Response:**
```json
{"status":"UP","components":{"db":{"status":"UP"}}}
```

### Test 2: Risk Assessment

```bash
curl -X POST https://your-app.koyeb.app/api/v1/risk/assess \
  -H "Content-Type: application/json" \
  -d '{
    "taxpayerId": "550e8400-e29b-41d4-a716-446655440000",
    "tin": "TEST123456789"
  }'
```

**Expected Response:**
```json
{
  "assessmentId": "...",
  "taxpayerId": "550e8400-e29b-41d4-a716-446655440000",
  "tin": "TEST123456789",
  "overallScore": 0.00,
  "riskLevel": "LOW",
  ...
}
```

### Test 3: Check Database Connection

```bash
curl https://your-app.koyeb.app/actuator/health/db
```

Should show PostgreSQL connection is UP.

---

## Phase 5: Configure Tax Audit Integration

Now that your Risk Engine is deployed, configure Tax Audit to use it:

### In Tax Audit Application

```yaml
# application.yml or application.properties
risk-engine:
  url: https://your-app.koyeb.app
  endpoints:
    assess: /api/v1/risk/assess
    explain: /api/v1/risk/explain/{taxpayerId}
```

### Example Integration Code

```java
@Service
public class RiskEngineClient {
    
    @Value("${risk-engine.url}")
    private String riskEngineUrl;
    
    private final RestTemplate restTemplate;
    
    public RiskAssessmentResponse assessTaxpayer(UUID taxpayerId, String tin) {
        String url = riskEngineUrl + "/api/v1/risk/assess";
        
        RiskAssessmentRequest request = new RiskAssessmentRequest(taxpayerId, tin);
        
        return restTemplate.postForObject(url, request, RiskAssessmentResponse.class);
    }
}
```

---

## Troubleshooting

### Issue: Build Fails

**Check Koyeb logs:**
1. Go to your app in Koyeb
2. Click "Logs" tab
3. Look for error messages

**Common issues:**
- ❌ Maven build timeout → Use `-DskipTests`
- ❌ Java version mismatch → Verify Java 17+ in pom.xml

**Solution:**
```bash
# Make sure pom.xml has:
<properties>
    <java.version>17</java.version>
</properties>
```

### Issue: Application Won't Start

**Check application logs:**
1. Koyeb dashboard → Your app → "Logs"
2. Look for startup errors

**Common issues:**
- ❌ Database connection failed
  - Verify `SPRING_DATASOURCE_URL` is correct JDBC format
  - Check `?sslmode=require` is included
- ❌ Flyway migration fails
  - Set `HIBERNATE_DDL_AUTO=update` instead of validate
- ❌ Port mismatch
  - Verify `PORT=8080` environment variable

### Issue: Health Check Fails

**Verify:**
```bash
# Direct port test
curl https://your-app.koyeb.app:8080/actuator/health

# Check if app is listening
curl https://your-app.koyeb.app/actuator/health
```

**Solution:**
- Make sure app binds to `0.0.0.0:8080` not `localhost:8080`
- This is automatically handled by Spring Boot with `PORT` env var

### Issue: Database Migrations Fail

**If Flyway fails on first deployment:**

1. **Option A**: Use Hibernate to create schema first
   ```
   Set: HIBERNATE_DDL_AUTO=update
   ```

2. **Option B**: Manually run Flyway
   ```bash
   # In Neon SQL Editor:
   # Run your V1__initial_schema.sql manually
   ```

3. **Option C**: Skip Flyway temporarily
   ```
   Add env var: SPRING_FLYWAY_ENABLED=false
   ```

### Issue: Redis/Kafka Connection Errors

**If you don't have Redis or Kafka:**

Add these environment variables to disable them:
```
SPRING_CACHE_TYPE=none
RISK_ENGINE_EVENTS_KAFKA_ENABLED=false
```

---

## Monitoring Your Deployment

### Koyeb Dashboard

Monitor:
- ✅ **Status**: Should show "Healthy"
- ✅ **CPU/Memory**: Should be stable
- ✅ **Requests**: Track incoming API calls
- ✅ **Logs**: Real-time application logs

### Application Endpoints

```bash
# Health check
curl https://your-app.koyeb.app/actuator/health

# Metrics
curl https://your-app.koyeb.app/actuator/metrics

# Info
curl https://your-app.koyeb.app/actuator/info
```

---

## Costs & Limits

### Neon Free Tier:
- ✅ 0.5 GB storage
- ✅ 10 GB data transfer/month
- ✅ 100 hours compute/month
- ✅ 1 project
- ⚠️ Database suspends after 5 minutes of inactivity (auto-resumes)

### Koyeb Free Tier:
- ✅ 1 web service
- ✅ $5.50/month compute credits
- ✅ Shared CPU
- ✅ 512 MB RAM
- ✅ Auto-scaling (1 instance for free)
- ⚠️ Apps may sleep after inactivity

**Perfect for:**
- ✅ Testing & development
- ✅ Demos
- ✅ Low-traffic production (<1000 requests/day)

**Upgrade if:**
- ⚠️ Need 24/7 availability
- ⚠️ High traffic (>5000 requests/day)
- ⚠️ Need more than 512MB RAM

---

## Next Steps After Deployment

1. ✅ **Test all endpoints** with Postman
2. ✅ **Configure Tax Audit** to use your Koyeb URL
3. ✅ **Test integration** from Tax Audit
4. ✅ **Monitor logs** for any errors
5. ✅ **Set up alerts** (optional) for downtime
6. ✅ **Add custom domain** (optional)

---

## Summary

### What You Have Now:

```
┌─────────────────────┐
│   GitHub Repo       │ ← Source code
│   Paul-dir/risk-    │
│   practice          │
└─────────┬───────────┘
          │ Auto-deploy on push
          ↓
┌─────────────────────┐     ┌──────────────────┐
│   Koyeb (App)       │────→│  Neon (Database) │
│   https://your-app  │ SSL │  PostgreSQL      │
│   .koyeb.app        │←────│  risk_engine_db  │
└─────────┬───────────┘     └──────────────────┘
          │
          │ HTTPS API
          ↓
┌─────────────────────┐
│  Tax Audit System   │ ← Can integrate now!
└─────────────────────┘
```

### URLs:
- **Your API**: https://your-app.koyeb.app
- **Health Check**: https://your-app.koyeb.app/actuator/health
- **Risk Assessment**: https://your-app.koyeb.app/api/v1/risk/assess

---

## 🎉 You're Ready to Deploy!

Follow the steps above and your Risk Engine will be live in ~20 minutes!

**Questions? Check Koyeb logs first, then troubleshooting section above.**

Good luck! 🚀
