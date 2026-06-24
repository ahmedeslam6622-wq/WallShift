package com.vynik.wallshift.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a single image in the rotation pool.
 */
@Entity(tableName = "wallpaper_images")
data class WallpaperImage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val uri: String,
    val displayName: String = "",
    val addedAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,       // can be toggled off without deleting
    val orderIndex: Int = 0             // user-controlled ordering
)

/**
 * Target screens for wallpaper application.
 */
enum class WallpaperTarget(val label: String) {
    HOME("Home Screen"),
    LOCK("Lock Screen"),
    BOTH("Both Screens")
}

/**
 * Rotation interval — backed by research on psychological "boredom threshold".
 * Studies suggest people tire of repeated visual stimuli in 3–7 days.
 * We offer fine-grained controls from 1 hour to 30 days.
 */
enum class RotationInterval(
    val label: String,
    val hours: Long
) {
    ONE_HOUR("1 Hour", 1),
    THREE_HOURS("3 Hours", 3),
    SIX_HOURS("6 Hours", 6),
    TWELVE_HOURS("12 Hours", 12),
    ONE_DAY("1 Day", 24),
    TWO_DAYS("2 Days", 48),
    THREE_DAYS("3 Days — Recommended", 72),   // sweet spot
    FIVE_DAYS("5 Days", 120),
    ONE_WEEK("1 Week", 168),
    TWO_WEEKS("2 Weeks", 336),
    THIRTY_DAYS("30 Days", 720);

    companion object {
        val DEFAULT = THREE_DAYS
    }
}

/**
 * App-wide settings stored in DataStore.
 */
data class AppSettings(
    val isEnabled: Boolean = false,
    val intervalHours: Long = RotationInterval.DEFAULT.hours,
    val target: WallpaperTarget = WallpaperTarget.BOTH,
    val currentIndex: Int = 0,
    val shuffleMode: Boolean = false,
    val lastChangedAt: Long = 0L
)
