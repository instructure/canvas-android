/*
 * Copyright (C) 2024 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.teacher.di

import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.apis.CalendarEventAPI
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.FeaturesAPI
import com.instructure.canvasapi2.apis.PlannerAPI
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.features.calendar.CalendarBehavior
import com.instructure.pandautils.features.calendar.CalendarRepository
import com.instructure.pandautils.features.calendar.CalendarRouter
import com.instructure.pandautils.room.calendar.daos.CalendarFilterDao
import com.instructure.teacher.features.calendar.TeacherCalendarRepository
import com.instructure.teacher.features.calendar.TeacherCalendarRouter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(FragmentComponent::class)
class CalendarModule {

    @Provides
    fun provideCalendarRouter(activity: FragmentActivity): CalendarRouter {
        return TeacherCalendarRouter(activity)
    }

    @Module
    @InstallIn(ViewModelComponent::class)
    class CalendarViewModelModule {

        @Provides
        fun provideCalendarRepository(
            plannerApi: PlannerAPI.PlannerInterface,
            coursesApi: CourseAPI.CoursesInterface,
            calendarEventsApi: CalendarEventAPI.CalendarEventInterface,
            apiPrefs: ApiPrefs,
            featuresApi: FeaturesAPI.FeaturesInterface,
            calendarFilterDao: CalendarFilterDao
        ): CalendarRepository {
            return TeacherCalendarRepository(plannerApi, coursesApi, calendarEventsApi, apiPrefs, featuresApi, calendarFilterDao)
        }

        @Provides
        fun provideCalendarBehavior(): CalendarBehavior {
            return object : CalendarBehavior {}
        }
    }
}