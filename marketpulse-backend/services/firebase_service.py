# services/firebase_service.py
import firebase_admin
from firebase_admin import credentials, firestore, messaging
from config import FIREBASE_CREDENTIALS_PATH, FIREBASE_PROJECT_ID

# Initialize Firebase app only once
if not firebase_admin._apps:
    cred = credentials.Certificate(FIREBASE_CREDENTIALS_PATH)
    firebase_admin.initialize_app(cred, {
        "projectId": FIREBASE_PROJECT_ID,
    })

db = firestore.client()

def get_firestore_client():
    return db

def send_fcm_notification(topic: str, title: str, body: str):
    """
    Send a push notification via FCM to a topic like 'org_123'.
    For now, you can comment this out if you don't want notifications.
    """
    message = messaging.Message(
        notification=messaging.Notification(
            title=title,
            body=body
        ),
        topic=topic
    )
    response = messaging.send(message)
    print(f"FCM message sent: {response}")