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

package com.instructure.student.di.feature

import com.instructure.canvasapi2.apis.AssignmentAPI
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.SubmissionAPI
import com.instructure.canvasapi2.managers.graphql.CustomGradeStatusesManager
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.features.grades.GradesRepository
import com.instructure.pandautils.features.grades.GradesViewModelBehavior
import com.instructure.pandautils.room.offline.daos.CustomGradeStatusDao
import com.instructure.pandautils.room.offline.facade.AssignmentFacade
import com.instructure.pandautils.room.offline.facade.CourseFacade
import com.instructure.pandautils.room.offline.facade.EnrollmentFacade
import com.instructure.pandautils.room.offline.facade.SubmissionFacade
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.features.grades.StudentGradesRepository
import com.instructure.student.features.grades.StudentGradesViewModelBehavior
import com.instructure.student.features.grades.datasource.GradesLocalDataSource
import com.instructure.student.features.grades.datasource.GradesNetworkDataSource
import com.instructure.student.util.StudentPrefs
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
class GradesModule {

    @Provides
    fun provideGradesListLocalDataSource(
        courseFacade: CourseFacade,
        enrollmentFacade: EnrollmentFacade,
        assignmentFacade: AssignmentFacade,
        submissionFacade: SubmissionFacade,
        customGradeStatusDao: CustomGradeStatusDao
    ): GradesLocalDataSource {
        return GradesLocalDataSource(courseFacade, enrollmentFacade, assignmentFacade, submissionFacade, customGradeStatusDao)
    }

    @Provides
    fun provideGradesListNetworkDataSource(
        courseApi: CourseAPI.CoursesInterface,
        assignmentApi: AssignmentAPI.AssignmentInterface,
        submissionApi: SubmissionAPI.SubmissionInterface,
        customGradeStatusesManager: CustomGradeStatusesManager
    ): GradesNetworkDataSource {
        return GradesNetworkDataSource(courseApi, assignmentApi, submissionApi, customGradeStatusesManager)
    }

    @Provides
    fun provideGradesRepository(
        localDataSource: GradesLocalDataSource,
        networkDataSource: GradesNetworkDataSource,
        networkStateProvider: NetworkStateProvider,
        featureFlagProvider: FeatureFlagProvider,
        apiPrefs: ApiPrefs,
        studentPrefs: StudentPrefs
    ): GradesRepository {
        return StudentGradesRepository(
            localDataSource,
            networkDataSource,
            networkStateProvider,
            featureFlagProvider,
            apiPrefs,
            studentPrefs
        )
    }

    @Provides
    fun provideGradesViewModelBehavior(): GradesViewModelBehavior {
        return StudentGradesViewModelBehavior()
    }
}
