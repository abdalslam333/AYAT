package com.wird.ayat.widget

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class WidgetUpdateWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            WidgetUpdater.updateWidget(applicationContext)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
