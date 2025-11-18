package com.instructure.pandautils.di

import com.instructure.canvasapi2.apis.AccountNotificationAPI
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.pandautils.data.repository.accountnotification.AccountNotificationRepository
import com.instructure.pandautils.data.repository.accountnotification.AccountNotificationRepositoryImpl
import com.instructure.pandautils.data.repository.course.CourseRepository
import com.instructure.pandautils.data.repository.course.CourseRepositoryImpl
import com.instructure.pandautils.data.repository.enrollment.EnrollmentRepository
import com.instructure.pandautils.data.repository.enrollment.EnrollmentRepositoryImpl
import com.instructure.pandautils.data.repository.user.UserRepository
import com.instructure.pandautils.data.repository.user.UserRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {

    @Provides
    @Singleton
    fun provideEnrollmentRepository(
        enrollmentApi: EnrollmentAPI.EnrollmentInterface
    ): EnrollmentRepository {
        return EnrollmentRepositoryImpl(enrollmentApi)
    }

    @Provides
    @Singleton
    fun provideCourseRepository(
        courseApi: CourseAPI.CoursesInterface
    ): CourseRepository {
        return CourseRepositoryImpl(courseApi)
    }

    @Provides
    @Singleton
    fun provideAccountNotificationRepository(
        accountNotificationApi: AccountNotificationAPI.AccountNotificationInterface
    ): AccountNotificationRepository {
        return AccountNotificationRepositoryImpl(accountNotificationApi)
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        userApi: UserAPI.UsersInterface
    ): UserRepository {
        return UserRepositoryImpl(userApi)
    }
}