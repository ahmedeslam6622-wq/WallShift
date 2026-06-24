package com.vynik.wallshift.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.vynik.wallshift.R
import com.vynik.wallshift.data.repository.SettingsRepository
import com.vynik.wallshift.data.repository.WallpaperRepository
import com.vynik.wallshift.utils.WallpaperChanger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Foreground service for applying a wallpaper immediately (e.g., "Apply Now" button).
 * Starts, applies, then stops itself.
 */
@AndroidEntryPoint
class WallpaperChangeService : Service() {

    @Inject lateinit var wallpaperRepository: WallpaperRepository
    @Inject lateinit var settingsRepository: SettingsRepository

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, buildNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val uriString = intent?.getStringExtra(EXTRA_URI)
        val targetName = intent?.getStringExtra(EXTRA_TARGET)

        scope.launch {
            if (uriString != null && targetName != null) {
                val uri = Uri.parse(uriString)
                val target = com.vynik.wallshift.data.model.WallpaperTarget.valueOf(targetName)
                WallpaperChanger.apply(this@WallpaperChangeService, uri, target)
            } else {
                // Apply next in rotation
                val settings = settingsRepository.getSettingsSnapshot()
                val images = wallpaperRepository.getActiveImagesSync()
                if (images.isNotEmpty()) {
                    val nextIndex = (settings.currentIndex + 1) % images.size
                    val image = images[nextIndex]
                    WallpaperChanger.apply(
                        this@WallpaperChangeService,
                        Uri.parse(image.uri),
                        settings.target
                    )
                    settingsRepository.setCurrentIndex(nextIndex)
                    settingsRepository.setLastChangedAt(System.currentTimeMillis())
                }
            }
            stopSelf(startId)
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    private fun buildNotification() = run {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID, "Applying Wallpaper", NotificationManager.IMPORTANCE_LOW
        )
        manager.createNotificationChannel(channel)

        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("WallShift")
            .setContentText("Applying wallpaper…")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    companion object {
        const val CHANNEL_ID = "wallshift_apply"
        const val NOTIFICATION_ID = 1002
        const val EXTRA_URI = "extra_uri"
        const val EXTRA_TARGET = "extra_target"
    }
}
