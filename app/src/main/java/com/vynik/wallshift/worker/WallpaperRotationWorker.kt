package com.vynik.wallshift.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.vynik.wallshift.R
import com.vynik.wallshift.data.repository.SettingsRepository
import com.vynik.wallshift.data.repository.WallpaperRepository
import com.vynik.wallshift.ui.MainActivity
import com.vynik.wallshift.utils.WallpaperChanger
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit
import kotlin.random.Random

@HiltWorker
class WallpaperRotationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val wallpaperRepository: WallpaperRepository,
    private val settingsRepository: SettingsRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val settings = settingsRepository.getSettingsSnapshot()

        if (!settings.isEnabled) return Result.success()

        val images = wallpaperRepository.getActiveImagesSync()
        if (images.isEmpty()) return Result.failure()

        // Pick next image — shuffle or sequential
        val nextIndex = if (settings.shuffleMode) {
            val candidates = images.indices.filter { it != settings.currentIndex }
            if (candidates.isEmpty()) settings.currentIndex
            else candidates[Random.nextInt(candidates.size)]
        } else {
            (settings.currentIndex + 1) % images.size
        }

        val image = images[nextIndex]
        val uri = Uri.parse(image.uri)

        val success = WallpaperChanger.apply(context, uri, settings.target)

        return if (success) {
            settingsRepository.setCurrentIndex(nextIndex)
            settingsRepository.setLastChangedAt(System.currentTimeMillis())
            showNotification(image.displayName.ifEmpty { "Image ${nextIndex + 1}" })
            Result.success()
        } else {
            // Retry up to 3 times if this image fails (might be a temporary IO issue)
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    private fun showNotification(imageName: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create channel
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Wallpaper Changes",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Notifies when wallpaper rotates"
        }
        manager.createNotificationChannel(channel)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Wallpaper rotated")
            .setContentText("Now showing: $imageName")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val WORK_NAME = "wallpaper_rotation"
        const val CHANNEL_ID = "wallshift_rotation"
        const val NOTIFICATION_ID = 1001

        fun schedule(context: Context, intervalHours: Long) {
            val request = PeriodicWorkRequestBuilder<WallpaperRotationWorker>(
                intervalHours, TimeUnit.HOURS
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(false)
                        .build()
                )
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
