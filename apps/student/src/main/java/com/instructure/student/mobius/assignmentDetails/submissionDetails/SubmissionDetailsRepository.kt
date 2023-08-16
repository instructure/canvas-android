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

package com.instructure.student.mobius.assignmentDetails.submissionDetails

import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.repository.Repository
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.mobius.assignmentDetails.submissionDetails.datasource.SubmissionDetailsDataSource
import com.instructure.student.mobius.assignmentDetails.submissionDetails.datasource.SubmissionDetailsLocalDataSource
import com.instructure.student.mobius.assignmentDetails.submissionDetails.datasource.SubmissionDetailsNetworkDataSource

class SubmissionDetailsRepository(
    localDataSource: SubmissionDetailsLocalDataSource,
    networkDataSource: SubmissionDetailsNetworkDataSource,
    networkStateProvider: NetworkStateProvider,
    featureFlagProvider: FeatureFlagProvider
) : Repository<SubmissionDetailsDataSource>(localDataSource, networkDataSource, networkStateProvider, featureFlagProvider) {

    suspend fun getObserveeEnrollments(forceNetwork: Boolean): DataResult<List<Enrollment>> {
        return dataSource().getObserveeEnrollments(forceNetwork)
    }

    suspend fun getSingleSubmission(courseId: Long, assignmentId: Long, studentId: Long, forceNetwork: Boolean): DataResult<Submission> {
        return dataSource().getSingleSubmission(courseId, assignmentId, studentId, forceNetwork)
    }

    suspend fun getAssignment(assignmentId: Long, courseId: Long, forceNetwork: Boolean): DataResult<Assignment> {
        return dataSource().getAssignment(assignmentId, courseId, forceNetwork)
    }

    suspend fun getExternalToolLaunchUrl(courseId: Long, externalToolId: Long, assignmentId: Long, forceNetwork: Boolean): DataResult<LTITool> {
        return dataSource().getExternalToolLaunchUrl(courseId, externalToolId, assignmentId, forceNetwork)
    }

    suspend fun getLtiFromAuthenticationUrl(url: String, forceNetwork: Boolean): DataResult<LTITool> {
        return dataSource().getLtiFromAuthenticationUrl(url, forceNetwork)
    }

    suspend fun getQuiz(courseId: Long, quizId: Long, forceNetwork: Boolean): DataResult<Quiz> {
        return dataSource().getQuiz(courseId, quizId, forceNetwork)
    }

    suspend fun getCourseFeatures(courseId: Long, forceNetwork: Boolean): DataResult<List<String>> {
        return dataSource().getCourseFeatures(courseId, forceNetwork)
    }

    suspend fun loadCourseSettings(courseId: Long, forceNetwork: Boolean): CourseSettings? {
        return dataSource().loadCourseSettings(courseId, forceNetwork)
    }
}
