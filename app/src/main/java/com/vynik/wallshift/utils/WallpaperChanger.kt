package com.vynik.wallshift.utils

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.Log
import com.vynik.wallshift.data.model.WallpaperTarget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object WallpaperChanger {

    private const val TAG = "WallpaperChanger"

    /**
     * Applies the bitmap at [uri] to the specified [target] screens.
     * Returns true on success, false on failure.
     */
    suspend fun apply(
        context: Context,
        uri: Uri,
        target: WallpaperTarget
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val wallpaperManager = WallpaperManager.getInstance(context)
            val bitmap = decodeBitmap(context, uri) ?: return@withContext false

            when (target) {
                WallpaperTarget.HOME -> {
                    wallpaperManager.setBitmap(
                        bitmap,
                        null,
                        true,
                        WallpaperManager.FLAG_SYSTEM
                    )
                }
                WallpaperTarget.LOCK -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        wallpaperManager.setBitmap(
                            bitmap,
                            null,
                            true,
                            WallpaperManager.FLAG_LOCK
                        )
                    } else {
                        // Pre-N: only system wallpaper exists
                        wallpaperManager.setBitmap(bitmap)
                    }
                }
                WallpaperTarget.BOTH -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        wallpaperManager.setBitmap(
                            bitmap,
                            null,
                            true,
                            WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
                        )
                    } else {
                        wallpaperManager.setBitmap(bitmap)
                    }
                }
            }
            bitmap.recycle()
            Log.d(TAG, "Wallpaper applied successfully → $target")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to apply wallpaper", e)
            false
        }
    }

    private fun decodeBitmap(context: Context, uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.isMutableRequired = true
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                }
            } else {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode bitmap from $uri", e)
            null
        }
    }
}
