import requests
import json
import time
import sys

# Configuration
BASE_URL = "http://localhost:8080/api"
HEADERS = {'Content-Type': 'application/json'}

# Test Data
ORGANIZER_USER = {"username": "test_org", "password": "password123"}
VOLUNTEER_1 = {"username": "test_vol1", "password": "password123"}
VOLUNTEER_2 = {"username": "test_vol2", "password": "password123"}
VOLUNTEER_3 = {"username": "test_vol3", "password": "password123"}

def log(msg):
    print(f"[TEST] {msg}")

def login_or_register(user, role):
    # Try Login
    try:
        resp = requests.post(f"{BASE_URL}/auth/login", json=user, headers=HEADERS)
        if resp.status_code == 200 and resp.json().get('code') == 200:
            return resp.json()['data']
    except Exception:
        pass
    
    # Try Register if login failed (assuming register endpoint exists, otherwise we fail)
    # Based on file structure, there is a LoginController but maybe not Register. 
    # Let's assume users might exist. If not, this script might fail if registration isn't open.
    # Looking at LoginService, it only does login. 
    # But wait, the user instructions implied "Use your Token".
    # I will try to use the existing login endpoint. If it fails, I cannot create users via API.
    # I will just print the error.
    return None

def create_activity(token):
    log("Creating Activity with Quota 2...")
    data = {
        "title": "High Concurrency Test Activity",
        "content": "Limited to 2 people",
        "startTime": "2024-12-01T10:00:00",
        "endTime": "2024-12-01T12:00:00",
        "quota": 2,
        "location": "Online"
    }
    headers = HEADERS.copy()
    headers['Authorization'] = token
    resp = requests.post(f"{BASE_URL}/activity/create", json=data, headers=headers)
    log(f"Create Activity Response: {resp.text}")
    return resp

def register_activity(token, activity_id):
    headers = HEADERS.copy()
    headers['Authorization'] = token
    resp = requests.post(f"{BASE_URL}/activity/register/{activity_id}", headers=headers)
    return resp

def main():
    # 1. Login Organizer
    # Note: Since I don't have registration API in context, I'm assuming these users MIGHT exist 
    # or I'm relying on the user to have them. 
    # However, to be helpful, I'll validly try to login. 
    # If login fails, I'll stop.
    
    # Actually, I can INSERT users into DB using a separate SQL script or just assume.
    # But I can't run SQL easily.
    # Let's try to simulate the flow assuming the user will handle the "User Account" part 
    # or I will just output the CURL commands for them.
    
    # Wait, the user asked ME to "Release test" and "Normal registration". 
    # This implies I should do it.
    
    # I will create a python script that *attempts* to do this.
    pass

if __name__ == "__main__":
    main()
