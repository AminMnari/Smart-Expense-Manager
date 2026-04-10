# Smart Expense Manager

A personal finance optimization app that helps users track expenses, detect spending anomalies, and receive AI-powered financial insights. Built with Kotlin, Jetpack Compose, and Google's Gemini AI.

## Getting Started

### Prerequisites
- Android Studio Hedgehog or later
- Android device or emulator running API 26+
- A free Gemini API key from https://aistudio.google.com

### Setup
1. Clone the repository:
   git clone https://github.com/AminMnari/Smart-Expense-Manager.git

2. Open the project in Android Studio

3. Create a local.properties file in the project root
   (this file is not included in the repo for security reasons)
   Add these two lines:
     sdk.dir=YOUR_ANDROID_SDK_PATH
     GEMINI_API_KEY=YOUR_GEMINI_API_KEY

   Example sdk.dir values:
     Windows: sdk.dir=C\:\\Users\\YourName\\AppData\\Local\\Android\\Sdk
     Mac:     sdk.dir=/Users/YourName/Library/Android/sdk
     Linux:   sdk.dir=/home/YourName/Android/Sdk

4. Sync Gradle: File -> Sync Project with Gradle Files

5. Run the app on an emulator or physical device (API 26+)

### Notes
- The app works offline for all features except
  AI receipt scanning and insight generation
- Camera permission will be requested on first use
- Categories are seeded automatically on first launch

## Features

- **Receipt Scanning & OCR** - Capture receipts using your device camera, automatically extract expense details with ML Kit text recognition
- **AI-Powered Expense Parsing** - Gemini AI intelligently parses receipt text and categorizes expenses with confidence scores
- **Monthly Expense Tracking** - View spending by category, date range, and custom filters with real-time summaries
- **Budget Management** - Set monthly budgets per category with customizable alert thresholds
- **Anomaly Detection** - Automatic detection of unusual spending spikes with weekly-to-average comparisons
- **Smart Alerts** - Real-time notifications when spending exceeds set thresholds or anomalies are detected
- **Financial Insights** - AI-generated monthly reports with spending analysis, trends, and personalized savings tips
- **Offline-First** - All expense data stored locally with Room database; AI features work with internet
- **Material Design 3** - Modern, accessible UI with Material Design 3 and dark mode support

## Tech Stack

| Component | Library | Purpose | Version |
|-----------|---------|---------|---------|
| **UI** | Jetpack Compose | Modern declarative UI framework | 2025.03.01 |
| **Navigation** | Compose Navigation | Type-safe routing between screens | 2.8.9 |
| **Database** | Room | Local SQLite persistence with type safety | 2.7.1 |
| **DI** | Hilt | Dependency injection at compile-time | 2.59 |
| **Async** | Coroutines | Async/await with Flow for reactive programming | 1.9.0 |
| **Networking** | Retrofit + OkHttp | HTTP client with interceptors | 2.11.0 + 4.12.0 |
| **JSON** | Gson | JSON serialization/deserialization | 2.11.0 |
| **OCR** | ML Kit Text Recognition | On-device receipt text extraction | 16.0.1 |
| **AI** | Gemini API | Receipt parsing and insight generation | gemini-2.5-flash |
| **Image Loading** | Coil | Efficient Compose image loading | 2.7.0 |
| **Background Jobs** | WorkManager | Scheduled anomaly detection tasks | 2.10.1 |
| **Charts** | MPAndroidChart | Spending visualization and trends | 3.1.0 |
| **Desugaring** | Desugar JDK Libs | Java 8+ APIs on API 26+ | 2.1.1 |

## Project Structure

```
SmartExpenseManager/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/smartexpense/
│   │   │   │   ├── data/
│   │   │   │   │   ├── local/
│   │   │   │   │   │   ├── db/              ← Room database setup
│   │   │   │   │   │   ├── dao/             ← Data Access Objects
│   │   │   │   │   │   └── entity/          ← Room entities
│   │   │   │   │   ├── mapper/              ← Entity ↔ Domain converters
│   │   │   │   │   ├── remote/
│   │   │   │   │   │   ├── gemini/          ← Gemini API client
│   │   │   │   │   │   └── ocr/             ← ML Kit wrapper
│   │   │   │   │   └── repository/          ← Repository implementations
│   │   │   │   ├── domain/
│   │   │   │   │   ├── model/               ← Domain data classes
│   │   │   │   │   ├── repository/          ← Repository interfaces
│   │   │   │   │   └── usecase/             ← Business logic orchestrators
│   │   │   │   ├── presentation/
│   │   │   │   │   ├── scan/                ← Receipt scanning screen
│   │   │   │   │   ├── dashboard/           ← Monthly summary screen
│   │   │   │   │   ├── expenses/            ← Expense list/detail screens
│   │   │   │   │   ├── budget/              ← Budget management screen
│   │   │   │   │   ├── alerts/              ← Anomaly alerts screen
│   │   │   │   │   ├── insights/            ← AI insights screen
│   │   │   │   │   ├── navigation/          ← Navigation routing
│   │   │   │   │   └── (ViewModels + State)
│   │   │   │   ├── di/                      ← Hilt dependency injection
│   │   │   │   ├── worker/                  ← WorkManager tasks (future)
│   │   │   │   ├── ui/theme/                ← Material Design 3 theme
│   │   │   │   ├── MainActivity.kt          ← App entry activity
│   │   │   │   └── SmartExpenseApplication.kt ← Hilt app class
│   │   │   ├── res/                         ← Resources (strings, colors, etc)
│   │   │   └── AndroidManifest.xml
│   ├── build.gradle.kts                     ← App dependencies
│   └── schemas/                             ← Room schema migration history
├── gradle/
│   ├── libs.versions.toml                   ← Version catalog
│   └── wrapper/
├── build.gradle.kts                         ← Project-level config
├── settings.gradle.kts                      ← Module configuration
├── gradle.properties                        ← Build properties
├── .gitignore                               ← Git exclusions
├── README.md                                ← This file
├── PROJECT_SUMMARY.md                       ← Development reference
├── project-context.md                       ← Architecture spec
└── local.properties                         ← Local config (git-ignored)
```

