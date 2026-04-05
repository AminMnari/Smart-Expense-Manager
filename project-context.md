# Smart Expense Manager — Copilot Project Context

## Project Info
- App name: Smart Expense Manager for Personal Finance Optimization
- Package: com.smartexpense
- Language: Kotlin
- UI: Jetpack Compose + Material Design 3
- Architecture: MVVM + Repository + Clean Architecture
- Min SDK: 26 | Target SDK: 35
- DI: Hilt
- Build: Gradle (Kotlin DSL)

## Package Structure
```
com.smartexpense/
  ├── di/
  │   └── DatabaseModule.kt
  ├── data/
  │   ├── local/
  │   │   ├── db/
  │   │   │   ├── AppDatabase.kt
  │   │   │   └── Converters.kt
  │   │   ├── dao/
  │   │   │   ├── ExpenseDao.kt
  │   │   │   ├── CategoryDao.kt
  │   │   │   ├── BudgetDao.kt
  │   │   │   └── AnomalyEventDao.kt
  │   │   └── entity/
  │   │       ├── ExpenseEntity.kt
  │   │       ├── CategoryEntity.kt
  │   │       ├── BudgetEntity.kt
  │   │       └── AnomalyEventEntity.kt
  │   ├── remote/
  │   │   ├── gemini/
  │   │   │   ├── GeminiApiService.kt
  │   │   │   ├── GeminiRequest.kt
  │   │   │   └── GeminiResponse.kt
  │   │   └── ocr/
  │   │       └── MlKitOcrService.kt
  │   └── repository/
  │       ├── ExpenseRepositoryImpl.kt
  │       ├── BudgetRepositoryImpl.kt
  │       └── InsightRepositoryImpl.kt
  ├── domain/
  │   ├── model/
  │   │   ├── Expense.kt
  │   │   ├── Category.kt
  │   │   ├── Budget.kt
  │   │   ├── AnomalyEvent.kt
  │   │   ├── WeeklyTotal.kt
  │   │   ├── InsightResult.kt
  │   │   ├── CategorySpend.kt
  │   │   └── ParsedExpense.kt
  │   ├── repository/
  │   │   ├── ExpenseRepository.kt
  │   │   ├── BudgetRepository.kt
  │   │   └── InsightRepository.kt
  │   └── usecase/
  │       ├── ScanReceiptUseCase.kt
  │       ├── SaveExpenseUseCase.kt
  │       ├── GetExpensesUseCase.kt
  │       ├── DetectAnomaliesUseCase.kt
  │       └── GenerateInsightUseCase.kt
  ├── presentation/
  │   ├── scan/
  │   │   ├── ScanScreen.kt
  │   │   └── ScanViewModel.kt
  │   ├── dashboard/
  │   │   ├── DashboardScreen.kt
  │   │   └── DashboardViewModel.kt
  │   ├── expenses/
  │   │   ├── ExpenseListScreen.kt
  │   │   └── ExpenseDetailScreen.kt
  │   ├── budget/
  │   │   ├── BudgetScreen.kt
  │   │   └── BudgetViewModel.kt
  │   ├── alerts/
  │   │   ├── AlertsScreen.kt
  │   │   └── AlertViewModel.kt
  │   ├── insights/
  │   │   ├── InsightScreen.kt
  │   │   └── InsightViewModel.kt
  │   └── navigation/
  │       ├── NavGraph.kt
  │       └── Screen.kt
  └── worker/
      ├── AnomalyWorker.kt
      └── WorkManagerHelper.kt
```

## Domain Models

```kotlin
// domain/model/Expense.kt
data class Expense(
    val id: Long = 0,
    val amount: Double,
    val merchantName: String,
    val categoryId: Long,
    val date: LocalDate,
    val notes: String = "",
    val receiptImagePath: String? = null,
    val isAiCategorized: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

// domain/model/Category.kt
data class Category(
    val id: Long = 0,
    val name: String,
    val iconRes: Int,
    val colorHex: String
)

// domain/model/Budget.kt
data class Budget(
    val id: Long = 0,
    val categoryId: Long,
    val monthlyLimit: Double,
    val alertThresholdPercent: Int = 150,
    val month: YearMonth
)

// domain/model/AnomalyEvent.kt
data class AnomalyEvent(
    val id: Long = 0,
    val categoryId: Long,
    val detectedAt: LocalDateTime,
    val currentWeekAmount: Double,
    val averageWeekAmount: Double,
    val percentIncrease: Float,
    val isRead: Boolean = false
)

// domain/model/WeeklyTotal.kt
data class WeeklyTotal(
    val categoryId: Long,
    val week: String,
    val total: Double
)

// domain/model/InsightResult.kt
data class InsightResult(
    val month: YearMonth,
    val summary: String,
    val topCategories: List<CategorySpend>,
    val savingsTips: List<String>,
    val budgetScore: Int,
    val generatedAt: LocalDateTime
)

// domain/model/CategorySpend.kt
data class CategorySpend(
    val category: String,
    val amount: Double,
    val trend: String
)

// domain/model/ParsedExpense.kt
data class ParsedExpense(
    val merchantName: String?,
    val amount: Double?,
    val date: LocalDate?,
    val category: String?,
    val confidence: Float?
)
```

