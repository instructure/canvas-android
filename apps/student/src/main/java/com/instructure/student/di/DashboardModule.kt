/*
 * Copyright (C) 2021 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.student.di

import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.GroupAPI
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.features.dashboard.edit.EditDashboardRouter
import com.instructure.pandautils.features.dashboard.notifications.DashboardRouter
import com.instructure.pandautils.room.offline.daos.CourseDao
import com.instructure.pandautils.room.offline.daos.CourseSyncSettingsDao
import com.instructure.pandautils.room.offline.daos.DashboardCardDao
import com.instructure.pandautils.room.offline.facade.CourseFacade
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.features.dashboard.DashboardLocalDataSource
import com.instructure.student.features.dashboard.DashboardNetworkDataSource
import com.instructure.student.features.dashboard.DashboardRepository
import com.instructure.student.features.dashboard.edit.StudentEditDashboardRouter
import com.instructure.student.features.dashboard.notifications.StudentDashboardRouter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@Module
@InstallIn(FragmentComponent::class)
class DashboardModule {

    @Provides
    fun provideDashboardRouter(activity: FragmentActivity): DashboardRouter {
        return StudentDashboardRouter(activity)
    }

    @Provides
    fun provideEditDashboardRouter(activity: FragmentActivity): EditDashboardRouter {
        return StudentEditDashboardRouter(activity)
    }

    @Provides
    fun provideDashboardNetworkDataSource(
        courseApi: CourseAPI.CoursesInterface,
        groupApi: GroupAPI.GroupInterface,
        apiPrefs: ApiPrefs
    ): DashboardNetworkDataSource {
        return DashboardNetworkDataSource(courseApi, groupApi, apiPrefs)
    }

    @Provides
    fun provideDashboardLocalDataSource(courseFacade: CourseFacade, dashboardCardDao: DashboardCardDao): DashboardLocalDataSource {
        return DashboardLocalDataSource(courseFacade, dashboardCardDao)
    }

    @Provides
    fun provideDashboardRepository(
        networkDataSource: DashboardNetworkDataSource,
        localDataSource: DashboardLocalDataSource,
        networkStateProvider: NetworkStateProvider,
        featureFlagProvider: FeatureFlagProvider,
        courseSyncSettingsDao: CourseSyncSettingsDao,
        courseDao: CourseDao
    ): DashboardRepository {
        return DashboardRepository(localDataSource, networkDataSource, networkStateProvider, featureFlagProvider, courseSyncSettingsDao, courseDao)
    }
}