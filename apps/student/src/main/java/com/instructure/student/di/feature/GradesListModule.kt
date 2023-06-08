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
import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.apis.SubmissionAPI
import com.instructure.pandautils.room.offline.facade.AssignmentFacade
import com.instructure.pandautils.room.offline.facade.CourseFacade
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.features.grades.GradesListRepository
import com.instructure.student.features.grades.datasource.GradesListLocalDataSource
import com.instructure.student.features.grades.datasource.GradesListNetworkDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@Module
@InstallIn(FragmentComponent::class)
class GradesListModule {

    @Provides
    fun provideAssignmentListLocalDataSource(
        assignmentFacade: AssignmentFacade,
        courseFacade: CourseFacade
    ): GradesListLocalDataSource {
        return GradesListLocalDataSource(assignmentFacade, courseFacade)
    }

    @Provides
    fun provideAssignmentListNetworkDataSource(
        courseApi: CourseAPI.CoursesInterface,
        enrollmentApi: EnrollmentAPI.EnrollmentInterface,
        assignmentApi: AssignmentAPI.AssignmentInterface,
        submissionApi: SubmissionAPI.SubmissionInterface
    ): GradesListNetworkDataSource {
        return GradesListNetworkDataSource(courseApi, enrollmentApi, assignmentApi, submissionApi)
    }

    @Provides
    fun provideAssignmentListRepository(
        localDataSource: GradesListLocalDataSource,
        networkDataSource: GradesListNetworkDataSource,
        networkStateProvider: NetworkStateProvider
    ): GradesListRepository {
        return GradesListRepository(localDataSource, networkDataSource, networkStateProvider)
    }
}