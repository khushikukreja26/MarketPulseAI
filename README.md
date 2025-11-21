# MarketPulse AI 📊🤖

MarketPulse AI is an **AI-powered competitor analytics platform** built as a native Android app with a Python/Flask backend.

It helps product and marketing teams:

- Track **competitor KPIs** in one place  
- Visualize trends in a clean mobile dashboard  
- Get **Gemini-powered AI insights** and concrete recommendations  
- Cut weekly manual analysis work by up to **70%**

---

## ✨ Key Features

### 📱 Android App (Kotlin + Jetpack Compose)

- **Modern dashboard UI**
  - Hero header with organization info & timeframe
  - Color-coded **risk badge** (0–100) based on AI analysis
- **Key KPIs section**
  - Cards showing KPI name, current value, and change
  - Green/red indicators with trending up/down icons
- **Trend overview**
  - Line chart built with **MPAndroidChart** wrapped in a composable
- **AI Insights panel**
  - Gemini-generated **summary** of strengths & weaknesses
  - Bullet list of **recommended actions**
  - Highlighted risk score
- **Refresh button**
  - Re-calls the backend API and updates all data

### 🧠 Backend (Flask + Gemini)

- REST API endpoints:
  - `GET /api/kpis` – returns KPI metrics for an org + timeframe
  - `POST /api/insights` – uses Gemini to analyze KPIs and return:
    - `summary`
    - `recommendations[]`
    - `risk_score` (0–100)
- **AI logic**
  - Takes raw KPI values and week-over-week changes
  - Builds a prompt and sends it to **Google Gemini**
  - Parses JSON response and returns it to the Android app
  - Safe **rule-based fallback** if Gemini/API fails
- Firebase-ready design (can plug in real data later using a service account)

---

## 🏗️ Architecture

**High-level flow:**

1. Android app starts → `DashboardViewModel` calls `DashboardRepository`
2. Repository uses **Retrofit** to hit Flask endpoints:
   - `/api/kpis?orgId=...&timeframe=weekly`
   - `/api/insights`
3. Flask backend:
   - Synthesizes KPI list (dummy or from Firebase)
   - Passes KPIs into `generate_ai_insights_from_kpis(kpis)` in `services/ai_service.py`
4. `ai_service.py`:
   - Calls **Gemini** (via `google-generativeai`) with a structured prompt
   - Expects clean JSON containing `summary`, `recommendations`, `risk_score`
   - Returns AI insights to the Flask route
5. Android app renders:
   - KPI cards
   - Trend chart
   - AI Insights card with recommendations & risk badge

---

## 📂 Project Structure

```text
MarketPulseAI/
├─ marketpulse-backend/      # Python Flask backend
│  ├─ app.py                 # Flask app entrypoint (routes + main API)
│  ├─ config.py              # Env config (Gemini, Firebase paths, etc.)
│  ├─ services/
│  │  ├─ ai_service.py       # Gemini + rule-based insights generator
│  │  └─ kpi_service.py      # KPI generation / retrieval (dummy or Firebase)
│  ├─ requirements.txt       # Backend dependencies
│  ├─ .env                   # Local env vars (NOT committed)
│  └─ firebase-service-account.json  # Local only, NOT committed
│
└─ marketpulseai/            # Android app (Kotlin, Compose)
   ├─ app/src/main/java/com/example/marketpulseai/
   │  ├─ data/
   │  │  └─ remote/
   │  │     └─ model/
   │  │        ├─ MarketPulseApi.kt      # Retrofit interface
   │  │        ├─ KpiModels.kt           # KPI DTOs
   │  │        ├─ InsightsModels.kt      # Insights DTOs
   │  │        └─ DashboardRepository.kt # Repository for dashboard data
   │  ├─ ui/
   │  │  ├─ dashboard/
   │  │  │  ├─ DashboardScreen.kt       # Main dashboard UI
   │  │  │  └─ DashboardViewModel.kt    # ViewModel (Hilt-injected)
   │  │  └─ components/
   │  │     └─ KpiLineChart.kt          # MPAndroidChart wrapper
   │  ├─ di/
   │  │  └─ NetworkModule.kt            # Hilt module (Retrofit/OkHttp)
   │  ├─ MainActivity.kt                # Hosts the Compose UI
   │  └─ MarketPulseApp.kt              # Application class (Hilt)
   ├─ app/build.gradle.kts
   └─ settings.gradle.kts
