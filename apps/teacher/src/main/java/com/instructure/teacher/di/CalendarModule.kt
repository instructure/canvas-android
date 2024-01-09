package com.instructure.teacher.di

import androidx.fragment.app.FragmentActivity
import com.instructure.pandautils.features.calendar.CalendarRouter
import com.instructure.teacher.features.calendar.TeacherCalendarRouter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@Module
@InstallIn(FragmentComponent::class)
class CalendarModule {

    @Provides
    fun provideCalendarRouter(activity: FragmentActivity): CalendarRouter {
        return TeacherCalendarRouter(activity)
    }
}