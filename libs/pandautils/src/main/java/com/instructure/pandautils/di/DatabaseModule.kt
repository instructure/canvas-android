package com.instructure.pandautils.di

import com.instructure.pandautils.room.appdatabase.AppDatabase
import com.instructure.pandautils.room.appdatabase.daos.*
import com.instructure.pandautils.room.common.daos.AttachmentDao
import com.instructure.pandautils.room.common.daos.AuthorDao
import com.instructure.pandautils.room.common.daos.MediaCommentDao
import com.instructure.pandautils.room.common.daos.SubmissionCommentDao
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
}