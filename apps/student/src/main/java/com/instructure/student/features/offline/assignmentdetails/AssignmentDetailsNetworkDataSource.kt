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

package com.instructure.student.features.offline.assignmentdetails

import com.instructure.canvasapi2.apis.AssignmentAPI
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.QuizAPI
import com.instructure.canvasapi2.apis.SubmissionAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Quiz
import com.instructure.canvasapi2.utils.DataResult

class AssignmentDetailsNetworkDataSource(
    private val coursesInterface: CourseAPI.CoursesInterface,
    private val assignmentInterface: AssignmentAPI.AssignmentInterface,
    private val quizInterface: QuizAPI.QuizInterface,
    private val submissionInterface: SubmissionAPI.SubmissionInterface
) {

    suspend fun getCourseWithGrade(courseId: Long, forceNetwork: Boolean): DataResult<Course> {
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        return coursesInterface.getCourseWithGrade(courseId, params)
    }

    suspend fun getAssignmentIncludeObservees(assignmentId: Long, courseId: Long, forceNetwork: Boolean): DataResult<Assignment?> {
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        return assignmentInterface.getAssignmentIncludeObservees(courseId, assignmentId, params).map { it.toAssignmentForObservee() }
    }

    suspend fun getAssignmentWithHistory(assignmentId: Long, courseId: Long, forceNetwork: Boolean): DataResult<Assignment> {
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        return assignmentInterface.getAssignmentWithHistory(courseId, assignmentId, params)
    }

    suspend fun getQuiz(courseId: Long, quizId: Long, forceNetwork: Boolean): DataResult<Quiz> {
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        return quizInterface.getQuiz(courseId, quizId, params)
    }
}
