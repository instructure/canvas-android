package com.instructure.teacher.di

import android.content.Context
import androidx.room.Room
import com.instructure.pandautils.room.AppDatabase
import com.instructure.pandautils.room.MIGRATION_1_2
import com.instructure.pandautils.room.MIGRATION_2_3
import com.instructure.pandautils.room.MIGRATION_3_4
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
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
            .build()
    }
}