## Database Tables

### expenses
| Column             | Type    | Constraints                       |
|--------------------|---------|-----------------------------------|
| id                 | INTEGER | PRIMARY KEY AUTOINCREMENT         |
| amount             | REAL    | NOT NULL                          |
| merchant_name      | TEXT    | NOT NULL                          |
| category_id        | INTEGER | NOT NULL, FK → categories(id)     |
| date               | TEXT    | NOT NULL (yyyy-MM-dd)             |
| notes              | TEXT    | DEFAULT ''                        |
| receipt_image_path | TEXT    | NULLABLE                          |
| is_ai_categorized  | INTEGER | NOT NULL DEFAULT 0                |
| created_at         | TEXT    | NOT NULL (yyyy-MM-dd'T'HH:mm:ss) |

### categories
| Column    | Type    | Constraints               |
|-----------|---------|---------------------------|
| id        | INTEGER | PRIMARY KEY AUTOINCREMENT |
| name      | TEXT    | NOT NULL UNIQUE           |
| icon_res  | INTEGER | NOT NULL                  |
| color_hex | TEXT    | NOT NULL                  |

### budgets
| Column                  | Type    | Constraints               |
|-------------------------|---------|---------------------------|
| id                      | INTEGER | PRIMARY KEY AUTOINCREMENT |
| category_id             | INTEGER | NOT NULL, FK → categories |
| monthly_limit           | REAL    | NOT NULL                  |
| alert_threshold_percent | INTEGER | NOT NULL DEFAULT 150      |
| month                   | TEXT    | NOT NULL (yyyy-MM)        |

### anomaly_events
| Column              | Type    | Constraints               |
|---------------------|---------|---------------------------|
| id                  | INTEGER | PRIMARY KEY AUTOINCREMENT |
| category_id         | INTEGER | NOT NULL, FK → categories |
| detected_at         | TEXT    | NOT NULL                  |
| current_week_amount | REAL    | NOT NULL                  |
| average_week_amount | REAL    | NOT NULL                  |
| percent_increase    | REAL    | NOT NULL                  |
| is_read             | INTEGER | NOT NULL DEFAULT 0        |

## Repository Interfaces

```kotlin
// domain/repository/ExpenseRepository.kt
interface ExpenseRepository {
    fun getAll(): Flow<List<Expense>>
    fun getByPeriod(start: LocalDate, end: LocalDate): Flow<List<Expense>>
    fun getByCategory(categoryId: Long): Flow<List<Expense>>
    suspend fun insert(expense: Expense): Long
    suspend fun update(expense: Expense)
    suspend fun delete(expense: Expense)
    suspend fun getWeeklyTotals(categoryId: Long, weeks: Int): List<WeeklyTotal>
}

// domain/repository/BudgetRepository.kt
interface BudgetRepository {
    fun getBudgets(month: YearMonth): Flow<List<Budget>>
    suspend fun upsertBudget(budget: Budget)
    suspend fun deleteBudget(categoryId: Long)
}

// domain/repository/InsightRepository.kt
interface InsightRepository {
    suspend fun generateInsight(expenses: List<Expense>): InsightResult
    fun getCachedInsight(month: YearMonth): Flow<InsightResult?>
    suspend fun saveInsight(insight: InsightResult)
}
```

## UI State Pattern

```kotlin
// Always use sealed classes for UI state — example for ScanScreen
sealed class ScanUiState {
    object Idle : ScanUiState()
    object CameraOpen : ScanUiState()
    object Processing : ScanUiState()
    data class Review(val parsed: ParsedExpense) : ScanUiState()
    data class Error(val message: String) : ScanUiState()
    object Saved : ScanUiState()
}

// Apply the same pattern to every screen:
// DashboardUiState, BudgetUiState, AlertUiState, InsightUiState
```

## Navigation Routes

