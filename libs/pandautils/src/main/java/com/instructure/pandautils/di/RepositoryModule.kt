package com.instructure.pandautils.di

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.pandautils.data.repository.course.CourseRepository
import com.instructure.pandautils.data.repository.course.CourseRepositoryImpl
import com.instructure.pandautils.data.repository.enrollment.EnrollmentRepository
import com.instructure.pandautils.data.repository.enrollment.EnrollmentRepositoryImpl
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
}