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

import android.content.Context
import com.instructure.canvasapi2.apis.AssignmentAPI
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.QuizAPI
import com.instructure.canvasapi2.apis.SubmissionAPI
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsBehaviour
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsColorProvider
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsRepository
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsRouter
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsSubmissionHandler
import com.instructure.pandautils.room.offline.daos.QuizDao
import com.instructure.pandautils.room.offline.facade.AssignmentFacade
import com.instructure.pandautils.room.offline.facade.CourseFacade
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.features.assignments.details.StudentAssignmentDetailsBehaviour
import com.instructure.student.features.assignments.details.StudentAssignmentDetailsColorProvider
import com.instructure.student.features.assignments.details.StudentAssignmentDetailsRepository
import com.instructure.student.features.assignments.details.StudentAssignmentDetailsRouter
import com.instructure.student.features.assignments.details.StudentAssignmentDetailsSubmissionHandler
import com.instructure.student.features.assignments.details.datasource.AssignmentDetailsLocalDataSource
import com.instructure.student.features.assignments.details.datasource.AssignmentDetailsNetworkDataSource
import com.instructure.student.mobius.common.ui.SubmissionHelper
import com.instructure.student.room.StudentDb
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(FragmentComponent::class)
class AssignmentDetailsFragmentModule {
    @Provides
    fun provideAssignmentDetailsRouter(): AssignmentDetailsRouter {
        return StudentAssignmentDetailsRouter()
    }

    @Provides
    fun provideAssignmentDetailsBehaviour(router: AssignmentDetailsRouter): AssignmentDetailsBehaviour {
        return StudentAssignmentDetailsBehaviour(router)
    }
}
@Module
@InstallIn(ViewModelComponent::class)
class AssignmentDetailsModule {

    @Provides
    fun provideAssignmentDetailsLocalDataSource(
        courseFacade: CourseFacade,
        assignmentFacade: AssignmentFacade,
        quizDao: QuizDao
    ): AssignmentDetailsLocalDataSource {
        return AssignmentDetailsLocalDataSource(courseFacade, assignmentFacade, quizDao)
    }

    @Provides
    fun provideAssignmentDetailsNetworkDataSource(
        coursesInterface: CourseAPI.CoursesInterface,
        assignmentInterface: AssignmentAPI.AssignmentInterface,
        quizInterface: QuizAPI.QuizInterface,
        submissionInterface: SubmissionAPI.SubmissionInterface
    ): AssignmentDetailsNetworkDataSource {
        return AssignmentDetailsNetworkDataSource(coursesInterface, assignmentInterface, quizInterface, submissionInterface)
    }

    @Provides
    fun provideAssignmentDetailsRepository(
        networkStateProvider: NetworkStateProvider,
        localDataSource: AssignmentDetailsLocalDataSource,
        networkDataSource: AssignmentDetailsNetworkDataSource,
        featureFlagProvider: FeatureFlagProvider,
    ): AssignmentDetailsRepository {
        return StudentAssignmentDetailsRepository(localDataSource, networkDataSource, networkStateProvider, featureFlagProvider)
    }

    @Provides
    fun provideAssignmentDetailsSubmissionHandler(@ApplicationContext context: Context, submissionHandler: SubmissionHelper, studentDb: StudentDb): AssignmentDetailsSubmissionHandler {
        return StudentAssignmentDetailsSubmissionHandler(context, submissionHandler, studentDb)
    }

    @Provides
    fun provideAssignmentDetailsColorProvider(colorKeeper: ColorKeeper): AssignmentDetailsColorProvider {
        return StudentAssignmentDetailsColorProvider(colorKeeper)
    }
}