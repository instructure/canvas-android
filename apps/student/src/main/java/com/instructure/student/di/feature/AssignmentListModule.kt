/*
 * Copyright (C) 2023 - present Instructure, Inc.
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

package com.instructure.student.di.feature

import com.instructure.canvasapi2.apis.AssignmentAPI
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.pandautils.features.assignments.list.AssignmentListBehavior
import com.instructure.pandautils.features.assignments.list.AssignmentListRepository
import com.instructure.pandautils.features.assignments.list.AssignmentListRouter
import com.instructure.pandautils.room.assignment.list.daos.AssignmentListSelectedFiltersEntityDao
import com.instructure.pandautils.room.offline.daos.CourseSettingsDao
import com.instructure.pandautils.room.offline.facade.AssignmentFacade
import com.instructure.pandautils.room.offline.facade.CourseFacade
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.features.assignments.list.StudentAssignmentListBehavior
import com.instructure.student.features.assignments.list.StudentAssignmentListRepository
import com.instructure.student.features.assignments.list.StudentAssignmentListRouter
import com.instructure.student.features.assignments.list.datasource.AssignmentListLocalDataSource
import com.instructure.student.features.assignments.list.datasource.AssignmentListNetworkDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(FragmentComponent::class)
class AssignmentListFragmentModule {
    @Provides
    fun provideAssignmentListRouter(): AssignmentListRouter {
        return StudentAssignmentListRouter()
    }
}

@Module
@InstallIn(ViewModelComponent::class)
class AssignmentListViewModelModule{
    @Provides
    fun provideAssignmentListLocalDataSource(
        assignmentFacade: AssignmentFacade,
        courseFacade: CourseFacade,
        courseSettingsDao: CourseSettingsDao
    ): AssignmentListLocalDataSource {
        return AssignmentListLocalDataSource(assignmentFacade, courseFacade, courseSettingsDao)
    }

    @Provides
    fun provideAssignmentListNetworkDataSource(
        assignmentApi: AssignmentAPI.AssignmentInterface,
        courseApi: CourseAPI.CoursesInterface
    ): AssignmentListNetworkDataSource {
        return AssignmentListNetworkDataSource(assignmentApi, courseApi)
    }

    @Provides
    fun provideAssignmentListRepository(
        localDataSource: AssignmentListLocalDataSource,
        networkDataSource: AssignmentListNetworkDataSource,
        networkStateProvider: NetworkStateProvider,
        featureFlagProvider: FeatureFlagProvider,
        assignmentListSelectedFiltersEntityDao: AssignmentListSelectedFiltersEntityDao
    ): AssignmentListRepository {
        return StudentAssignmentListRepository(localDataSource, networkDataSource, networkStateProvider, featureFlagProvider, assignmentListSelectedFiltersEntityDao)
    }

    @Provides
    fun provideAssignmentListBehavior(): AssignmentListBehavior {
        return StudentAssignmentListBehavior()
    }
}