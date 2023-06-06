package com.instructure.student.di

import com.instructure.canvasapi2.apis.CalendarEventAPI
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.mobius.syllabus.SyllabusRepository
import com.instructure.student.mobius.syllabus.datasource.SyllabusLocalDataSource
import com.instructure.student.mobius.syllabus.datasource.SyllabusNetworkDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@Module
@InstallIn(FragmentComponent::class)
class SyllabusModule {

    @Provides
    fun provideNetworkDataSource(courseApi: CourseAPI.CoursesInterface, calendarEventApi: CalendarEventAPI.CalendarEventInterface): SyllabusNetworkDataSource {
        return SyllabusNetworkDataSource(courseApi, calendarEventApi)
    }

    @Provides
    fun provideLocalDataSource(): SyllabusLocalDataSource {
        return SyllabusLocalDataSource()
    }

    @Provides
    fun provideSyllabusRepository(
        localDataSource: SyllabusLocalDataSource,
        networkDataSource: SyllabusNetworkDataSource,
        networkStateProvider: NetworkStateProvider
    ): SyllabusRepository {
        return SyllabusRepository(localDataSource, networkDataSource, networkStateProvider)
    }
}