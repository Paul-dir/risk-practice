# 🚀 Free Deployment Guide for Tax Risk Engine

## Overview

Your Risk Engine can be deployed for **FREE** on several platforms. Here are the best options:

---

## Option 1: Railway.app (⭐ RECOMMENDED - Easiest)

**Pros:**
- ✅ Truly free tier (500 hours/month)
- ✅ Includes PostgreSQL database
- ✅ Automatic deployments from GitHub
- ✅ Easy setup (< 10 minutes)
- ✅ HTTPS included
- ✅ Good for testing and small production

**Cons:**
- ⚠️ Apps sleep after 30 minutes of inactivity (free tier)
- ⚠️ Limited to 500 hours/month

**Perfect for:** Testing, demos, small production workloads

### Step-by-Step Railway Deployment:

#### 1. Sign Up
```
1. Go to https://railway.app
2. Click "Start a New Project"
3. Sign in with GitHub
4. Authorize Railway to access your repositories
```

#### 2. Create New Project
```
1. Click "New Project"
2. Select "Deploy from GitHub repo"
3. Choose "Paul-dir/risk-practice"
4. Railway will detect it's a Java/Maven project
```

#### 3. Add PostgreSQL Database
```
1. In your project, click "+ New"
2. Select "Database" → "PostgreSQL"
3. Railway will create a PostgreSQL instance
4. Copy the connection details
```

#### 4. Configure Environment Variables
```
In your Railway project settings, add these variables:

SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=<copy from Railway PostgreSQL>
SPRING_DATASOURCE_USERNAME=<copy from Railway PostgreSQL>
SPRING_DATASOURCE_PASSWORD=<copy from Railway PostgreSQL>
PORT=8080
```

#### 5. Deploy
```
1. Railway will automatically build and deploy
2. Wait 5-10 minutes for first deployment
3. Railway will give you a public URL like:
   https://risk-practice-production.up.railway.app
```

#### 6. Test
```bash
# Health check
curl https://your-app.up.railway.app/actuator/health

# Risk assessment
curl -X POST https://your-app.up.railway.app/api/v1/risk/assess \
  -H "Content-Type: application/json" \
  -d '{"taxpayerId":"550e8400-e29b-41d4-a716-446655440000","tin":"TEST123"}'
```

#### 7. Configure Tax Audit
```
In Tax Audit system:
risk-engine.url=https://your-app.up.railway.app
```

---

## Option 2: Render.com (Good Alternative)

**Pros:**
- ✅ Free tier (750 hours/month)
- ✅ Includes PostgreSQL
- ✅ Auto-deploy from GitHub
- ✅ HTTPS included
- ✅ Doesn't sleep as aggressively

**Cons:**
- ⚠️ Slower cold starts (free tier)
- ⚠️ Limited resources

### Step-by-Step Render Deployment:

#### 1. Sign Up
```
1. Go to https://render.com
2. Sign up with GitHub
3. Authorize Render
```

#### 2. Create Web Service
```
1. Click "New +"
2. Select "Web Service"
3. Connect GitHub repository: Paul-dir/risk-practice
4. Configure:
   - Name: risk-engine
   - Environment: Java
   - Build Command: ./mvnw clean package -DskipTests
   - Start Command: java -jar target/risk-engine-1.0.0-SNAPSHOT.jar
```

#### 3. Add PostgreSQL
```
1. Click "New +"
2. Select "PostgreSQL"
3. Name: risk-engine-db
4. Plan: Free
5. Copy connection string
```

#### 4. Configure Environment Variables
```
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=<Render PostgreSQL URL>
SPRING_DATASOURCE_USERNAME=<from Render>
SPRING_DATASOURCE_PASSWORD=<from Render>
```

#### 5. Deploy
```
Render will automatically deploy
URL: https://risk-engine.onrender.com
```

---

## Option 3: Fly.io (Most Control)

**Pros:**
- ✅ More generous free tier
- ✅ Better performance
- ✅ Global deployment
- ✅ More control

**Cons:**
- ⚠️ Requires more configuration
- ⚠️ Need to install CLI tool
- ⚠️ Steeper learning curve

### Step-by-Step Fly.io Deployment:

