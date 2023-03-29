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
    fun provideCourseDao(appDatabase: AppDatabase): CourseDao {
        return appDatabase.courseDao()
    }

    @Provides
    @Singleton
    fun provideEnrollmentDao(appDatabase: AppDatabase): EnrollmentDao {
        return appDatabase.enrollmentDao()
    }

    @Provides
    @Singleton
    fun provideGradesDao(appDatabase: AppDatabase): GradesDao {
        return appDatabase.gradesDao()
    }

    @Provides
    @Singleton
    fun provideGradingPeriodDao(appDatabase: AppDatabase): GradingPeriodDao {
        return appDatabase.gradingPeriodDao()
    }

    @Provides
    @Singleton
    fun provideSectionDao(appDatabase: AppDatabase): SectionDao {
        return appDatabase.sectionDao()
    }

    @Provides
    @Singleton
    fun provideTermDao(appDatabase: AppDatabase): TermDao {
        return appDatabase.termDao()
    }

    @Provides
    @Singleton
    fun provideUserCalendarDao(appDatabase: AppDatabase): UserCalendarDao {
        return appDatabase.userCalendarDao()
    }

    @Provides
    @Singleton
    fun provideUserDao(appDatabase: AppDatabase): UserDao {
        return appDatabase.userDao()
    }

    @Provides
    @Singleton
    fun provideCourseGradingPeriodDao(appDatabase: AppDatabase): CourseGradingPeriodDao {
        return appDatabase.courseGradingPeriodDao()
    }
}