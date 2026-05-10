import requests
import time
import random
from datetime import datetime
import os

# BART stations and lines — fake data modelled on real BART structure
STATIONS = ["Montgomery St", "Powell St", "Civic Center", "16th St Mission", "24th St Mission"]
LINES = ["Red", "Yellow", "Blue", "Green", "Orange"]
DESTINATIONS = {"Red": "Richmond", "Yellow": "Antioch", "Blue": "Dublin/Pleasanton",
                "Green": "Berryessa", "Orange": "Berryessa"}
STATUSES = ["on time", "on time", "on time", "delayed", "delayed", "cancelled"]

SPRING_BOOT_URL = os.getenv("SPRING_BOOT_URL", "http://localhost:8080/api/bart/events")

def generate_event():
    line = random.choice(LINES)
    return {
        "station": random.choice(STATIONS),
        "line": line,
        "destination": DESTINATIONS[line],
        "minutes": random.randint(1, 20),
        "status": random.choice(STATUSES),
        "timestamp": datetime.now().isoformat()
    }

def send_event(event):
    try:
        response = requests.post(SPRING_BOOT_URL, json=event, timeout=5)
        print(f"✅ Sent | {event['station']} | {event['line']} line | {event['minutes']} min | {event['status']}")
        print(f"   Response: {response.text}\n")
    except requests.exceptions.ConnectionError:
        print(f"❌ Could not connect to Spring Boot at {SPRING_BOOT_URL}")
        print("   Make sure the Spring Boot service is running.\n")

if __name__ == "__main__":
    print("🚇 BART Event CLI — sending events every 2 seconds")
    print(f"   Target: {SPRING_BOOT_URL}\n")
    while True:
        event = generate_event()
        send_event(event)
        time.sleep(2)
