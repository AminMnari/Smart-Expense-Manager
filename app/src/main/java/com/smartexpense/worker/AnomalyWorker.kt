package com.smartexpense.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.smartexpense.data.local.dao.AnomalyEventDao
import com.smartexpense.data.mapper.toEntity
import com.smartexpense.domain.usecase.DetectAnomaliesUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.IOException

/**
 * Periodic worker that detects anomalies and notifies the user.
 */
@HiltWorker
class AnomalyWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val detectAnomaliesUseCase: DetectAnomaliesUseCase,
    private val anomalyEventDao: AnomalyEventDao,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val events = detectAnomaliesUseCase()
            events.forEach { event ->
                anomalyEventDao.insert(event.toEntity())
                notificationHelper.sendAnomalyNotification(event)
            }
            Result.success()
        } catch (ioException: IOException) {
            Result.retry()
        } catch (_: Exception) {
            Result.failure()
        }
    }
}

