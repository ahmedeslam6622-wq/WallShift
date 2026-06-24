package com.vynik.wallshift.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.vynik.wallshift.data.repository.SettingsRepository
import com.vynik.wallshift.worker.WallpaperRotationWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            Log.d("BootReceiver", "Device booted — rescheduling wallpaper worker")
            CoroutineScope(Dispatchers.IO).launch {
                val settings = settingsRepository.getSettingsSnapshot()
                if (settings.isEnabled) {
                    WallpaperRotationWorker.schedule(context, settings.intervalHours)
                }
            }
        }
    }
}
