package com.instructure.teacher.di

import com.instructure.canvasapi2.apis.CalendarEventAPI
import com.instructure.canvasapi2.apis.PlannerAPI
import com.instructure.teacher.features.syllabus.SyllabusRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@Module
@InstallIn(FragmentComponent::class)
class SyllabusModule {

    @Provides
    fun provideSyllabusRepository(
        plannerApi: PlannerAPI.PlannerInterface,
        calendarEventApi: CalendarEventAPI.CalendarEventInterface
    ): SyllabusRepository {
        return SyllabusRepository(plannerApi, calendarEventApi)
    }
}
