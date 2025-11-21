# app.py
from flask import Flask, jsonify, request
from flask_cors import CORS  # optional if you want to call from web later
from services.kpi_service import get_kpis_for_org
from services.ai_service import generate_ai_insights_from_kpis
from services.report_service import generate_and_store_weekly_report

app = Flask(__name__)
CORS(app)  # Allow cross-origin requests (useful when testing from browser)

@app.route("/")
def health_check():
    return jsonify({"status": "ok", "message": "MarketPulse AI backend is running"})

@app.route("/api/kpis", methods=["GET"])
def api_get_kpis():
    org_id = request.args.get("orgId")
    timeframe = request.args.get("timeframe", "weekly")

    if not org_id:
        return jsonify({"error": "Missing orgId query param"}), 400

    kpis = get_kpis_for_org(org_id, timeframe)
    return jsonify({"orgId": org_id, "timeframe": timeframe, "metrics": kpis})

@app.route("/api/insights", methods=["POST"])
def api_generate_insights():
    data = request.get_json() or {}
    org_id = data.get("orgId")
    timeframe = data.get("timeframe", "weekly")

    if not org_id:
        return jsonify({"error": "Missing orgId in request body"}), 400

    # Option 1: read KPIs directly from Firestore
    kpis = get_kpis_for_org(org_id, timeframe)

    # Option 2 (later): allow client to pass KPIs directly
    # kpis = data.get("metrics", [])

    insights = generate_ai_insights_from_kpis(kpis)

    return jsonify({
        "orgId": org_id,
        "timeframe": timeframe,
        "kpis": kpis,
        "insights": insights
    })

@app.route("/api/generate-weekly-report", methods=["POST"])
def api_generate_weekly_report():
    data = request.get_json() or {}
    org_id = data.get("orgId")

    if not org_id:
        return jsonify({"error": "Missing orgId in request body"}), 400

    report = generate_and_store_weekly_report(org_id)
    return jsonify(report)

if __name__ == "__main__":
    # Run on all interfaces so Android emulator can reach it
    app.run(host="0.0.0.0", port=5000, debug=True)