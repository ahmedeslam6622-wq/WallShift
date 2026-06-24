package com.vynik.wallshift.data.repository

import com.vynik.wallshift.data.model.WallpaperImage
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WallpaperRepository @Inject constructor(
    private val dao: WallpaperImageDao
) {
    fun getActiveImages(): Flow<List<WallpaperImage>> = dao.getActiveImages()

    fun getAllImages(): Flow<List<WallpaperImage>> = dao.getAllImages()

    suspend fun getActiveImagesSync(): List<WallpaperImage> = dao.getActiveImagesSync()

    suspend fun getActiveCount(): Int = dao.getActiveCount()

    suspend fun addImage(uri: String, displayName: String = ""): Long {
        val count = dao.getActiveCount()
        val image = WallpaperImage(
            uri = uri,
            displayName = displayName,
            orderIndex = count
        )
        return dao.insert(image)
    }

    suspend fun addImages(uris: List<Pair<String, String>>) {
        val startIndex = dao.getActiveCount()
        val images = uris.mapIndexed { i, (uri, name) ->
            WallpaperImage(
                uri = uri,
                displayName = name,
                orderIndex = startIndex + i
            )
        }
        dao.insertAll(images)
    }

    suspend fun removeImage(image: WallpaperImage) = dao.delete(image)

    suspend fun removeById(id: Long) = dao.deleteById(id)

    suspend fun toggleActive(image: WallpaperImage) {
        dao.setActive(image.id, !image.isActive)
    }

    suspend fun reorder(images: List<WallpaperImage>) {
        images.forEachIndexed { index, image ->
            dao.updateOrder(image.id, index)
        }
    }
}
