# Smart Expense Manager - Project Summary

## Project Overview
Smart Expense Manager is a personal finance optimization app built with Kotlin, Jetpack Compose, and Material Design 3. The application uses a clean architecture with MVVM, Room for offline-first data persistence, ML Kit for receipt OCR, and Gemini AI for intelligent expense parsing and financial insights generation.

## Complete Generated Files by Layer

### Domain Layer
- **domain/model/Expense.kt** - Core expense data model with timestamps and categorization
- **domain/model/Category.kt** - Expense category definition with icon and color
- **domain/model/Budget.kt** - Monthly budget per category with alert thresholds
- **domain/model/AnomalyEvent.kt** - Detected spending spike event with comparative metrics
- **domain/model/WeeklyTotal.kt** - Aggregated weekly spending projection for anomaly detection
- **domain/model/InsightResult.kt** - AI-generated monthly financial insight report
- **domain/model/CategorySpend.kt** - Category-level spend with trend indicator
- **domain/model/ParsedExpense.kt** - Temporary model for OCR/AI parsed receipt data

- **domain/repository/ExpenseRepository.kt** - Contract for expense CRUD and filtering operations
- **domain/repository/BudgetRepository.kt** - Contract for monthly budget management
- **domain/repository/InsightRepository.kt** - Contract for insight generation and caching

- **domain/usecase/GetExpensesUseCase.kt** - Orchestrator for loading expenses with various filters
- **domain/usecase/SaveExpenseUseCase.kt** - Orchestrator for persisting expense changes
- **domain/usecase/ScanReceiptUseCase.kt** - Orchestrator for receipt capture → OCR → AI parsing pipeline
- **domain/usecase/DetectAnomaliesUseCase.kt** - Orchestrator for weekly spending anomaly detection
- **domain/usecase/GenerateInsightUseCase.kt** - Orchestrator for monthly insight generation with caching

### Data Layer

#### Local (Room)
- **data/local/db/AppDatabase.kt** - Room database with all entities, DAOs, and type converters
- **data/local/db/Converters.kt** - Room type converters for LocalDate, LocalDateTime, YearMonth

- **data/local/entity/ExpenseEntity.kt** - Room entity for expenses table with FK index on category_id
- **data/local/entity/CategoryEntity.kt** - Room entity for categories table
- **data/local/entity/BudgetEntity.kt** - Room entity for budgets table with FK index on category_id
- **data/local/entity/AnomalyEventEntity.kt** - Room entity for anomaly events table with FK index on category_id

- **data/local/dao/ExpenseDao.kt** - DAO for expense queries and mutations with Flow support
- **data/local/dao/CategoryDao.kt** - DAO for category operations
- **data/local/dao/BudgetDao.kt** - DAO for budget upsert/delete operations
- **data/local/dao/AnomalyEventDao.kt** - DAO for anomaly event logging and retrieval

#### Remote (Networking)
- **data/remote/ocr/MlKitOcrService.kt** - ML Kit text recognition wrapper for on-device OCR
- **data/remote/gemini/GeminiApiService.kt** - Gemini API client for expense parsing and insight generation
- **data/remote/gemini/GeminiRequest.kt** - Gemini API request payload structures
- **data/remote/gemini/GeminiResponse.kt** - Gemini API response payload structures

#### Mappers
- **data/mapper/EntityMappers.kt** - Bidirectional converters between Room entities and domain models

#### Repositories
- **data/repository/ExpenseRepositoryImpl.kt** - Implementation of ExpenseRepository with DAO delegation
- **data/repository/BudgetRepositoryImpl.kt** - Implementation of BudgetRepository with DAO delegation
- **data/repository/InsightRepositoryImpl.kt** - Implementation of InsightRepository with Gemini integration and local fallback

### Dependency Injection
- **di/DatabaseModule.kt** - Hilt module providing Room database, DAOs, OkHttp client, and services

### Presentation Layer

#### Navigation
- **presentation/navigation/Screen.kt** - Sealed class defining all app navigation routes
- **presentation/navigation/NavGraph.kt** - Compose navigation graph with placeholder screens for all routes

#### ViewModels & State
- **presentation/scan/ScanViewModel.kt** - Handles receipt capture, OCR/parsing, and expense review workflow with ScanUiState
- **presentation/dashboard/DashboardViewModel.kt** - Manages monthly expense summary, category breakdown with DashboardUiState
- **presentation/budget/BudgetViewModel.kt** - Manages budget CRUD operations with BudgetUiState
- **presentation/alerts/AlertViewModel.kt** - Manages anomaly detection and alert display with AlertUiState
- **presentation/insights/InsightViewModel.kt** - Manages AI insight generation and caching with InsightUiState
- **presentation/expenses/ExpenseViewModel.kt** - Manages expense listing and filtering with ExpenseUiState and ExpenseFilter

