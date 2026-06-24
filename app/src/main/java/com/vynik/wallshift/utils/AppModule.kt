package com.vynik.wallshift.utils

import android.content.Context
import androidx.room.Room
import com.vynik.wallshift.data.repository.WallShiftDatabase
import com.vynik.wallshift.data.repository.WallpaperImageDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): WallShiftDatabase =
        Room.databaseBuilder(
            context,
            WallShiftDatabase::class.java,
            "wallshift.db"
        ).build()

    @Provides
    fun provideWallpaperImageDao(db: WallShiftDatabase): WallpaperImageDao =
        db.wallpaperImageDao()
}
