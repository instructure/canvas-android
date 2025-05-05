package com.instructure.student.di

import android.content.Context
import androidx.room.Room
import com.instructure.pandautils.room.appdatabase.AppDatabase
import com.instructure.pandautils.room.appdatabase.appDatabaseMigrations
import com.instructure.pandautils.room.calendar.CalendarFilterDatabase
import com.instructure.pandautils.room.calendar.calendarDatabaseMigrations
import com.instructure.student.room.StudentDb
import com.instructure.student.room.entities.daos.CreateFileSubmissionDao
import com.instructure.student.room.entities.daos.CreatePendingSubmissionCommentDao
import com.instructure.student.room.entities.daos.CreateSubmissionCommentFileDao
import com.instructure.student.room.entities.daos.CreateSubmissionDao
import com.instructure.student.room.studentDbMigrations
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
        return Room.databaseBuilder(context, AppDatabase::class.java, "db-canvas-student")
            .addMigrations(*appDatabaseMigrations)
            .build()
    }

    @Provides
    @Singleton
    fun provideCalendarDatabase(@ApplicationContext context: Context): CalendarFilterDatabase {
        return Room.databaseBuilder(
            context,
            CalendarFilterDatabase::class.java,
            "canvas_student_flutter.db"
        ) // We need to have the same db name as in the Flutter calendar
            .addMigrations(*calendarDatabaseMigrations)
            .build()
    }

    @Provides
    @Singleton
    fun provideStudentDb(@ApplicationContext context: Context): StudentDb {
        return Room.databaseBuilder(context, StudentDb::class.java, "student.db")
            .addMigrations(*studentDbMigrations)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideCreateSubmissionDao(studentDb: StudentDb): CreateSubmissionDao {
        return studentDb.submissionDao()
    }

    @Provides
    @Singleton
    fun provideCreateFileSubmissionDao(studentDb: StudentDb): CreateFileSubmissionDao {
        return studentDb.fileSubmissionDao()
    }

    @Provides
    @Singleton
    fun provideCreatePendingSubmissionCommentDao(studentDb: StudentDb): CreatePendingSubmissionCommentDao {
        return studentDb.pendingSubmissionCommentDao()
    }

    @Provides
    @Singleton
    fun provideCreateSubmissionCommentFileDao(studentDb: StudentDb): CreateSubmissionCommentFileDao {
        return studentDb.submissionCommentFileDao()
    }
}