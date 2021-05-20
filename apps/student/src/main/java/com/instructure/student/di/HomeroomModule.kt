/*
 * Copyright (C) 2021 - present Instructure, Inc.
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
package com.instructure.student.di

import android.content.res.Resources
import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.managers.AnnouncementManager
import com.instructure.canvasapi2.managers.PlannerManager
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.pandautils.features.elementary.homeroom.CourseCardCreator
import com.instructure.pandautils.features.elementary.homeroom.HomeroomRouter
import com.instructure.student.mobius.elementary.StudentHomeroomRouter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(FragmentComponent::class)
class HomeroomModule {

    @Provides
    fun provideHomeroomRouter(activity: FragmentActivity): HomeroomRouter {
        return StudentHomeroomRouter(activity)
    }
}

@Module
@InstallIn(ViewModelComponent::class)
class HomeroomViewModelModule {

    @Provides
    fun provideCourseCardCreator(plannerManager: PlannerManager, userManager: UserManager,
                                 announcementManager: AnnouncementManager, resources: Resources): CourseCardCreator {
        return CourseCardCreator(plannerManager, userManager, announcementManager, resources)
    }
}