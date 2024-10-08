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
package com.instructure.teacher.di

import com.instructure.pandautils.features.assignments.details.AssignmentDetailsRepository
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsRouter
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsSubmissionHandler
import com.instructure.pandautils.receivers.alarm.AlarmReceiverNotificationHandler
import com.instructure.teacher.features.assignment.details.TeacherAlarmReceiverNotificationHandler
import com.instructure.teacher.features.assignment.details.TeacherAssignmentDetailsRepository
import com.instructure.teacher.features.assignment.details.TeacherAssignmentDetailsRouter
import com.instructure.teacher.features.assignment.details.TeacherAssignmentDetailsSubmissionHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(FragmentComponent::class)
class AssignmentDetailsFragmentModule() {
    @Provides
    fun provideAssignmentDetailsRouter(): AssignmentDetailsRouter {
        return TeacherAssignmentDetailsRouter()
    }
}

@Module
@InstallIn(ViewModelComponent::class)
class AssignmentDetailsModule {
    @Provides
    fun provideAssignmentDetailsRepository(): AssignmentDetailsRepository {
        return TeacherAssignmentDetailsRepository()
    }

    @Provides
    fun provideAssignmentDetailsSubmissionHandler(): AssignmentDetailsSubmissionHandler {
        return TeacherAssignmentDetailsSubmissionHandler()
    }
}

@Module
@InstallIn(SingletonComponent::class)
class AssignmentDetailsSingletonModule {
    @Provides
    fun provideAssignmentDetailsNotificationHandler(): AlarmReceiverNotificationHandler {
        return TeacherAlarmReceiverNotificationHandler()
    }
}