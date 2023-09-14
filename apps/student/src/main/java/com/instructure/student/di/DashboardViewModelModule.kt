/*
 * Copyright (C) 2022 - present Instructure, Inc.
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

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.GroupAPI
import com.instructure.pandautils.features.dashboard.edit.EditDashboardRepository
import com.instructure.pandautils.room.offline.daos.CourseDao
import com.instructure.pandautils.room.offline.daos.CourseSyncSettingsDao
import com.instructure.pandautils.room.offline.daos.EditDashboardItemDao
import com.instructure.pandautils.room.offline.facade.CourseFacade
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.features.dashboard.edit.StudentEditDashboardRepository
import com.instructure.student.features.dashboard.edit.datasource.StudentEditDashboardLocalDataSource
import com.instructure.student.features.dashboard.edit.datasource.StudentEditDashboardNetworkDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
class DashboardViewModelModule {

    @Provides
    fun provideStudentEditDashboardLocalDataSource(courseFacade: CourseFacade,
                                                   editDashboardItemDao: EditDashboardItemDao): StudentEditDashboardLocalDataSource {
        return StudentEditDashboardLocalDataSource(courseFacade, editDashboardItemDao)
    }

    @Provides
    fun provideStudentEditDashboardNetworkDataSource(courseApi: CourseAPI.CoursesInterface,
                                                     groupApi: GroupAPI.GroupInterface): StudentEditDashboardNetworkDataSource {
        return StudentEditDashboardNetworkDataSource(courseApi, groupApi)
    }

    @Provides
    fun provideEditDashboardRepository(localDataSource: StudentEditDashboardLocalDataSource,
                                       networkDataSource: StudentEditDashboardNetworkDataSource,
                                       networkStateProvider: NetworkStateProvider,
                                       featureFlagProvider: FeatureFlagProvider,
                                       courseSyncSettingsDao: CourseSyncSettingsDao,
                                       courseDao: CourseDao
    ): EditDashboardRepository {
        return StudentEditDashboardRepository(localDataSource, networkDataSource, networkStateProvider, featureFlagProvider, courseSyncSettingsDao, courseDao)
    }
}
