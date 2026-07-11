# 🔧 Render Deployment - FINAL FIX

## What was wrong?

Your environment variables were **NOT being set** in Render. The logs show:
```
Connection to localhost:5432 refused
No active profile set, falling back to default
```

This means Render didn't have your environment variables configured.

---

## ✅ Solution: Use `render.yaml` file

I've created `render.yaml` which tells Render exactly what to do.

---

## 🚀 How to deploy NOW

### Option A: Use current Neon database (RECOMMENDED - Already pushed!)

**The `render.yaml` file is already in GitHub!** Render will detect it automatically.

1. **Delete your current service** on Render:
   - Go to: https://dashboard.render.com
   - Click on "risk-engine" service
   - Settings → Delete Service

2. **Create NEW service from `render.yaml`**:
   - Go to: https://dashboard.render.com
   - Click "New +" → "Blueprint"
   - Select your repository: `Paul-dir/risk-practice`
   - Render will detect `render.yaml` automatically
   - Click "Apply"

3. **Done!** Render will deploy with all environment variables automatically.

---

### Option B: Use Render's PostgreSQL (if you don't trust Neon)

If you want Render to create its own PostgreSQL:

1. **Replace render.yaml**:
   ```bash
   cd /home/paul/Desktop/risk-practice
   cp render-with-render-db.yaml render.yaml
   git add render.yaml
   git commit -m "Switch to Render PostgreSQL"
   git push
   ```

2. **Create service from Blueprint** (same as Option A step 2)

3. Render will create BOTH:
   - PostgreSQL database (free tier)
   - Your web service
   - Connect them automatically

---

## 📊 Comparison: Neon vs Render PostgreSQL

| Feature | Neon (Option A) | Render PostgreSQL (Option B) |
|---------|----------------|------------------------------|
| **Free Tier** | 0.5 GB | 1 GB |
| **Uptime** | Always on | Spins down after 90 days inactive |
| **Speed** | Fast (serverless) | Good |
| **Trust** | ✅ Real company, VC-backed | ✅ Render's own service |
| **Setup** | Already done! | Need to recreate |

---

## ⚡ Quick Test: Does Neon work?

Let's test the Neon connection from your local machine:

```bash
psql "postgresql://neondb_owner:npg_C02LRFcUdHQW@ep-fancy-boat-atwyn5yp-pooler.c-9.us-east-1.aws.neon.tech/neondb?sslmode=require"
```

If you see `neondb=>` prompt, **Neon works perfectly!**

---

## 🎯 Recommended Next Steps

**OPTION A** (Easiest - use current setup):
1. Delete current Render service
2. Create new service from Blueprint (it will read `render.yaml`)
3. Wait 5 minutes
4. Test: `curl https://risk-engine.onrender.com/actuator/health`

**OPTION B** (Use Render's database):
1. Run the commands above to switch to `render-with-render-db.yaml`
2. Create service from Blueprint
3. Render creates both database and app
4. Test the same way

---

## ❓ Why render.yaml is better than Web UI?

- ✅ **Declarative** - Everything in one file
- ✅ **Version controlled** - In Git
- ✅ **Reproducible** - Can recreate anytime
- ✅ **No manual clicks** - Render reads it automatically

---

## 🔍 How to verify it's working

After deployment succeeds, you'll see in logs:
```
✅ Started RiskPracticeApplication
✅ Tomcat started on port 8080
✅ No errors about "localhost:5432"
```

Then test:
```bash
# Health check
curl https://YOUR-URL.onrender.com/actuator/health

# Expected: {"status":"UP","components":{"db":{"status":"UP"}}}

# Risk assessment
curl -X POST https://YOUR-URL.onrender.com/api/v1/risk/assess \
  -H "Content-Type: application/json" \
  -d '{"taxpayerId":"550e8400-e29b-41d4-a716-446655440000","tin":"TEST123"}'
```

---

## 🆘 Still not working?

Check these in order:

1. **Is `render.yaml` in GitHub?**
   - Go to: https://github.com/Paul-dir/risk-practice
   - You should see `render.yaml` file

2. **Did you create service from Blueprint?**
   - Must use "Blueprint" option, NOT "Web Service"

3. **Check Render logs**:
   - Look for line: `Running with Spring Boot v3.2.1`
   - Should NOT see "falling back to default profile"
   - Should see "Started RiskPracticeApplication"

---

**Choose Option A (Neon) - it's already configured and pushed!** 🚀