#### Application
- **MainActivity.kt** - Hilt-enabled main activity hosting the NavGraph
- **SmartExpenseApplication.kt** - HiltAndroidApp entry point for DI initialization
- **ui/theme/Color.kt** - Material Design 3 color palette
- **ui/theme/Theme.kt** - Theme composition with dynamic and dark mode support
- **ui/theme/Type.kt** - Material Design 3 typography scale

### Configuration
- **AndroidManifest.xml** - App manifest with application class and main activity registration
- **app/build.gradle.kts** - App module Gradle configuration with all dependencies
- **gradle/libs.versions.toml** - Centralized version catalog for dependencies and plugins

---

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                       │
│                                                             │
│  Screens (Placeholders) ← NavGraph ← Screen Routes         │
│         ↓                                                   │
│  ViewModels (6 total) + UI States                          │
│         ↓                                                   │
└─────────────────────────────────────────────────────────────┘
         ↓
┌─────────────────────────────────────────────────────────────┐
│                      DOMAIN LAYER                           │
│                                                             │
│  Use Cases (5 total) ← Business Logic Orchestration        │
│         ↓                                                   │
│  Interfaces (Repository & Model Contracts)                 │
│         ↓                                                   │
│  Models (8 data classes)                                   │
└─────────────────────────────────────────────────────────────┘
         ↓
