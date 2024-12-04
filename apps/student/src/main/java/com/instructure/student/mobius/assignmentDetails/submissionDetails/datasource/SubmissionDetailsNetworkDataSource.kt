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

import com.instructure.canvasapi2.apis.*
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.depaginate

class SubmissionDetailsNetworkDataSource(
    private val enrollmentApi: EnrollmentAPI.EnrollmentInterface,
    private val submissionApi: SubmissionAPI.SubmissionInterface,
    private val assignmentApi: AssignmentAPI.AssignmentInterface,
    private val quizApi: QuizAPI.QuizInterface,
    private val featuresApi: FeaturesAPI.FeaturesInterface,
    private val courseApi: CourseAPI.CoursesInterface
) : SubmissionDetailsDataSource {

    override suspend fun getObserveeEnrollments(forceNetwork: Boolean): DataResult<List<Enrollment>> {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        return enrollmentApi.firstPageObserveeEnrollments(params).depaginate {
            enrollmentApi.getNextPage(it, params)
        }
    }

    override suspend fun getSingleSubmission(courseId: Long, assignmentId: Long, studentId: Long, forceNetwork: Boolean): DataResult<Submission> {
        val params = RestParams(isForceReadFromNetwork = forceNetwork, domain = ApiPrefs.overrideDomains[courseId])
        return submissionApi.getSingleSubmission(courseId, assignmentId, studentId, params)
    }

    override suspend fun getAssignment(assignmentId: Long, courseId: Long, forceNetwork: Boolean): DataResult<Assignment> {
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        return assignmentApi.getAssignment(courseId, assignmentId, params)
    }

    override suspend fun getExternalToolLaunchUrl(
        courseId: Long,
        externalToolId: Long,
        assignmentId: Long,
        forceNetwork: Boolean
    ): DataResult<LTITool> {
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        return assignmentApi.getExternalToolLaunchUrl(courseId, externalToolId, assignmentId, restParams = params)
    }

    override suspend fun getLtiFromAuthenticationUrl(url: String, forceNetwork: Boolean): DataResult<LTITool> {
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        return submissionApi.getLtiFromAuthenticationUrl(url, params)
    }

    override suspend fun getQuiz(courseId: Long, quizId: Long, forceNetwork: Boolean): DataResult<Quiz> {
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        return quizApi.getQuiz(courseId, quizId, params)
    }

    override suspend fun getCourseFeatures(courseId: Long, forceNetwork: Boolean): DataResult<List<String>> {
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        return featuresApi.getEnabledFeaturesForCourse(courseId, params)
    }

    override suspend fun loadCourseSettings(courseId: Long, forceNetwork: Boolean): CourseSettings? {
        val restParams = RestParams(isForceReadFromNetwork = forceNetwork)
        return courseApi.getCourseSettings(courseId, restParams).dataOrNull
    }
}
