package com.smartexpense

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.smartexpense.worker.WorkManagerHelper
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application entry point for Hilt dependency injection.
 */
@HiltAndroidApp
class SmartExpenseApplication : Application(), Configuration.Provider {

	@Inject
	lateinit var workerFactory: HiltWorkerFactory

	override fun onCreate() {
		super.onCreate()
		WorkManagerHelper.scheduleAnomalyDetection(this)
	}

	override val workManagerConfiguration: Configuration
		get() = Configuration.Builder()
			.setWorkerFactory(workerFactory)
			.build()
}

