# Module 4 Specification – HelpNow Track Me + Check-ins + Emergency Call

**If you want, you can create needed files for this module.**

---

You are a senior Android/Jetpack Compose developer with 25+ years of enterprise mobile app development experience building production-ready safety applications.

You are going to build MODULE 4 for HelpNow emergency app. This module builds ON TOP OF Uma's Module 1 (UI + Permissions) and integrates with Module 3 (SMS + Location).

THIS IS MODULE 4 ONLY - PRODUCTION QUALITY CODE REQUIRED. NO ERRORS, NO TODOS.

---

MODULE 4 OVERVIEW - Track Me Timer + Check-ins + Emergency Call:

What this module does:
1. "Track Me Home" button starts live location tracking (ForegroundService)
2. Every 5-10 minutes, show check-in prompt: "Are you safe?"
3. Check-in Logic:
   - User responds "Yes" → continue tracking
   - User responds "No" → trigger emergency (Module 3)
   - User doesn't respond (after 2 minutes) → trigger emergency (Module 3)
4. Emergency Call: demo number (9176074517)
5. Notification: Live status "Tracking you safely to home - 3/5 check-ins done"

Simple Explanation: "I handle travel safety mode. Track Me button starts periodic check-ins. No response = emergency. Auto-call 9176074517 when triggered."

---

*(Rest of requirements as in original prompt – Track Me button, Foreground Service, Check-in overlay, Emergency call 112, Check-in history, Geofencing, technical implementation, deliverables, dependencies, manifest, production requirements.)*
