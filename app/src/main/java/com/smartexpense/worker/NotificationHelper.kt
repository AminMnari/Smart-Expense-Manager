package com.smartexpense.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.smartexpense.R
import com.smartexpense.domain.model.AnomalyEvent
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper for creating and dispatching anomaly alert notifications.
 */
@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    init {
        createChannelIfNeeded()
    }

    fun sendAnomalyNotification(event: AnomalyEvent) {
        if (
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val categoryName = categoryName(event.categoryId)
        val title = context.getString(R.string.notification_title, categoryName)
        val message = context.getString(
            R.string.notification_text,
            String.format(Locale.getDefault(), "%.1f", event.percentIncrease)
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context)
            .notify(event.id.toInt().takeIf { it != 0 } ?: event.detectedAt.hashCode(), notification)
    }

    private fun createChannelIfNeeded() {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )
        manager.createNotificationChannel(channel)
    }

    private fun categoryName(categoryId: Long): String = when (categoryId) {
        1L -> context.getString(R.string.category_food)
        2L -> context.getString(R.string.category_transport)
        3L -> context.getString(R.string.category_shopping)
        4L -> context.getString(R.string.category_health)
        5L -> context.getString(R.string.category_utilities)
        6L -> context.getString(R.string.category_entertainment)
        7L -> context.getString(R.string.category_education)
        else -> context.getString(R.string.category_other)
    }

    private companion object {
        const val CHANNEL_ID = "anomaly_alerts"
        const val CHANNEL_NAME = "Spending Alerts"
    }
}

