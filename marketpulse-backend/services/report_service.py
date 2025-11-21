# services/report_service.py
from typing import Dict
from datetime import datetime
from services.firebase_service import get_firestore_client, send_fcm_notification
from services.kpi_service import get_kpis_for_org
from services.ai_service import generate_ai_insights_from_kpis

def generate_and_store_weekly_report(org_id: str) -> Dict:
    """
    Generate a weekly report for an org, store it in Firestore,
    and send an FCM notification to topic 'org_{org_id}'.
    """
    db = get_firestore_client()

    kpis = get_kpis_for_org(org_id, timeframe="weekly")
    insights = generate_ai_insights_from_kpis(kpis)

    report_data = {
        "orgId": org_id,
        "createdAt": datetime.utcnow().isoformat() + "Z",
        "kpis": kpis,
        "insights": insights,
        "title": f"Weekly MarketPulse Report for org {org_id}"
    }

    # Store in collection: weekly_reports/{orgId}/reports/{autoId}
    doc_ref = db.collection("weekly_reports").document(org_id).collection("reports").document()
    doc_ref.set(report_data)

    # Send notification (you can comment this out if not configured)
    try:
        send_fcm_notification(
            topic=f"org_{org_id}",
            title="New Weekly MarketPulse Report",
            body="Your latest competitor insights report is ready."
        )
    except Exception as e:
        print("Error sending FCM notification:", e)

    return report_data