┌─────────────────────────────────────────────────────────────┐
│                      DATA LAYER                             │
│                                                             │
│  Repositories (3 impl) ← DI Injection (Hilt)              │
│         ↓                                                   │
│  ┌────────────────┬──────────────────┐                     │
│  │ LOCAL          │  REMOTE          │                     │
│  │ (Room)         │  (Networking)    │                     │
│  │                │                  │                     │
│  │ ├─ DAOs (4)    │ ├─ ML Kit OCR   │                     │
│  │ ├─ Entities(4) │ ├─ Gemini API   │                     │
│  │ ├─ Database    │ └─ Payloads     │                     │
│  │ └─ Converters  │                  │                     │
│  │    ↓           │                  │                     │
│  │ Mappers (Entity ← → Domain)       │                     │
│  └────────────────┴──────────────────┘                     │
└─────────────────────────────────────────────────────────────┘
```

---

## Third-Party Libraries & Versions

### Core & Lifecycle
- `androidx.core:core-ktx` → 1.17.0
- `androidx.lifecycle:lifecycle-runtime-ktx` → 2.10.0
- `androidx.lifecycle:lifecycle-viewmodel-ktx` → 2.10.0
- `androidx.lifecycle:lifecycle-viewmodel-compose` → 2.10.0
- `androidx.lifecycle:lifecycle-runtime-compose` → 2.10.0
- `androidx.activity:activity-compose` → 1.10.1

### Compose & Navigation
- `androidx.compose:compose-bom` → 2025.03.01 (BOM-managed versions)
- `androidx.compose.ui:ui`
- `androidx.compose.ui:ui-graphics`
- `androidx.compose.ui:ui-tooling`
- `androidx.compose.ui:ui-tooling-preview`
- `androidx.compose.material3:material3`
- `androidx.compose.material:material-icons-extended`
- `androidx.navigation:navigation-compose` → 2.8.9

### Room (Database)
- `androidx.room:room-runtime` → 2.7.1
- `androidx.room:room-ktx` → 2.7.1 (KSP processor)

### Hilt (Dependency Injection)
- `com.google.dagger:hilt-android` → 2.59 (KSP processor)
- `androidx.hilt:hilt-navigation-compose` → 1.2.0
- `androidx.hilt:hilt-work` → 1.2.0 (KSP processor)

### Networking
- `com.squareup.retrofit2:retrofit` → 2.11.0
- `com.squareup.retrofit2:converter-gson` → 2.11.0
- `com.squareup.okhttp3:logging-interceptor` → 4.12.0
- `com.google.code.gson:gson` → 2.11.0

### Coroutines
- `org.jetbrains.kotlinx:kotlinx-coroutines-core` → 1.9.0
- `org.jetbrains.kotlinx:kotlinx-coroutines-android` → 1.9.0
- `org.jetbrains.kotlinx:kotlinx-coroutines-test` → 1.9.0 (test)

### AI & Media
- `com.google.mlkit:text-recognition` → 16.0.1 (on-device OCR)
- `io.coil-kt:coil-compose` → 2.7.0 (image loading)

### Background Processing
- `androidx.work:work-runtime-ktx` → 2.10.1

### Charting
- `com.github.PhilJay:MPAndroidChart` → 3.1.0 (via JitPack)

### Java Desugaring
- `com.android.tools:desugar_jdk_libs` → 2.1.1

### Build & KSP
- `com.android.application` (AGP) → 9.0.1
- `org.jetbrains.kotlin.plugin.compose` → 2.0.21
- `com.google.devtools.ksp` → 2.0.21-1.0.27

### Testing
- `junit:junit` → 4.13.2
- `androidx.test.ext:junit` → 1.3.0
- `androidx.test.espresso:espresso-core` → 3.7.0
- `androidx.compose.ui:ui-test-junit4` (BOM-managed)

---

## Known Limitations

### Current State
1. **No UI Screens Yet** - All presentation routes are placeholder composables
2. **Mock Insight Generation** - Fallback local insight builder lacks real Gemini API calls (when offline)
3. **No Camera Implementation** - ScanViewModel expects bitmap input but no camera capture UI
4. **No Notifications** - Anomaly alerts detected but not surfaced as push notifications
5. **No Background Sync** - WorkManager scaffolded but not wired to AnomalyWorker
6. **Single-User Only** - No multi-account or cloud sync support

### Build & Environment
1. **Kotlin JVM Target Fallback** - JDK 25 falls back to JVM 24 (non-critical)
2. **Schema Export Disabled** - Room schema migrations not tracked yet
3. **Foreign Key Indexes** - Implicit Room index creation (Room warnings suppressed)
4. **Experimental KSP Mode** - `android.disallowKotlinSourceSets=false` is experimental

---

## Future Improvements

### Short Term (Next Phase)
1. **Implement Feature Screens** - Create Compose UIs for all 6 screen routes
2. **Wire Camera Capture** - Integrate camera intent and bitmap delivery to ScanViewModel
3. **Enable WorkManager** - Connect AnomalyWorker to DetectAnomaliesUseCase with daily triggers
4. **Push Notifications** - Add Firebase Cloud Messaging for anomaly alerts
5. **Add Unit Tests** - Test use cases, repositories, and ViewModels with coroutines-test

### Medium Term
1. **Implement Data Migrations** - Version Room database and handle schema evolution
2. **Category Management** - UI for custom category creation and management
3. **Receipt Image Storage** - Persist receipt images to app storage or cloud
4. **Expense Attachments** - Link receipts to expenses for audit trail
5. **Export Reports** - PDF/CSV export of monthly spending summaries

### Long Term
1. **Cloud Sync** - Firebase Firestore for cross-device data sync
2. **Multi-User** - Account management and expense sharing between users
3. **Recurring Expenses** - Subscription tracking and auto-categorization
4. **Advanced Charts** - Dashboard graphs showing spending trends and forecasts
5. **Mobile Pay Integration** - Direct bank/card API connection for transaction import
6. **Budgeting AI** - Predictive budgeting based on spending patterns

---

## Git Commit Checklist

Use these logical commit points as you continue development:

- [ ] `feat: add gradle version catalog and dependencies`
- [ ] `feat: scaffold Room database layer with entities, DAOs, and converters`
- [ ] `feat: implement Room foreign key indexes for category_id`
- [ ] `feat: add entity mappers for domain/entity conversion`
- [ ] `feat: implement repository layer with DAO delegation`
- [ ] `feat: wire Hilt DI with AppDatabase, services, and repositories`
- [ ] `feat: add domain models and repository interfaces`
- [ ] `feat: implement all five use cases with business logic`
- [ ] `feat: add ML Kit OCR service wrapper`
- [ ] `feat: implement Gemini API client with expense parsing`
- [ ] `feat: add all six ViewModels with state management`
- [ ] `feat: scaffold navigation graph with placeholder screens`
- [ ] `feat: initialize Hilt AndroidApp entry point`
- [ ] `feat: implement MainActivity as Hilt activity`

**Post-Summary Commits:**
- [ ] `feat: create ScanScreen with camera and receipt review UI`
- [ ] `feat: create DashboardScreen with monthly expense summary`
- [ ] `feat: create BudgetScreen for monthly budget management`
- [ ] `feat: create AlertsScreen for anomaly notifications`
- [ ] `feat: create InsightScreen for AI-generated financial insights`
- [ ] `feat: create ExpenseListScreen with filtering and search`
- [ ] `test: add unit tests for use cases and ViewModels`
- [ ] `feat: enable WorkManager background anomaly detection`
- [ ] `feat: add Firebase Cloud Messaging for push notifications`

---

## Next Steps

1. **Sync Android Studio**: File → Sync Project with Gradle Files
2. **Set Gemini API Key**: Add `GEMINI_API_KEY=...` to `local.properties`
3. **Run Build**: `./gradlew assembleDebug` to verify packaging
4. **Implement Screens**: Start with `ScanScreen` for the receipt capture flow
5. **Test Integration**: Verify OCR → Gemini → UI state flow end-to-end

