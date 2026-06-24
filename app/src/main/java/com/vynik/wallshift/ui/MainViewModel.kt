package com.vynik.wallshift.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vynik.wallshift.data.model.AppSettings
import com.vynik.wallshift.data.model.RotationInterval
import com.vynik.wallshift.data.model.WallpaperImage
import com.vynik.wallshift.data.model.WallpaperTarget
import com.vynik.wallshift.data.repository.SettingsRepository
import com.vynik.wallshift.data.repository.WallpaperRepository
import com.vynik.wallshift.service.WallpaperChangeService
import com.vynik.wallshift.worker.WallpaperRotationWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UiState(
    val settings: AppSettings = AppSettings(),
    val images: List<WallpaperImage> = emptyList(),
    val isLoading: Boolean = false,
    val snackbarMessage: String? = null
)

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val wallpaperRepository: WallpaperRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _snackbar = MutableStateFlow<String?>(null)

    val uiState: StateFlow<UiState> = combine(
        settingsRepository.settingsFlow,
        wallpaperRepository.getAllImages(),
        _snackbar
    ) { settings, images, snackbar ->
        UiState(settings = settings, images = images, snackbarMessage = snackbar)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        UiState(isLoading = true)
    )

    // ── Enable / disable ─────────────────────────────────────────────────

    fun setEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setEnabled(enabled)
            if (enabled) {
                val settings = settingsRepository.getSettingsSnapshot()
                WallpaperRotationWorker.schedule(context, settings.intervalHours)
                _snackbar.value = "Auto-rotation enabled"
            } else {
                WallpaperRotationWorker.cancel(context)
                _snackbar.value = "Auto-rotation paused"
            }
        }
    }

    // ── Settings ─────────────────────────────────────────────────────────

    fun setInterval(interval: RotationInterval) {
        viewModelScope.launch {
            settingsRepository.setInterval(interval.hours)
            val settings = settingsRepository.getSettingsSnapshot()
            if (settings.isEnabled) {
                WallpaperRotationWorker.schedule(context, interval.hours)
            }
        }
    }

    fun setTarget(target: WallpaperTarget) {
        viewModelScope.launch {
            settingsRepository.setTarget(target)
        }
    }

    fun setShuffleMode(shuffle: Boolean) {
        viewModelScope.launch {
            settingsRepository.setShuffleMode(shuffle)
        }
    }

    // ── Image management ─────────────────────────────────────────────────

    fun addImages(uris: List<Uri>) {
        viewModelScope.launch {
            // Persist URI access
            val pairs = uris.mapNotNull { uri ->
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    val cursor = context.contentResolver.query(
                        uri, null, null, null, null
                    )
                    val name = cursor?.use { c ->
                        val col = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        c.moveToFirst()
                        if (col >= 0) c.getString(col) else "Image"
                    } ?: "Image"
                    uri.toString() to name
                } catch (e: Exception) {
                    null
                }
            }
            wallpaperRepository.addImages(pairs)
            _snackbar.value = "${pairs.size} image(s) added"
        }
    }

    fun removeImage(image: WallpaperImage) {
        viewModelScope.launch {
            wallpaperRepository.removeImage(image)
            _snackbar.value = "Removed"
        }
    }

    fun toggleImageActive(image: WallpaperImage) {
        viewModelScope.launch {
            wallpaperRepository.toggleActive(image)
        }
    }

    fun reorderImages(images: List<WallpaperImage>) {
        viewModelScope.launch {
            wallpaperRepository.reorder(images)
        }
    }

    // ── Apply now ────────────────────────────────────────────────────────

    fun applyNow() {
        viewModelScope.launch {
            val intent = Intent(context, WallpaperChangeService::class.java)
            context.startForegroundService(intent)
            _snackbar.value = "Applying next wallpaper…"
        }
    }

    fun applySpecific(image: WallpaperImage) {
        viewModelScope.launch {
            val settings = settingsRepository.getSettingsSnapshot()
            val intent = Intent(context, WallpaperChangeService::class.java).apply {
                putExtra(WallpaperChangeService.EXTRA_URI, image.uri)
                putExtra(WallpaperChangeService.EXTRA_TARGET, settings.target.name)
            }
            context.startForegroundService(intent)
            _snackbar.value = "Applying \"${image.displayName}\"…"
        }
    }

    fun clearSnackbar() {
        _snackbar.value = null
    }
}
