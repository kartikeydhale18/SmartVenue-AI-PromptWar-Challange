# SmartVenue AI 🏟️🚀

**SmartVenue AI** is an intelligent, real-time Android application designed to elevate the fan experience at large-scale stadiums and event venues. Built specifically for hackathon demonstrations, the platform integrates precise indoor geonavigation, crowdsourced congestion analytics, and live queue tracking into a seamless UI.

---

## 🌟 Key Features

### 📍 Interactive Indoor Mapping (Powered by MapLibre)
- **Ring-Routing Navigation:** Implements a proprietary curved routing algorithm that precisely maps pathways along stadium concourses—guaranteeing navigation lines never erroneously intersect the center playfield.
- **Dynamic Origin Matrix:** Users can tap to seamlessly trace navigational routes across multiple custom points of interest (e.g., Gate A, Food Stall 1, Restroom).
- **Live Crowd Heatmaps:** Renders an interactive, opacity-stacking geolocation heatmap built specifically on custom MapLibre overlay layers.

### 👥 Real-Time Crowdsourcing Pipeline
- **Profile-To-Map Sync:** Users can report crowd congestion levels natively through their in-app Profile.
- **Instant Map Flares:** Crowd reports push telemetry to Firebase, instantly re-rendering the geospatial MapLibre heatmap with new orange density clusters visible to all users. 
- **Queue Autopilot:** Logging a High/Medium/Low crowd event systematically links into the venue's active Alert Queue—creating or modifying Wait Time counters natively without manual database intervention.

### ⚡ Cloud Architecture (Firebase)
- **Firestore Data Streaming:** Employs optimized `SnapshotListeners` for instantaneous UI updates across the entire application ecosystem without manual screen refreshes.
- **Authentication:** Fully native Firebase User Authentication with integrated realtime Data Binding for user profiles.

---

## 🛠️ Tech Stack & Architecture
- **Language:** Java
- **UI Framework:** Android / XML
- **Mapping Engine:** MapLibre GL Native
- **Backend:** Google Firebase (Auth + Cloud Firestore)
- **Design:** Modern dark-mode architecture with tailored glassmorphic tendencies. 

---

## 🚀 Getting Started

1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-username/SmartVenue-AI.git
   ```
2. **Setup Firebase:**
   - Create a project on the [Firebase Console](https://console.firebase.google.com/).
   - Add your Android app package name.
   - Download the generated `google-services.json` file and place it rigidly inside the `app/` directory (Note: This is strictly ignored by version control for security).
3. **Database Configuration:**
   - Enable **Cloud Firestore** and deploy in Test Mode (or apply necessary security policies to the `users`, `crowd_reports`, and `queues` collections).
4. **Build & Run:**
   - Sync the Gradle environments via Android Studio and deploy onto a physical device or emulator (API 24+).

---

## 🔒 Security Notice
To protect your Cloud limits and infrastructure pipelines, the `.gitignore` has been forcefully configured to ignore `google-services.json`, your local `keystore.properties`, and `.env` secrets. Ensure you never hardcode backend keys directly into the remote repository structure.
