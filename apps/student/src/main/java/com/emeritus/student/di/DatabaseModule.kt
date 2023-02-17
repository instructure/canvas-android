package com.emeritus.student.di

import android.content.Context
import androidx.room.Room
import com.instructure.pandautils.room.AppDatabase
import com.emeritus.student.db.Db
import com.instructure.student.db.StudentDb
import com.emeritus.student.db.getInstance
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
        return Room.databaseBuilder(context, AppDatabase::class.java, "db-canvas-student").build()
    }

    @Provides
    @Singleton
    fun provideSqlDelightDatabase(@ApplicationContext context: Context): StudentDb {
        return Db.getInstance(context)
    }
}