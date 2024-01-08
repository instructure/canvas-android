package com.instructure.student.di.feature

import androidx.fragment.app.FragmentActivity
import com.instructure.pandautils.features.calendar.CalendarRouter
import com.instructure.student.features.calendar.StudentCalendarRouter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
class CalendarModule {

    @Provides
    fun provideCalendarRouter(activity: FragmentActivity): CalendarRouter {
        return StudentCalendarRouter(activity)
    }
}