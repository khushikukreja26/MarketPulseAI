# services/kpi_service.py

from typing import List, Dict
# from services.firebase_service import get_firestore_client   # <- not needed for now

def get_kpis_for_org(org_id: str, timeframe: str = "weekly") -> List[Dict]:
    """
    TEMP VERSION: return dummy KPI data without touching Firestore.
    Later, we can re-enable Firestore once it’s set up correctly.
    """
    return [
        {"name": "Market Share", "value": 25.0, "change": 2.5},
        {"name": "Avg Price vs Competitor", "value": -3.2, "change": -1.1},
        {"name": "Campaigns Active", "value": 4, "change": 1.0},
    ]