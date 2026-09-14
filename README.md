# FastZen 🧘‍♂️
### Modern Intermittent Fasting Tracker & Wellness Companion for Android

FastZen is an Android application built with **Jetpack Compose** and **Material Design 3**, designed to guide you through mindful intermittent fasting, cellular renewal (autophagy), hydration tracking, and metabolic wellness.

---

## 🌟 Key Features

### ⏱️ Circular Fasting Timer & Metabolic Stages
- **Real-Time Cellular Zones**: Dynamically tracks biological fasting milestones:
  - **Fed State (0–4h)**: Digestive processing and blood glucose stabilization.
  - **Early Fast (4–12h)**: Glycogen consumption and insulin decrease.
  - **Ketosis (12–18h)**: Active liver glycogen depletion and fat oxidation.
  - **Deep Ketosis (18–24h)**: Elevated ketone production and mitochondrial energy.
  - **Autophagy (24h+)**: Cellular recycling, protein cleanup, and rejuvenation.
- **Adjust Start Time**: Retroactively backdate your fast if you forgot to start the timer after your last meal.
- **Custom Timer Duration**: Configure targets from 1 to 72 hours, with popular protocols:
  - **16:8 LeanGains**
  - **18:6 Accelerated**
  - **20:4 Warrior Diet**
  - **24h OMAD (One Meal A Day)**
  - **36h Monk Fast**
  - **Custom Target**

### 💧 Hydration & Symptom Logging
- **Water Tracker**: Daily progress toward your 2500ml target with quick-log buttons (+250ml).
- **Weight Diary**: Track weight trends over time with visual timestamped history.
- **Mindful Energy & Symptom Logs**: Record energy levels (1-5 stars) and feelings (Energized, Focused, Hungry wave).

### 📚 Interactive Fasting Knowledge & FAQs
- Categorized guides answering essential intermittent fasting questions:
  - *Drinks & Fasting*: Black coffee, unsweetened tea, apple cider vinegar, and clean fasting rules.
  - *Metabolism & Science*: Autophagy triggers, insulin spikes, and ketosis explanation.
  - *Hunger & Symptoms*: Managing ghrelin hunger waves, electrolytes, and pink salt remedies.
  - *Breaking a Fast*: Gentle food choices for opening the eating window.
  - *Safety & Fitness*: Working out fasted, electrolyte balance, and medical precautions.
- Real-time search and category filtering.

### 🔔 Smart Notification Alerts
- **Goal Reached**: Notifies you the minute your fasting target duration is completed.
- **Stage Milestones**: Celebrates transitions into Ketosis and Autophagy.
- **Hydration Reminders**: Keeps you hydrated throughout the fasting window.
- Notification testing affordance in Settings.

### 🎨 Themes & Customization
- **OLED Dark Canvas** ("Midnight Zen") & High-Contrast Light Mode.
- Multiple Zen accent palettes: Emerald Sage, Violet Lotus, Rosewood Sunset, Ocean Drift, and Pure Amber.

---

## 🛠️ Architecture & Tech Stack

- **Language**: Kotlin
- **UI Toolkit**: Jetpack Compose (100% declarative UI)
- **Design System**: Material Design 3 (M3)
- **Architecture**: Single Activity, MVVM state-driven architecture with clean unidirectional data flow
- **Minimum SDK**: Android API 26+ (Android 8.0 Oreo)
- **Target SDK**: Android API 34+

---

## 🚀 Building & Running

1. Clone this repository:
   ```bash
   git clone https://github.com/KUSHALMN/FastZen-Fasting-Tracke.git
   ```
2. Open the project in **Android Studio Hedgehog / Iguana / Jellyfish** or newer.
3. Sync Gradle and run on an Android device or emulator running API 26 or higher:
   ```bash
   ./gradlew installDebug
   ```

---

## 📄 License
This project is open-source and available under the MIT License.
