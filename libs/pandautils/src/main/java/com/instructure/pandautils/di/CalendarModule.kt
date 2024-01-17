package com.instructure.pandautils.di

import com.instructure.canvasapi2.apis.PlannerAPI
import com.instructure.pandautils.features.calendar.CalendarRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
class CalendarModule {

    @Provides
    fun provideCalendarRepository(plannerApi: PlannerAPI.PlannerInterface): CalendarRepository {
        return CalendarRepository(plannerApi)
    }
}