```kotlin
// presentation/navigation/Screen.kt
sealed class Screen(val route: String) {
    object Dashboard    : Screen("dashboard")
    object Scan         : Screen("scan")
    object ExpenseDetail: Screen("expense/{id}") {
        fun createRoute(id: Long) = "expense/$id"
    }
    object AddExpense   : Screen("add_expense")
    object Budget       : Screen("budget")
    object Alerts       : Screen("alerts")
    object Insights     : Screen("insights")
    object Settings     : Screen("settings")
}
```

## AI Integration

### ML Kit OCR
- Library: ML Kit Text Recognition v2
- Processing: fully on-device, no internet required
- Input: Bitmap captured from camera or gallery
- Output: raw String of extracted text

### Gemini API
- Model: gemini-pro
- Purpose 1: parse receipt OCR text → structured expense data
- Purpose 2: generate monthly financial insight report
- API key: stored in local.properties as GEMINI_API_KEY
- Accessed in code via BuildConfig.GEMINI_API_KEY
- Always returns strict JSON — wrap all parsing in try/catch

### Gemini Expense Parsing Prompt Template
```
You are a financial data extraction assistant.
Given the following receipt text, extract the expense details.
Return ONLY a valid JSON object with this exact schema:
{
  "merchantName": "string",
  "amount": number,
  "date": "yyyy-MM-dd",
  "category": one of [Food, Transport, Shopping, Health,
              Utilities, Entertainment, Education, Other],
  "confidence": number between 0 and 1
}
If a field cannot be determined, use null.
Receipt text:
{rawText}
```

### Gemini Monthly Insight Prompt Template
```
You are a personal finance coach.
Analyze the following monthly spending summary and provide insights.
Return ONLY a valid JSON object with this exact schema:
{
  "summary": "2-3 sentence overview",
  "topCategories": [
    { "category": string, "amount": number, "trend": "up|down|stable" }
  ],
  "savingsTips": ["tip1", "tip2", "tip3"],
  "budgetScore": number between 0 and 100
}
Monthly spending data:
{expenseSummaryJson}
```

## Sequence Flows (for reference)

### Receipt Scan Flow (UC-01)
1. User taps Scan → ScanScreen calls ScanViewModel.onScanRequested()
2. ScanViewModel launches camera intent
3. Image captured → ScanViewModel.onImageCaptured(uri)
4. ScanViewModel calls ScanReceiptUseCase.execute(uri)
5. ScanReceiptUseCase calls MlKitOcrService.recognizeText(bitmap) → rawText
6. ScanReceiptUseCase calls GeminiApiService.parseExpense(rawText) → ParsedExpense
7. ScanViewModel emits ScanUiState.Review(parsedExpense)
8. User reviews/edits → taps Confirm
9. ScanViewModel calls SaveExpenseUseCase.execute(expense)
10. SaveExpenseUseCase calls ExpenseRepository.insert(expense)
11. ScanViewModel emits ScanUiState.Saved → navigate to Dashboard

### Anomaly Detection Flow (UC-06)
1. WorkManager triggers AnomalyWorker.doWork() daily
2. AnomalyWorker calls DetectAnomaliesUseCase.execute()
3. Use case fetches 5 weeks of weekly totals per category
4. Use case compares current week vs 4-week average
5. If spike detected → save AnomalyEvent + send push notification

### AI Insight Flow (UC-07)
1. User taps Generate Insight → InsightViewModel.onGenerateInsightRequested()
2. Check InsightRepository.getCachedInsight(month) first
3. If no cache → fetch expenses → call GeminiApiService.generateInsight()
4. Save result via InsightRepository.saveInsight()
5. Emit InsightUiState with result

## Error Handling Rules
- All network and AI calls must return Result<T>
- OCR failure → show error + auto-navigate to manual entry form
- Gemini timeout → retry up to 3 times with exponential backoff
- Gemini 429 (rate limit) → show "Try again later" message
- No internet → Room features work normally, AI features show offline banner
- Room insert failure → show Snackbar, preserve the user's entered data in the form

## Strict Rules — Copilot Must Always Follow
- Use Flow and coroutines everywhere. No LiveData. No RxJava.
- Suspend functions only in the data layer (DAOs, repository implementations).
- ViewModels only collect Flows and call use cases — never call repositories directly.
- Use cases contain all business logic — keep ViewModels thin.
- Hilt for all dependency injection — no manual class instantiation.
- No hardcoded strings — always use string resources (res/values/strings.xml).
- All dates use LocalDate / LocalDateTime / YearMonth with Room TypeConverters.
- Never rename or invent new class names — use exactly the names defined in this file.
- Every class and interface must have a KDoc comment.
  - Generate every file completely — never summarize or use placeholder comments like "// rest of code here".