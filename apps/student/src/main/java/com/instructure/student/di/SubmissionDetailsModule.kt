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

package com.instructure.student.di

import com.instructure.canvasapi2.apis.AssignmentAPI
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.apis.FeaturesAPI
import com.instructure.canvasapi2.apis.QuizAPI
import com.instructure.canvasapi2.apis.SubmissionAPI
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.room.offline.daos.CourseFeaturesDao
import com.instructure.pandautils.room.offline.daos.CourseSettingsDao
import com.instructure.pandautils.room.offline.daos.QuizDao
import com.instructure.pandautils.room.offline.facade.AssignmentFacade
import com.instructure.pandautils.room.offline.facade.EnrollmentFacade
import com.instructure.pandautils.room.offline.facade.SubmissionFacade
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.mobius.assignmentDetails.submissionDetails.SubmissionDetailsRepository
import com.instructure.student.mobius.assignmentDetails.submissionDetails.datasource.SubmissionDetailsLocalDataSource
import com.instructure.student.mobius.assignmentDetails.submissionDetails.datasource.SubmissionDetailsNetworkDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@Module
@InstallIn(FragmentComponent::class)
class SubmissionDetailsModule {

    @Provides
    fun provideNetworkDataSource(
        enrollmentApi: EnrollmentAPI.EnrollmentInterface,
        submissionApi: SubmissionAPI.SubmissionInterface,
        assignmentApi: AssignmentAPI.AssignmentInterface,
        quizApi: QuizAPI.QuizInterface,
        featuresApi: FeaturesAPI.FeaturesInterface,
        courseApi: CourseAPI.CoursesInterface,
        apiPrefs: ApiPrefs
    ): SubmissionDetailsNetworkDataSource {
        return SubmissionDetailsNetworkDataSource(
            enrollmentApi,
            submissionApi,
            assignmentApi,
            quizApi,
            featuresApi,
            courseApi,
            apiPrefs
        )
    }

    @Provides
    fun provideLocalDataSource(
        enrollmentFacade: EnrollmentFacade,
        submissionFacade: SubmissionFacade,
        assignmentFacade: AssignmentFacade,
        quizDao: QuizDao,
        courseFeaturesDao: CourseFeaturesDao,
        courseSettingsDao: CourseSettingsDao
    ): SubmissionDetailsLocalDataSource {
        return SubmissionDetailsLocalDataSource(enrollmentFacade, submissionFacade, assignmentFacade, quizDao, courseFeaturesDao, courseSettingsDao)
    }

    @Provides
    fun provideRepository(
        localDataSource: SubmissionDetailsLocalDataSource,
        networkDataSource: SubmissionDetailsNetworkDataSource,
        networkStateProvider: NetworkStateProvider,
        featureFlagProvider: FeatureFlagProvider
    ): SubmissionDetailsRepository {
        return SubmissionDetailsRepository(localDataSource, networkDataSource, networkStateProvider, featureFlagProvider)
    }
}
