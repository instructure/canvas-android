package com.instructure.pandautils.di

import com.instructure.pandautils.room.appdatabase.AppDatabase
import com.instructure.pandautils.room.appdatabase.daos.AttachmentDao
import com.instructure.pandautils.room.appdatabase.daos.AuthorDao
import com.instructure.pandautils.room.appdatabase.daos.DashboardFileUploadDao
import com.instructure.pandautils.room.appdatabase.daos.EnvironmentFeatureFlagsDao
import com.instructure.pandautils.room.appdatabase.daos.FileUploadInputDao
import com.instructure.pandautils.room.appdatabase.daos.MediaCommentDao
import com.instructure.pandautils.room.appdatabase.daos.PendingSubmissionCommentDao
import com.instructure.pandautils.room.appdatabase.daos.ReminderDao
import com.instructure.pandautils.room.appdatabase.daos.SubmissionCommentDao
import com.instructure.pandautils.room.assignment.list.daos.AssignmentListSelectedFiltersEntityDao
import com.instructure.pandautils.room.calendar.CalendarFilterDatabase
import com.instructure.pandautils.room.calendar.daos.CalendarFilterDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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
    fun provideCalendarFilterDao(calendarFilterDatabase: CalendarFilterDatabase): CalendarFilterDao {
        return calendarFilterDatabase.calendarFilterDao()
    }

    @Provides
    @Singleton
    fun provideAssignmentListSelectedFiltersEntityDao(appDatabase: AppDatabase): AssignmentListSelectedFiltersEntityDao {
        return appDatabase.assignmentListSelectedFiltersEntityDao()
    }
}