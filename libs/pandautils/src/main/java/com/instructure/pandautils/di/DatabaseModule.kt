package com.instructure.pandautils.di

import android.content.Context
import androidx.room.Room
import com.instructure.pandautils.room.appdatabase.AppDatabase
import com.instructure.pandautils.room.appdatabase.daos.AttachmentDao
import com.instructure.pandautils.room.appdatabase.daos.AuthorDao
import com.instructure.pandautils.room.appdatabase.daos.DashboardFileUploadDao
import com.instructure.pandautils.room.appdatabase.daos.EnvironmentFeatureFlagsDao
import com.instructure.pandautils.room.appdatabase.daos.FileDownloadProgressDao
import com.instructure.pandautils.room.appdatabase.daos.FileUploadInputDao
import com.instructure.pandautils.room.appdatabase.daos.MediaCommentDao
import com.instructure.pandautils.room.appdatabase.daos.PendingSubmissionCommentDao
import com.instructure.pandautils.room.appdatabase.daos.ReminderDao
import com.instructure.pandautils.room.appdatabase.daos.SubmissionCommentDao
import com.instructure.pandautils.room.appdatabase.daos.ToDoFilterDao
import com.instructure.pandautils.room.assignment.list.daos.AssignmentListSelectedFiltersEntityDao
import com.instructure.pandautils.room.calendar.CalendarFilterDatabase
import com.instructure.pandautils.room.calendar.daos.CalendarFilterDao
import com.instructure.pandautils.room.studentdb.StudentDb
import com.instructure.pandautils.room.studentdb.entities.daos.CreateFileSubmissionDao
import com.instructure.pandautils.room.studentdb.entities.daos.CreatePendingSubmissionCommentDao
import com.instructure.pandautils.room.studentdb.entities.daos.CreateSubmissionCommentFileDao
import com.instructure.pandautils.room.studentdb.entities.daos.CreateSubmissionDao
import com.instructure.pandautils.room.studentdb.studentDbMigrations
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
    fun provideAttachmentDao(appDatabase: AppDatabase): AttachmentDao {
        return appDatabase.attachmentDao()
    }

    @Provides
    @Singleton
    fun provideAuthorDao(appDatabase: AppDatabase): AuthorDao {
        return appDatabase.authorDao()
    }

    @Provides
    @Singleton
    fun provideFileUploadInputDao(appDatabase: AppDatabase): FileUploadInputDao {
        return appDatabase.fileUploadInputDao()
    }

    @Provides
    @Singleton
    fun provideMediaCommentDao(appDatabase: AppDatabase): MediaCommentDao {
        return appDatabase.mediaCommentDao()
    }

    @Provides
    @Singleton
    fun provideSubmissionCommentDao(appDatabase: AppDatabase): SubmissionCommentDao {
        return appDatabase.submissionCommentDao()
    }

    @Provides
    @Singleton
    fun providePendingSubmissionCommentDao(appDatabase: AppDatabase): PendingSubmissionCommentDao {
        return appDatabase.pendingSubmissionCommentDao()
    }

    @Provides
    @Singleton
    fun provideDashboardFileUploadDao(appDatabase: AppDatabase): DashboardFileUploadDao {
        return appDatabase.dashboardFileUploadDao()
    }

    @Provides
    @Singleton
    fun provideEnvironmentFeatureFlagsDao(appDatabase: AppDatabase): EnvironmentFeatureFlagsDao {
        return appDatabase.environmentFeatureFlagsDao()
    }

    @Provides
    @Singleton
    fun provideReminderDao(appDatabase: AppDatabase): ReminderDao {
        return appDatabase.reminderDao()
    }

    @Provides
    @Singleton
    fun provideFileDownloadProgressDao(appDatabase: AppDatabase): FileDownloadProgressDao {
        return appDatabase.fileDownloadProgressDao()
    }

    @Provides
    @Singleton
    fun provideToDoFilterDao(appDatabase: AppDatabase): ToDoFilterDao {
        return appDatabase.toDoFilterDao()
    }

    @Provides
    @Singleton
    fun provideCalendarFilterDao(calendarFilterDatabase: CalendarFilterDatabase): CalendarFilterDao {
        return calendarFilterDatabase.calendarFilterDao()
    }

    @Provides
    @Singleton
    fun provideAssignmentListSelectedFiltersEntityDao(appDatabase: AppDatabase): AssignmentListSelectedFiltersEntityDao {
        return appDatabase.assignmentListSelectedFiltersEntityDao()
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