#### 1. Install Fly CLI
```bash
# On Linux
curl -L https://fly.io/install.sh | sh

# Add to PATH
export PATH="$HOME/.fly/bin:$PATH"
```

#### 2. Sign Up and Login
```bash
fly auth signup
# or
fly auth login
```

#### 3. Create fly.toml Configuration
```bash
cd /home/paul/Desktop/risk-practice

# Create fly.toml file (I'll provide content below)
```

#### 4. Create PostgreSQL Database
```bash
fly postgres create --name risk-engine-db --region fra
```

#### 5. Deploy
```bash
fly deploy
```

#### 6. Get Database Connection
```bash
fly postgres connect -a risk-engine-db
```

---

## Option 4: Oracle Cloud Free Tier (Most Resources)

**Pros:**
- ✅ ALWAYS FREE (not trial)
- ✅ 2 VMs with 1GB RAM each (24GB total)
- ✅ Full control
- ✅ No time limits

**Cons:**
- ⚠️ Complex setup
- ⚠️ Manual configuration
- ⚠️ Need to manage server yourself

### Quick Setup:
```
1. Sign up at https://cloud.oracle.com/free
2. Create Compute Instance (Ubuntu)
3. Install Java 17
4. Install PostgreSQL
5. Upload JAR file
6. Run application
```

---

## Comparison Table

| Platform | Free Tier | Database | Setup Time | Best For |
|----------|-----------|----------|------------|----------|
| **Railway** ⭐ | 500 hrs/month | ✅ Included | 10 min | **Quick demo/testing** |
| **Render** | 750 hrs/month | ✅ Included | 15 min | Testing/small prod |
| **Fly.io** | Generous | ✅ Need setup | 30 min | More control |
| **Oracle Cloud** | Always free | ✅ Self-managed | 2 hours | Full production |

---

## 🎯 My Recommendation: Railway.app

For your use case (integration testing with Tax Audit), **Railway.app** is perfect:

1. ✅ **Fastest setup** - 10 minutes from signup to live URL
2. ✅ **PostgreSQL included** - No extra configuration
3. ✅ **Auto-deploy from GitHub** - Push code → automatically deploys
4. ✅ **HTTPS URL** - Secure connection for Tax Audit
5. ✅ **Good for testing** - Perfect for integration testing phase

### Complete Railway Setup (Detailed):

#### Step 1: Sign Up (2 minutes)
```
1. Open https://railway.app
2. Click "Start a New Project"
3. Click "Login with GitHub"
4. Authorize Railway (click Authorize)
```

#### Step 2: Create Project (3 minutes)
```
1. Click "New Project"
2. Click "Deploy from GitHub repo"
3. Select "Paul-dir/risk-practice"
4. Wait for Railway to analyze the repo
```

#### Step 3: Add Database (2 minutes)
```
1. In project dashboard, click "+ New"
2. Click "Database"
3. Click "Add PostgreSQL"
4. Railway creates database automatically
5. Click on PostgreSQL service
6. Go to "Connect" tab
7. Copy these values:
   - DATABASE_URL (full connection string)
   - Or individual: PGHOST, PGPORT, PGUSER, PGPASSWORD, PGDATABASE
```

#### Step 4: Configure Application (2 minutes)
```
1. Click on your "risk-practice" service
2. Go to "Variables" tab
3. Click "+ New Variable"
4. Add these (one by one):

   SPRING_PROFILES_ACTIVE = prod
   
   SPRING_DATASOURCE_URL = postgresql://<PGHOST>:<PGPORT>/<PGDATABASE>
   (replace with values from Step 3)
   
   SPRING_DATASOURCE_USERNAME = <PGUSER from Step 3>
   
   SPRING_DATASOURCE_PASSWORD = <PGPASSWORD from Step 3>
   
   PORT = 8080

5. Click "Save"
```

#### Step 5: Deploy (1 minute)
```
1. Railway automatically starts deployment
2. Wait 5-10 minutes for first build
3. Watch logs in "Deployments" tab
4. When you see "Started RiskPracticeApplication", it's ready!
```

