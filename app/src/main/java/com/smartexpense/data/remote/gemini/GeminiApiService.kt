package com.smartexpense.data.remote.gemini

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.smartexpense.domain.model.CategorySpend
import com.smartexpense.domain.model.Expense
import com.smartexpense.domain.model.InsightResult
import com.smartexpense.domain.model.ParsedExpense
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Gemini API client for receipt parsing and insight generation.
 */
@Singleton
class GeminiApiService(
    private val okHttpClient: OkHttpClient,
    @param:Named("GeminiApiKey") private val apiKey: String
) {

    private val gson = Gson()

    suspend fun parseExpense(rawText: String): Result<ParsedExpense> = runCatching {
        executeAndParse(
            prompt = EXPENSE_PROMPT_TEMPLATE.replace("{rawText}", rawText),
            parser = { json ->
                gson.fromJson(json, ParsedExpensePayload::class.java).toDomain()
            }
        )
    }

    suspend fun generateInsight(
        expenses: List<Expense>,
        budgetSummary: String
    ): Result<InsightResult> = runCatching {
        val expenseSummaryJson = gson.toJson(
            expenses.map { expense ->
                mapOf(
                    "id" to expense.id,
                    "amount" to expense.amount,
                    "merchantName" to expense.merchantName,
                    "categoryId" to expense.categoryId,
                    "date" to expense.date.toString(),
                    "notes" to expense.notes,
                    "isAiCategorized" to expense.isAiCategorized
                )
            }
        )

        executeAndParse(
            prompt = INSIGHT_PROMPT_TEMPLATE
                .replace("{expenseSummaryJson}", if (budgetSummary.isNotBlank()) budgetSummary else expenseSummaryJson),
            parser = { json ->
                gson.fromJson(json, InsightPayload::class.java).toDomain(expenses)
            }
        )
    }

    private suspend fun <T> executeAndParse(
        prompt: String,
        parser: (String) -> T
    ): T = withContext(Dispatchers.IO) {
        val requestPayload = GeminiRequest(
            contents = listOf(
                GeminiContent(parts = listOf(GeminiPart(text = prompt)))
            )
        )
        val body = gson.toJson(requestPayload).toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=$apiKey")
            .post(body)
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Gemini API error: ${response.code}")
            }

            val responseBody = response.body?.string().orEmpty()
            val geminiResponse = gson.fromJson(responseBody, GeminiResponse::class.java)
            val rawText = geminiResponse.candidates.firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull()
                ?.text
                ?.extractJsonObject()
                ?: throw IllegalStateException("Empty Gemini response")

            parser(rawText)
        }
    }

    private fun String.extractJsonObject(): String {
        val start = indexOf('{')
        val end = lastIndexOf('}')
        if (start == -1 || end == -1 || end <= start) {
            return trim()
        }
        return substring(start, end + 1)
    }

    private fun ParsedExpensePayload.toDomain(): ParsedExpense = ParsedExpense(
        merchantName = merchantName,
        amount = amount,
        date = date?.let(LocalDate::parse),
        category = category,
        confidence = confidence
    )

    private fun InsightPayload.toDomain(expenses: List<Expense>): InsightResult = InsightResult(
        month = expenses.maxOfOrNull { YearMonth.from(it.date) } ?: YearMonth.now(),
        summary = summary.orEmpty(),
        topCategories = topCategories.map { item ->
            CategorySpend(
                category = item.category,
                amount = item.amount,
                trend = item.trend
            )
        },
        savingsTips = savingsTips,
        budgetScore = budgetScore,
        generatedAt = LocalDateTime.now()
    )

    private data class ParsedExpensePayload(
        @SerializedName("merchantName") val merchantName: String?,
        @SerializedName("amount") val amount: Double?,
        @SerializedName("date") val date: String?,
        @SerializedName("category") val category: String?,
        @SerializedName("confidence") val confidence: Float?
    )

    private data class InsightPayload(
        @SerializedName("summary") val summary: String?,
        @SerializedName("topCategories") val topCategories: List<CategorySpendPayload>,
        @SerializedName("savingsTips") val savingsTips: List<String>,
        @SerializedName("budgetScore") val budgetScore: Int
    )

    private data class CategorySpendPayload(
        @SerializedName("category") val category: String,
        @SerializedName("amount") val amount: Double,
        @SerializedName("trend") val trend: String
    )

    private companion object {
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

        private const val EXPENSE_PROMPT_TEMPLATE = """
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
        """

        private const val INSIGHT_PROMPT_TEMPLATE = """
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
        """
    }
}


