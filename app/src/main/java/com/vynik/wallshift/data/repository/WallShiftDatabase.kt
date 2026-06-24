package com.vynik.wallshift.data.repository

import androidx.room.*
import com.vynik.wallshift.data.model.WallpaperImage
import kotlinx.coroutines.flow.Flow

@Dao
interface WallpaperImageDao {

    @Query("SELECT * FROM wallpaper_images WHERE isActive = 1 ORDER BY orderIndex ASC, addedAt ASC")
    fun getActiveImages(): Flow<List<WallpaperImage>>

    @Query("SELECT * FROM wallpaper_images ORDER BY orderIndex ASC, addedAt ASC")
    fun getAllImages(): Flow<List<WallpaperImage>>

    @Query("SELECT COUNT(*) FROM wallpaper_images WHERE isActive = 1")
    suspend fun getActiveCount(): Int

    @Query("SELECT * FROM wallpaper_images WHERE isActive = 1 ORDER BY orderIndex ASC, addedAt ASC")
    suspend fun getActiveImagesSync(): List<WallpaperImage>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(image: WallpaperImage): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(images: List<WallpaperImage>)

    @Update
    suspend fun update(image: WallpaperImage)

    @Delete
    suspend fun delete(image: WallpaperImage)

    @Query("DELETE FROM wallpaper_images WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE wallpaper_images SET isActive = :active WHERE id = :id")
    suspend fun setActive(id: Long, active: Boolean)

    @Query("UPDATE wallpaper_images SET orderIndex = :orderIndex WHERE id = :id")
    suspend fun updateOrder(id: Long, orderIndex: Int)
}

@Database(
    entities = [WallpaperImage::class],
    version = 1,
    exportSchema = false
)
abstract class WallShiftDatabase : RoomDatabase() {
    abstract fun wallpaperImageDao(): WallpaperImageDao
}
