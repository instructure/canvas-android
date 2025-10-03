package com.instructure.student.di

import com.instructure.canvasapi2.apis.CalendarEventAPI
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.PlannerAPI
import com.instructure.pandautils.room.offline.daos.CourseSettingsDao
import com.instructure.pandautils.room.offline.facade.CourseFacade
import com.instructure.pandautils.room.offline.facade.ScheduleItemFacade
import com.instructure.pandautils.utils.FeatureFlagProvider
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
    fun provideNetworkDataSource(
        courseApi: CourseAPI.CoursesInterface,
        calendarEventApi: CalendarEventAPI.CalendarEventInterface,
        plannerApi: PlannerAPI.PlannerInterface
    ): SyllabusNetworkDataSource {
        return SyllabusNetworkDataSource(courseApi, calendarEventApi, plannerApi)
    }

    @Provides
    fun provideLocalDataSource(
        courseSettingsDao: CourseSettingsDao,
        courseFacade: CourseFacade,
        scheduleItemFacade: ScheduleItemFacade
    ): SyllabusLocalDataSource {
        return SyllabusLocalDataSource(courseSettingsDao, courseFacade, scheduleItemFacade)
    }

    @Provides
    fun provideSyllabusRepository(
        localDataSource: SyllabusLocalDataSource,
        networkDataSource: SyllabusNetworkDataSource,
        networkStateProvider: NetworkStateProvider,
        featureFlagProvider: FeatureFlagProvider
    ): SyllabusRepository {
        return SyllabusRepository(localDataSource, networkDataSource, networkStateProvider, featureFlagProvider)
    }
}