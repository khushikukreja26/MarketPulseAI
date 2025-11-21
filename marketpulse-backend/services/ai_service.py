# services/ai_service.py

from typing import List, Dict
import json
import re

import google.generativeai as genai
from config import GEMINI_API_KEY, GEMINI_MODEL

# ---------- 1. Configure Gemini (if key exists) ----------

if GEMINI_API_KEY:
    genai.configure(api_key=GEMINI_API_KEY)
else:
    print("GEMINI_API_KEY not set. AI insights will fall back to rule-based logic.")

def _clean_json_from_text(text: str) -> str:
    """
    Sometimes models wrap JSON in ``` ``` or add extra text.
    This tries to extract the first {...} block.
    """
    # Try to find first { ... } block
    match = re.search(r"\{.*\}", text, re.DOTALL)
    if match:
        return match.group(0)
    return text.strip()

# ---------- 2. Old rule-based insights (fallback) ----------

def _rule_based_insights(kpis: List[Dict]) -> Dict:
    """
    Simple rule-based logic, used as fallback when Gemini fails
    or if no API key is configured.
    """
    if not kpis:
        return {
            "summary": "No KPI data available for the selected period.",
            "recommendations": [
                "Please ensure data collection is configured correctly.",
                "Verify that competitor signals are being tracked."
            ],
            "risk_score": 0
        }

    positive = []
    negative = []

    for kpi in kpis:
        name = kpi.get("name", "Unknown KPI")
        change = kpi.get("change", 0)
        value = kpi.get("value", 0)

        if change > 0:
            positive.append(f"{name} improved by {change:.2f} points (current: {value}).")
        elif change < 0:
            negative.append(f"{name} declined by {abs(change):.2f} points (current: {value}).")

    summary_parts = []
    if positive:
        summary_parts.append("Strengths: " + " ".join(positive))
    if negative:
        summary_parts.append("Weaknesses: " + " ".join(negative))
    if not summary_parts:
        summary_parts.append("KPI performance is stable with no significant changes.")

    recommendations = []
    if negative:
        recommendations.append(
            "Focus on improving the KPIs that declined. Consider targeted campaigns "
            "and pricing adjustments where performance dropped."
        )
    if positive:
        recommendations.append(
            "Double down on areas where KPIs improved. Allocate more budget and resources "
            "to reinforce these strengths."
        )

    risk_score = max(0, min(100, 50 - len(positive) * 5 + len(negative) * 10))

    return {
        "summary": " ".join(summary_parts),
        "recommendations": recommendations,
        "risk_score": risk_score
    }

# ---------- 3. Gemini-powered insights ----------

def generate_ai_insights_from_kpis(kpis: List[Dict]) -> Dict:
    """
    Main function used by Flask endpoints.

    - If GEMINI_API_KEY is set, call Gemini to generate insights.
    - If anything fails (no key, network, parsing), fall back to rule-based logic.
    """
    # If no key configured, fallback
    if not GEMINI_API_KEY:
        print("GEMINI_API_KEY missing, using rule-based insights.")
        return _rule_based_insights(kpis)

    if not kpis:
        return _rule_based_insights(kpis)

    try:
        # Prepare prompt
        kpis_json = json.dumps(kpis, indent=2)

        system_instructions = (
            "You are a senior competitive strategy analyst for a SaaS company. "
            "You receive KPI metrics about competitor performance and must generate "
            "clear business insights for a product/marketing manager. "
            "Always respond with VALID JSON only."
        )

        user_prompt = f"""
Here are the weekly competitor KPIs in JSON:

{kpis_json}

Analyze these KPIs and respond ONLY in valid JSON with this exact structure:

{{
  "summary": "2-4 sentences describing key strengths and weaknesses.",
  "recommendations": [
    "Actionable recommendation 1",
    "Actionable recommendation 2",
    "Actionable recommendation 3"
  ],
  "risk_score": 0
}}

Rules:
- Do NOT include any extra commentary outside the JSON.
- "summary": short, executive-level explanation.
- "recommendations": 2-4 concrete, practical actions.
- "risk_score": integer 0 (no risk) to 100 (very high risk).
"""

        model = genai.GenerativeModel(GEMINI_MODEL)

        response = model.generate_content(
            contents=[
                system_instructions,
                user_prompt
            ]
        )

        raw_text = response.text or ""
        json_str = _clean_json_from_text(raw_text)
        data = json.loads(json_str)

        summary = str(data.get("summary", "")).strip()
        recs = data.get("recommendations", [])
        risk = data.get("risk_score", 50)

        if not isinstance(recs, list):
            recs = [str(recs)]
        recs = [str(r).strip() for r in recs if str(r).strip()]

        try:
            risk_int = int(risk)
        except Exception:
            risk_int = 50

        risk_int = max(0, min(100, risk_int))

        return {
            "summary": summary,
            "recommendations": recs,
            "risk_score": risk_int
        }

    except Exception as e:
        print("Error in generate_ai_insights_from_kpis (Gemini):", e)
        return _rule_based_insights(kpis)