## Architecture

The app follows a **Clean Architecture** with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                       │
│  Screens (Compose) ← NavGraph ← ViewModels + State         │
│         ↓                                                   │
└─────────────────────────────────────────────────────────────┘
         ↓
┌─────────────────────────────────────────────────────────────┐
│                      DOMAIN LAYER                           │
│  Use Cases ← Business Logic ← Repository Interfaces        │
│         ↓                                                   │
│  Models (Data Classes)                                     │
└─────────────────────────────────────────────────────────────┘
         ↓
┌─────────────────────────────────────────────────────────────┐
│                      DATA LAYER                             │
│  ┌────────────────────┬──────────────────┐                 │
│  │ LOCAL (Room)       │  REMOTE (APIs)   │                 │
│  │ ├─ DAOs            │ ├─ ML Kit OCR    │                 │
│  │ ├─ Entities        │ ├─ Gemini API    │                 │
│  │ └─ Database        │ └─ Payloads      │                 │
│  │    ↓               │                  │                 │
│  │ Mappers (Entity ↔ Domain)            │                 │
│  └────────────────────┴──────────────────┘                 │
│         ↓                                                   │
│  Repositories (DI via Hilt)                                │
└─────────────────────────────────────────────────────────────┘
```

### Key Principles
- **Single Responsibility**: Each layer has one reason to change
- **Dependency Inversion**: High-level modules depend on abstractions, not implementations
- **No Coupling**: UI never directly accesses data layer
- **Testability**: Use cases and repositories can be tested in isolation
- **Reactive**: Flow-based state management with Coroutines

## Current Status

### ✅ Complete
- Full Clean Architecture implementation (data, domain, presentation)
- Room database with 4 entities, DAOs, TypeConverters, and category seeding
- ML Kit OCR on-device receipt text extraction
- Gemini 2.5 Flash AI integration for expense parsing and monthly insights
- All 9 Compose screens fully implemented and wired
- Bottom navigation with 4 tabs (Dashboard, Scan, Alerts, Insights)
- WorkManager daily anomaly detection with push notifications
- Real-time dashboard with Flow-based expense updates
- Budget management with per-category limits and thresholds
- AI Insights with real category names and empty-month guard
- Language switcher supporting English, French, and Arabic
- Currency selector supporting TND, USD, EUR, GBP, SAR, AED, MAD, DZD
- Clear all data with Room table wipe and app restart
- Camera integration with FileProvider and runtime permission handling
- 12 unit tests passing (use cases, mappers, repositories)
- 25 Git commits showing incremental development progress

### 📋 Future Improvements
- Dashboard pie/bar charts using MPAndroidChart
- Expense list accessible from bottom navigation
- Receipt image saved and displayed in expense detail screen
- Firebase Cloud Messaging for remote push notifications
- Room schema migrations for future database changes
- Export expenses as PDF or CSV

## Development Workflow

1. **Feature Branch**: `git checkout -b feat/your-feature`
2. **Code Changes**: Implement feature following the architecture
3. **Test**: Run `./gradlew test` for unit tests
4. **Build**: Run `./gradlew :app:assembleDebug` to verify
5. **Commit**: Use conventional commit messages
6. **Push**: `git push origin feat/your-feature`
7. **PR**: Open a pull request for review

## Building & Testing

### Debug Build
```bash
./gradlew :app:assembleDebug
```

### Release Build
```bash
./gradlew :app:assembleRelease
```

### Run Tests
```bash
./gradlew test
```

### Check Lint
```bash
./gradlew lint
```

### Clean Build
```bash
./gradlew clean :app:compileDebugKotlin
```

## Troubleshooting

### Gradle sync fails
- Check `JAVA_HOME` points to JDK 11+
- Delete `.gradle` directory and retry
- Update Android Studio to latest stable version

### Gemini API errors
- Verify `GEMINI_API_KEY` in `local.properties`
- Check API key is valid at https://aistudio.google.com
- Ensure internet connection is working

### Room schema conflicts
- Room schema is auto-generated; keep `app/schemas/` tracked for migrations
- If deleted, rebuild the project: `./gradlew clean :app:compileDebugKotlin`

## License

This project is licensed under the MIT License — see the LICENSE file for details.

## Contributing

Contributions are welcome! Please follow the commit message conventions and ensure all tests pass before submitting a pull request.

## Contact

For questions or suggestions, open an issue on GitHub or contact the maintainers.

---

**Last Updated**: April 2026  
**Project Status**: Fully functional — tested on Android device  
**GitHub**: https://github.com/AminMnari/Smart-Expense-Manager

