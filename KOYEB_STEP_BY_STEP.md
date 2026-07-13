# 🎯 Koyeb Deployment - EXACT Steps with Copy-Paste Values

## ✅ Prerequisites Done:
- ✅ Code pushed to GitHub
- ✅ Neon database created

---

## 📋 STEP 1: Go to Koyeb (1 minute)

### Actions:
1. Open browser
2. Go to: **https://app.koyeb.com**
3. Click **"Sign in with GitHub"**
4. Click **"Authorize Koyeb"**

---

## 📋 STEP 2: Create New App (1 minute)

### You'll see Koyeb dashboard. Now:

1. Click the big blue button: **"Create App"**
2. You'll see: "How do you want to deploy?"
3. Click: **"GitHub"** (the first option with GitHub logo)

---

## 📋 STEP 3: Connect GitHub Repository (2 minutes)

### You'll see: "Deploy a GitHub repository"

1. Click: **"Install & authorize the GitHub app"**
2. A GitHub page opens
3. Select: **"Only select repositories"**
4. Click the dropdown and select: **"risk-practice"**
5. Click: **"Install & Authorize"**
6. You'll return to Koyeb

### Now back in Koyeb:
1. In "Repository" dropdown, select: **"Paul-dir/risk-practice"**
2. Branch will auto-select: **"main"** ✅

---

## 📋 STEP 4: Configure Build (2 minutes)

### Scroll down to "Build and deployment settings"

You'll see several fields. Fill them EXACTLY like this:

#### Builder:
- Select: **"Buildpack"** (not Docker)

#### Build command:
**COPY THIS EXACTLY:**
```
./mvnw clean package -DskipTests
```

**WHERE TO PASTE:**
- Look for field labeled: "Build command (optional)"
- Click in the text box
- Paste the above command

#### Run command:
**COPY THIS EXACTLY:**
```
java -jar target/risk-engine-1.0.0-SNAPSHOT.jar
```

**WHERE TO PASTE:**
- Look for field labeled: "Run command"
- Click in the text box
- Paste the above command

---

## 📋 STEP 5: Add Environment Variables (5 minutes) ⭐ MOST IMPORTANT

### Scroll down to "Environment variables" section

Click: **"Add variable"** button

You'll add **6 variables** one by one. For each variable:

---

### Variable 1:
**Field 1 (Name):**
```
SPRING_DATASOURCE_URL
```

**Field 2 (Value):**
```
jdbc:postgresql://ep-fancy-boat-atwyn5yp-pooler.c-9.us-east-1.aws.neon.tech:5432/neondb?sslmode=require
```

Click **"Add"** then click **"Add variable"** again for next one.

---

### Variable 2:
**Field 1 (Name):**
```
SPRING_DATASOURCE_USERNAME
```

**Field 2 (Value):**
```
neondb_owner
```

Click **"Add"** then click **"Add variable"** again.

---

### Variable 3:
**Field 1 (Name):**
```
SPRING_DATASOURCE_PASSWORD
```

**Field 2 (Value):**
```
npg_C02LRFcUdHQW
```

Click **"Add"** then click **"Add variable"** again.

---

### Variable 4:
**Field 1 (Name):**
```
SPRING_PROFILES_ACTIVE
```

**Field 2 (Value):**
```
prod
```

Click **"Add"** then click **"Add variable"** again.

---

### Variable 5:
**Field 1 (Name):**
```
HIBERNATE_DDL_AUTO
```

**Field 2 (Value):**
```
update
```

Click **"Add"** then click **"Add variable"** again.

---

### Variable 6:
**Field 1 (Name):**
```
PORT
```

**Field 2 (Value):**
```
8080
```

Click **"Add"**

✅ **You should now see 6 environment variables listed**

---

## 📋 STEP 6: Configure Service Settings (2 minutes)

### Scroll down to "Service" section

#### App name:
**Type this:**
```
risk-engine
```

#### Instance type:
- Select: **"Eco"** (this is the free tier)

#### Scaling:
- Regions: Select **"Washington, D.C. (us-east)"** (same region as your Neon database)
- Instances: Keep it at **"1"**

---

## 📋 STEP 7: Configure Ports and Health Checks (1 minute)

### Scroll down to "Ports" section

Click **"Add port"** if not already there:
- Port: **"8080"**
- Protocol: **"HTTP"**

### Health checks:
Click **"Add health check"**:
- Path: **"/actuator/health"**
- Port: **"8080"**
- Protocol: **"HTTP"**
- Grace period: **"30"** seconds

