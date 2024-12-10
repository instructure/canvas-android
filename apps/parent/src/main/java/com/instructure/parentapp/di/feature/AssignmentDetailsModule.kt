/*
 * Copyright (C) 2024 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.parentapp.di.feature

import com.instructure.canvasapi2.apis.AssignmentAPI
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.FeaturesAPI
import com.instructure.canvasapi2.apis.QuizAPI
import com.instructure.canvasapi2.apis.SubmissionAPI
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsBehaviour
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsColorProvider
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsRepository
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsRouter
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsSubmissionHandler
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.parentapp.features.assignment.details.ParentAssignmentDetailsBehaviour
import com.instructure.parentapp.features.assignment.details.ParentAssignmentDetailsColorProvider
import com.instructure.parentapp.features.assignment.details.ParentAssignmentDetailsRepository
import com.instructure.parentapp.features.assignment.details.ParentAssignmentDetailsRouter
import com.instructure.parentapp.features.assignment.details.ParentAssignmentDetailsSubmissionHandler
import com.instructure.parentapp.util.ParentPrefs
import com.instructure.parentapp.util.navigation.Navigation
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(FragmentComponent::class)
class AssignmentDetailsFragmentModule {
    @Provides
    fun provideAssignmentDetailsRouter(navigation: Navigation, parentPrefs: ParentPrefs, apiPrefs: ApiPrefs): AssignmentDetailsRouter {
        return ParentAssignmentDetailsRouter(navigation, parentPrefs, apiPrefs)
    }

    @Provides
    fun provideAssignmentDetailsBehaviour(parentPrefs: ParentPrefs, apiPrefs: ApiPrefs): AssignmentDetailsBehaviour {
        return ParentAssignmentDetailsBehaviour(parentPrefs, apiPrefs)
    }
}

@Module
@InstallIn(ViewModelComponent::class)
class AssignmentDetailsModule {
    @Provides
    fun provideAssignmentDetailsRepository(
        coursesApi: CourseAPI.CoursesInterface,
        assignmentApi: AssignmentAPI.AssignmentInterface,
        quizApi: QuizAPI.QuizInterface,
        submissionApi: SubmissionAPI.SubmissionInterface,
        featuresApi: FeaturesAPI.FeaturesInterface,
        parentPrefs: ParentPrefs
    ): AssignmentDetailsRepository {
        return ParentAssignmentDetailsRepository(coursesApi, assignmentApi, quizApi, submissionApi, featuresApi, parentPrefs)
    }

    @Provides
    fun provideAssignmentDetailsSubmissionHandler(): AssignmentDetailsSubmissionHandler {
        return ParentAssignmentDetailsSubmissionHandler()
    }

    @Provides
    fun provideAssignmentDetailsColorProvider(parentPrefs: ParentPrefs, colorKeeper: ColorKeeper): AssignmentDetailsColorProvider {
        return ParentAssignmentDetailsColorProvider(parentPrefs, colorKeeper)
    }
}