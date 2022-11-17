package com.instructure.pandautils.di

import com.instructure.pandautils.room.AppDatabase
import com.instructure.pandautils.room.daos.*
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
}