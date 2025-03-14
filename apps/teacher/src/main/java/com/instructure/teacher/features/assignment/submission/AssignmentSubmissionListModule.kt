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
 */package com.instructure.teacher.features.assignment.submission

import com.instructure.canvasapi2.apis.AssignmentAPI
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.apis.SectionAPI
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class AssignmentSubmissionListModule {

    @Provides
    fun provideAssignmentSubmissionListRepository(
        assignmentApi: AssignmentAPI.AssignmentInterface,
        enrollmentApi: EnrollmentAPI.EnrollmentInterface,
        courseApi: CourseAPI.CoursesInterface,
        sectionApi: SectionAPI.SectionsInterface
    ): AssignmentSubmissionRepository {
        return AssignmentSubmissionRepository(assignmentApi, enrollmentApi, courseApi, sectionApi)
    }
}