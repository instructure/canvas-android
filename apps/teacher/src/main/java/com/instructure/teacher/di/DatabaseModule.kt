package com.instructure.teacher.di

import android.content.Context
import androidx.room.Room
import com.instructure.pandautils.room.AppDatabase
import com.instructure.pandautils.room.appDatabaseMigrations
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "db-canvas-teacher")
            .addMigrations(*appDatabaseMigrations)
            .build()
    }
}