---

## 📋 STEP 8: Deploy! (1 minute)

### You're ready!

1. Scroll to bottom
2. Click the big blue button: **"Deploy"**
3. Wait and watch...

### You'll see:
```
🔄 Building... (this takes 3-5 minutes)
📦 Starting...
✅ Healthy
```

---

## 📋 STEP 9: Get Your URL (1 minute)

### After deployment succeeds:

1. You'll see your app dashboard
2. Look for: **"Public URL"** or **"Domain"**
3. It will be something like:
   ```
   https://risk-engine-paul-dir.koyeb.app
   ```
4. **COPY THIS URL** - this is your API endpoint!

---

## 📋 STEP 10: Test Your Deployment (2 minutes)

### Open a terminal and test:

**Replace `YOUR-URL` with your actual Koyeb URL:**

### Test 1: Health Check
```bash
curl https://YOUR-URL-HERE.koyeb.app/actuator/health
```

**Expected response:**
```json
{"status":"UP"}
```

### Test 2: Risk Assessment
```bash
curl -X POST https://YOUR-URL-HERE.koyeb.app/api/v1/risk/assess \
  -H "Content-Type: application/json" \
  -d '{"taxpayerId":"550e8400-e29b-41d4-a716-446655440000","tin":"TEST123"}'
```

**Expected response:**
```json
{
  "assessmentId": "...",
  "taxpayerId": "550e8400-e29b-41d4-a716-446655440000",
  "tin": "TEST123",
  "overallScore": 0.00,
  "riskLevel": "LOW",
  ...
}
```

---

## 🎉 YOU'RE DONE!

Your Risk Engine is now:
- ✅ Live on the internet
- ✅ Has a public URL
- ✅ Connected to Neon PostgreSQL
- ✅ Ready for Tax Audit integration

---

## 🔧 If Something Goes Wrong

### Build fails?
1. Go to Koyeb dashboard
2. Click your app name
3. Click "Logs" tab
4. Look for error message
5. Common issue: Java version - make sure pom.xml has Java 17

### App won't start?
1. Check logs in Koyeb
2. Common issues:
   - Database connection failed → Check environment variables are exactly as shown
   - Port mismatch → Make sure PORT=8080 is set

### Health check fails?
1. Wait 2 minutes - first start takes time
2. Check if `/actuator/health` path is correct
3. Make sure PORT=8080 is set

---

## 📸 Visual Guide Summary

Here's what you'll see:

```
Step 1: Koyeb homepage
        ↓
Step 2: Click "Create App" → Choose "GitHub"
        ↓
Step 3: Select "risk-practice" repository
        ↓
Step 4: Fill in:
        - Build command: ./mvnw clean package -DskipTests
        - Run command: java -jar target/risk-engine-1.0.0-SNAPSHOT.jar
        ↓
Step 5: Add 6 environment variables (most important!)
        1. SPRING_DATASOURCE_URL = jdbc:postgresql://...
        2. SPRING_DATASOURCE_USERNAME = neondb_owner
        3. SPRING_DATASOURCE_PASSWORD = npg_C02LRFcUdHQW
        4. SPRING_PROFILES_ACTIVE = prod
        5. HIBERNATE_DDL_AUTO = update
        6. PORT = 8080
        ↓
Step 6: App name: risk-engine, Instance: Eco
        ↓
Step 7: Port: 8080, Health check: /actuator/health
        ↓
Step 8: Click "Deploy" button
        ↓
Step 9: Wait 5 minutes → Get your URL
        ↓
Step 10: Test with curl → SUCCESS! 🎉
```

---

## 📝 Quick Checklist

Before clicking "Deploy", verify:

- [ ] Repository selected: Paul-dir/risk-practice
- [ ] Branch: main
- [ ] Build command: `./mvnw clean package -DskipTests`
- [ ] Run command: `java -jar target/risk-engine-1.0.0-SNAPSHOT.jar`
- [ ] 6 environment variables added (SPRING_DATASOURCE_URL, USERNAME, PASSWORD, SPRING_PROFILES_ACTIVE, HIBERNATE_DDL_AUTO, PORT)
- [ ] App name: risk-engine
- [ ] Instance type: Eco
- [ ] Port: 8080
- [ ] Health check: /actuator/health

If all checked ✅ → Click Deploy!

---

## 🆘 Need Help?

If you get stuck at any step:
1. Take a screenshot
2. Check which step number you're on
3. Read the error message in Koyeb logs
4. Ask for help with the step number

**Good luck! You got this! 🚀**
