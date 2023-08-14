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

package com.instructure.student.mobius.assignmentDetails.submissionDetails.datasource

import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.DataResult

interface SubmissionDetailsDataSource {

    suspend fun getObserveeEnrollments(forceNetwork: Boolean): DataResult<List<Enrollment>>

    suspend fun getSingleSubmission(courseId: Long, assignmentId: Long, studentId: Long, forceNetwork: Boolean): DataResult<Submission>

    suspend fun getAssignment(assignmentId: Long, courseId: Long, forceNetwork: Boolean): DataResult<Assignment>

    suspend fun getExternalToolLaunchUrl(courseId: Long, externalToolId: Long, assignmentId: Long, forceNetwork: Boolean): DataResult<LTITool>

    suspend fun getLtiFromAuthenticationUrl(url: String, forceNetwork: Boolean): DataResult<LTITool>

    suspend fun getQuiz(courseId: Long, quizId: Long, forceNetwork: Boolean): DataResult<Quiz>

    suspend fun getCourseFeatures(courseId: Long, forceNetwork: Boolean): DataResult<List<String>>

    suspend fun loadCourseSettings(courseId: Long, forceNetwork: Boolean): CourseSettings?
}
