# 🚀 Render.com Deployment - Step-by-Step Guide

## ✅ You're at: https://dashboard.render.com/web/new

Perfect! Let's deploy your Risk Engine now.

---

## 📋 STEP 1: Connect GitHub Repository (On the page you're at now)

You'll see: "Deploy a Web Service"

### Public Git Repository Section:

1. **Repository URL**: Paste this:
   ```
   https://github.com/Paul-dir/risk-practice
   ```

2. Click the **"Connect"** button (blue button)

---

## 📋 STEP 2: Configure Your Service (Next page)

After clicking Connect, you'll see a form. Fill it EXACTLY like this:

### Basic Information:

**Name:**
```
risk-engine
```

**Region:**
- Select: **Oregon (US West)** or **Ohio (US East)** (choose closest to your Neon database)

**Branch:**
```
main
```

**Root Directory:**
- Leave EMPTY (blank)

**Runtime:**
- Select: **Java** from the dropdown

---

## 📋 STEP 3: Build Configuration

### Build Command:
**COPY AND PASTE THIS EXACTLY:**
```
./mvnw clean package -DskipTests
```

### Start Command:
**COPY AND PASTE THIS EXACTLY:**
```
java -jar target/risk-engine-1.0.0-SNAPSHOT.jar
```

---

## 📋 STEP 4: Choose Plan

Scroll down to "Instance Type":

- Select: **Free** (first option)
  - 512 MB RAM
  - Free for 750 hours/month
  - Perfect for testing!

---

## 📋 STEP 5: Environment Variables (MOST IMPORTANT!)

Scroll down to **"Environment Variables"** section.

Click **"Add Environment Variable"** button.

You'll add **6 variables**. For each one, click "Add Environment Variable" to get new fields:

---

### Variable 1:
**Key:**
```
SPRING_DATASOURCE_URL
```

**Value:**
```
jdbc:postgresql://ep-fancy-boat-atwyn5yp-pooler.c-9.us-east-1.aws.neon.tech:5432/neondb?sslmode=require
```

---

### Variable 2:
**Key:**
```
SPRING_DATASOURCE_USERNAME
```

**Value:**
```
neondb_owner
```

---

### Variable 3:
**Key:**
```
SPRING_DATASOURCE_PASSWORD
```

**Value:**
```
npg_C02LRFcUdHQW
```

---

### Variable 4:
**Key:**
```
SPRING_PROFILES_ACTIVE
```

**Value:**
```
prod
```

---

### Variable 5:
**Key:**
```
HIBERNATE_DDL_AUTO
```

**Value:**
```
update
```

---

### Variable 6:
**Key:**
```
PORT
```

**Value:**
```
8080
```

---

✅ **You should now have 6 environment variables listed**

---

## 📋 STEP 6: Advanced Settings (Optional but Recommended)

Click **"Advanced"** to expand advanced settings:

### Auto-Deploy:
- Keep: **Yes** (checked) ✅
  - This means every time you push to GitHub, it auto-deploys

### Health Check Path:
**PASTE THIS:**
```
/actuator/health
```

---

## 📋 STEP 7: Create Web Service!

Scroll to the bottom.

Click the big blue button: **"Create Web Service"**

---

## 📋 STEP 8: Wait for Deployment (5-10 minutes)

You'll see:

```
🔄 In Progress
   ↓
📦 Build starting...
   ↓
⚙️ Deploying...
   ↓
✅ Live
```

### What to watch:
1. **Logs tab** - You can click "Logs" to see build progress
2. You'll see Maven downloading dependencies
3. Then building your JAR
4. Then starting Spring Boot
5. Finally: "Started RiskPracticeApplication"

---

## 📋 STEP 9: Get Your URL

After deployment succeeds (when you see "Live"):

1. Look at the top of the page
2. You'll see your URL:
   ```
   https://risk-engine.onrender.com
   ```
   or
   ```
   https://risk-engine-xxxx.onrender.com
   ```

3. **COPY THIS URL** - this is your public API!

---

## 📋 STEP 10: Test Your Deployment

### Test 1: Health Check

**Replace YOUR-URL with your actual Render URL:**

```bash
curl https://YOUR-URL.onrender.com/actuator/health
```

**Expected Response:**
```json
{"status":"UP","components":{"db":{"status":"UP"}}}
```

### Test 2: Risk Assessment

