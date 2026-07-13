package com.ansonboby.almanac.di

import android.content.Context
import androidx.room.Room
import com.ansonboby.almanac.data.local.AlmanacDatabase
import com.ansonboby.almanac.data.local.PlaceholderDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Hilt bindings for the Room layer. */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AlmanacDatabase =
        Room.databaseBuilder(
            context,
            AlmanacDatabase::class.java,
            AlmanacDatabase.NAME,
        ).build()

    @Provides
    fun providePlaceholderDao(database: AlmanacDatabase): PlaceholderDao =
        database.placeholderDao()
}
