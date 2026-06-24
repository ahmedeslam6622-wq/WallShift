package com.vynik.wallshift.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.vynik.wallshift.data.model.AppSettings
import com.vynik.wallshift.data.model.RotationInterval
import com.vynik.wallshift.data.model.WallpaperTarget
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "wallshift_prefs")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val IS_ENABLED = booleanPreferencesKey("is_enabled")
        val INTERVAL_HOURS = longPreferencesKey("interval_hours")
        val TARGET = stringPreferencesKey("target")
        val CURRENT_INDEX = intPreferencesKey("current_index")
        val SHUFFLE_MODE = booleanPreferencesKey("shuffle_mode")
        val LAST_CHANGED_AT = longPreferencesKey("last_changed_at")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { prefs ->
            AppSettings(
                isEnabled = prefs[Keys.IS_ENABLED] ?: false,
                intervalHours = prefs[Keys.INTERVAL_HOURS] ?: RotationInterval.DEFAULT.hours,
                target = WallpaperTarget.valueOf(
                    prefs[Keys.TARGET] ?: WallpaperTarget.BOTH.name
                ),
                currentIndex = prefs[Keys.CURRENT_INDEX] ?: 0,
                shuffleMode = prefs[Keys.SHUFFLE_MODE] ?: false,
                lastChangedAt = prefs[Keys.LAST_CHANGED_AT] ?: 0L
            )
        }

    suspend fun setEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.IS_ENABLED] = enabled }
    }

    suspend fun setInterval(hours: Long) {
        context.dataStore.edit { it[Keys.INTERVAL_HOURS] = hours }
    }

    suspend fun setTarget(target: WallpaperTarget) {
        context.dataStore.edit { it[Keys.TARGET] = target.name }
    }

    suspend fun setCurrentIndex(index: Int) {
        context.dataStore.edit { it[Keys.CURRENT_INDEX] = index }
    }

    suspend fun setShuffleMode(shuffle: Boolean) {
        context.dataStore.edit { it[Keys.SHUFFLE_MODE] = shuffle }
    }

    suspend fun setLastChangedAt(time: Long) {
        context.dataStore.edit { it[Keys.LAST_CHANGED_AT] = time }
    }

    suspend fun getSettingsSnapshot(): AppSettings {
        var result = AppSettings()
        context.dataStore.data.catch { emit(emptyPreferences()) }
            .map { prefs ->
                AppSettings(
                    isEnabled = prefs[Keys.IS_ENABLED] ?: false,
                    intervalHours = prefs[Keys.INTERVAL_HOURS] ?: RotationInterval.DEFAULT.hours,
                    target = WallpaperTarget.valueOf(
                        prefs[Keys.TARGET] ?: WallpaperTarget.BOTH.name
                    ),
                    currentIndex = prefs[Keys.CURRENT_INDEX] ?: 0,
                    shuffleMode = prefs[Keys.SHUFFLE_MODE] ?: false,
                    lastChangedAt = prefs[Keys.LAST_CHANGED_AT] ?: 0L
                )
            }.collect { result = it }
        return result
    }
}