```bash
curl -X POST https://YOUR-URL.onrender.com/api/v1/risk/assess \
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

---

## 🎉 YOU'RE DONE!

Your Risk Engine is now:
- ✅ Live on Render.com
- ✅ Connected to Neon PostgreSQL
- ✅ Has a public URL
- ✅ Ready for Tax Audit integration

---

## 📊 Your Architecture Now:

```
GitHub (risk-practice)
        ↓ auto-deploy on push
Render.com (Your App)
        ↓ SSL connection
Neon PostgreSQL (Database)
        ↓ HTTPS API
Tax Audit System (Can integrate!)
```

---

## 🔧 Troubleshooting

### Build Fails?

1. Click **"Logs"** tab in Render dashboard
2. Look for error message
3. Common issues:
   - ❌ Maven timeout → Try deploying again
   - ❌ Java version → Check pom.xml has Java 17

### App Won't Start?

1. Check **"Logs"** tab
2. Look for:
   - ❌ "Connection refused" → Database URL wrong
   - ❌ "Authentication failed" → Check password
   - ❌ "Database does not exist" → Check database name is `neondb`

**Solution:**
- Go to your service → **"Environment"** tab
- Click **"Edit"** on any variable to verify values
- Make sure SPRING_DATASOURCE_URL is exactly:
  ```
  jdbc:postgresql://ep-fancy-boat-atwyn5yp-pooler.c-9.us-east-1.aws.neon.tech:5432/neondb?sslmode=require
  ```

### Health Check Fails?

1. Wait 2 minutes - first start is slow
2. Check logs for "Started RiskPracticeApplication"
3. Verify health check path is `/actuator/health`

### App is Slow?

**This is normal for Render free tier:**
- Apps sleep after 15 minutes of inactivity
- First request wakes it up (takes 30-60 seconds)
- Subsequent requests are fast
- Upgrade to paid plan for always-on

---

## 💡 Pro Tips

### View Logs:
1. Go to your service dashboard
2. Click **"Logs"** tab
3. See real-time logs

### Redeploy:
1. Click **"Manual Deploy"** button
2. Select **"Deploy latest commit"**
3. Click **"Deploy"**

### Update Environment Variables:
1. Click **"Environment"** tab
2. Click **"Add Environment Variable"** to add new ones
3. Or click **"Edit"** on existing ones
4. Changes trigger automatic redeploy

### Custom Domain (Optional):
1. Click **"Settings"** tab
2. Scroll to **"Custom Domain"**
3. Add your own domain (e.g., api.yourdomain.com)

---

## 📝 Quick Checklist

Before clicking "Create Web Service", verify:

- [ ] Repository URL: https://github.com/Paul-dir/risk-practice
- [ ] Name: risk-engine
- [ ] Branch: main
- [ ] Runtime: Java
- [ ] Build Command: `./mvnw clean package -DskipTests`
- [ ] Start Command: `java -jar target/risk-engine-1.0.0-SNAPSHOT.jar`
- [ ] Instance Type: Free
- [ ] 6 environment variables added:
  - [ ] SPRING_DATASOURCE_URL
  - [ ] SPRING_DATASOURCE_USERNAME
  - [ ] SPRING_DATASOURCE_PASSWORD
  - [ ] SPRING_PROFILES_ACTIVE
  - [ ] HIBERNATE_DDL_AUTO
  - [ ] PORT
- [ ] Health Check Path: /actuator/health

If all checked ✅ → Click "Create Web Service"!

---

## 🎯 After Deployment Success

### Configure Tax Audit Integration:

```yaml
# In Tax Audit application.yml
risk-engine:
  url: https://your-app.onrender.com
  endpoints:
    assess: /api/v1/risk/assess
```

### Test from Tax Audit:

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

## 📞 Need Help?

If you get stuck:
1. Take a screenshot of the error
2. Check the "Logs" tab in Render
3. Look for the error message
4. Common issues are in Troubleshooting section above

---

## 🆚 Render vs Koyeb

**Render.com Advantages:**
- ✅ Simpler interface
- ✅ Better free tier (750 hours vs 500)
- ✅ More reliable
- ✅ Better documentation

**Why we switched:**
- You were already on Render's page
- Render is easier to use
- Same result - working API!

---

**Good luck! You're almost there! 🚀**

After deployment, test with curl and you're done!