#### Step 6: Get Your URL (1 minute)
```
1. Go to "Settings" tab
2. Scroll to "Domains"
3. Click "Generate Domain"
4. Railway gives you: https://risk-practice-production.up.railway.app
5. Copy this URL
```

#### Step 7: Test Your Deployment
```bash
# Replace with YOUR Railway URL
export RAILWAY_URL="https://risk-practice-production.up.railway.app"

# Health check
curl $RAILWAY_URL/actuator/health

# Should return: {"status":"UP"}

# Risk assessment
curl -X POST $RAILWAY_URL/api/v1/risk/assess \
  -H "Content-Type: application/json" \
  -d '{
    "taxpayerId": "550e8400-e29b-41d4-a716-446655440000",
    "tin": "TEST123456789"
  }'

# Should return: Complete risk assessment JSON
```

#### Step 8: Configure Tax Audit
```
In your Tax Audit system configuration:

risk.engine.url=https://risk-practice-production.up.railway.app
risk.engine.endpoint=/api/v1/risk/assess
```

#### Step 9: Test Integration from Tax Audit
```java
// In Tax Audit system
String riskEngineUrl = "https://risk-practice-production.up.railway.app/api/v1/risk/assess";

RestTemplate restTemplate = new RestTemplate();
RiskAssessmentRequest request = new RiskAssessmentRequest(taxpayerId, tin);

ResponseEntity<RiskAssessmentResponse> response = 
    restTemplate.postForEntity(riskEngineUrl, request, RiskAssessmentResponse.class);

RiskAssessmentResponse assessment = response.getBody();
System.out.println("Risk Level: " + assessment.getRiskLevel());
```

---

## Important Notes

### Railway Free Tier Limits:
- **500 hours/month** = ~20 days of continuous running
- **Perfect for:** Development, testing, demos
- **App sleeps** after 30 minutes of inactivity
- **Wakes up** automatically on first request (takes ~30 seconds)

### Production Considerations:
- For **24/7 production**, consider Railway's paid plan ($5/month)
- Or use Oracle Cloud Always Free tier
- Or deploy to your organization's servers

### Redis and Kafka:
Railway also offers Redis for caching:
```
1. Click "+ New" in your project
2. Select "Database" → "Redis"
3. Configure SPRING_REDIS_HOST and SPRING_REDIS_PORT
```

For Kafka, you'll need:
- Use Railway's paid tier, or
- Use external service like CloudKarafka (has free tier), or
- Disable Kafka in application.yml for testing

---

## Troubleshooting

### Issue: Build Fails
```
Solution: Check Railway build logs
- Go to "Deployments" tab
- Click on failed deployment
- Read error message
- Common issue: Java version mismatch
```

### Issue: Application Won't Start
```
Solution: Check application logs
- Go to "Deployments" tab
- Click "View Logs"
- Look for error messages
- Usually database connection issues
```

### Issue: Database Connection Fails
```
Solution: Verify environment variables
- Check SPRING_DATASOURCE_URL format
- Should be: postgresql://host:port/database
- Not: postgres:// (note the 'ql' suffix)
```

### Issue: App is Slow
```
Solution: Free tier limitation
- First request after sleep takes 30 seconds
- Subsequent requests are fast
- Upgrade to paid tier for always-on
```

---

## Next Steps After Deployment

1. ✅ **Test health endpoint**
2. ✅ **Test risk assessment endpoint**
3. ✅ **Update Tax Audit configuration** with Railway URL
4. ✅ **Test integration** from Tax Audit
5. ✅ **Monitor logs** on Railway dashboard
6. ✅ **Add custom domain** (optional, Railway supports it)

---

## Summary

**Fastest Path to Deployment:**

1. Sign up at https://railway.app (2 min)
2. Deploy from GitHub (3 min)
3. Add PostgreSQL (2 min)
4. Configure variables (2 min)
5. Get public URL (1 min)
6. Test and integrate (5 min)

**Total Time: ~15 minutes** ⚡

**Result:** Fully functional Risk Engine accessible via HTTPS for Tax Audit integration!

---

## Ready to Deploy?

Follow the Railway steps above. If you get stuck, Railway has excellent docs at https://docs.railway.app

**Good luck with your deployment!** 🚀
