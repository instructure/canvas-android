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

package com.instructure.student.features.assignments.details.datasource

import com.instructure.canvasapi2.apis.AssignmentAPI
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.QuizAPI
import com.instructure.canvasapi2.apis.SubmissionAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.models.Quiz

class AssignmentDetailsNetworkDataSource(
    private val coursesInterface: CourseAPI.CoursesInterface,
    private val assignmentInterface: AssignmentAPI.AssignmentInterface,
    private val quizInterface: QuizAPI.QuizInterface,
    private val submissionInterface: SubmissionAPI.SubmissionInterface
) : AssignmentDetailsDataSource {

    override suspend fun getCourseWithGrade(courseId: Long, forceNetwork: Boolean): Course {
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        return coursesInterface.getCourseWithGrade(courseId, params).dataOrThrow
    }

    override suspend fun getAssignment(isObserver: Boolean, assignmentId: Long, courseId: Long, forceNetwork: Boolean): Assignment {
        return if (isObserver) {
            getAssignmentIncludeObservees(assignmentId, courseId, forceNetwork)
        } else {
            getAssignmentWithHistory(assignmentId, courseId, forceNetwork)
        }
    }

    override suspend fun getQuiz(courseId: Long, quizId: Long, forceNetwork: Boolean): Quiz {
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        return quizInterface.getQuiz(courseId, quizId, params).dataOrThrow
    }

    override suspend fun getExternalToolLaunchUrl(courseId: Long, externalToolId: Long, assignmentId: Long, forceNetwork: Boolean): LTITool {
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        return assignmentInterface.getExternalToolLaunchUrl(courseId, externalToolId, assignmentId, restParams = params).dataOrThrow
    }

    override suspend fun getLtiFromAuthenticationUrl(url: String, forceNetwork: Boolean): LTITool {
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        return submissionInterface.getLtiFromAuthenticationUrl(url, params).dataOrThrow
    }

    private suspend fun getAssignmentIncludeObservees(assignmentId: Long, courseId: Long, forceNetwork: Boolean): Assignment {
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        return assignmentInterface.getAssignmentIncludeObservees(courseId, assignmentId, params).map { it.toAssignmentForObservee() }.dataOrThrow
    }

    private suspend fun getAssignmentWithHistory(assignmentId: Long, courseId: Long, forceNetwork: Boolean): Assignment {
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        return assignmentInterface.getAssignmentWithHistory(courseId, assignmentId, params).dataOrThrow
    }
}
