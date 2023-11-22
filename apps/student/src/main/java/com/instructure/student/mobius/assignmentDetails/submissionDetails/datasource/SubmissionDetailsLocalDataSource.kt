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
import com.instructure.pandautils.room.offline.daos.CourseFeaturesDao
import com.instructure.pandautils.room.offline.daos.CourseSettingsDao
import com.instructure.pandautils.room.offline.daos.QuizDao
import com.instructure.pandautils.room.offline.facade.AssignmentFacade
import com.instructure.pandautils.room.offline.facade.EnrollmentFacade
import com.instructure.pandautils.room.offline.facade.SubmissionFacade

class SubmissionDetailsLocalDataSource(
    private val enrollmentFacade: EnrollmentFacade,
    private val submissionFacade: SubmissionFacade,
    private val assignmentFacade: AssignmentFacade,
    private val quizDao: QuizDao,
    private val courseFeaturesDao: CourseFeaturesDao,
    private val courseSettingsDao: CourseSettingsDao
) : SubmissionDetailsDataSource {

    override suspend fun getObserveeEnrollments(forceNetwork: Boolean): DataResult<List<Enrollment>> {
        return DataResult.Success(enrollmentFacade.getAllEnrollments())
    }

    override suspend fun getSingleSubmission(courseId: Long, assignmentId: Long, studentId: Long, forceNetwork: Boolean): DataResult<Submission> {
        val submission = submissionFacade.findByAssignmentId(assignmentId)
        return submission?.let { DataResult.Success(it) } ?: DataResult.Fail()
    }

    override suspend fun getAssignment(assignmentId: Long, courseId: Long, forceNetwork: Boolean): DataResult<Assignment> {
        val assignment = assignmentFacade.getAssignmentById(assignmentId)
        return assignment?.let { DataResult.Success(it) } ?: DataResult.Fail()
    }

    override suspend fun getExternalToolLaunchUrl(
        courseId: Long,
        externalToolId: Long,
        assignmentId: Long,
        forceNetwork: Boolean
    ): DataResult<LTITool> {
        return DataResult.Fail()
    }

    override suspend fun getLtiFromAuthenticationUrl(url: String, forceNetwork: Boolean): DataResult<LTITool> {
        return DataResult.Fail()
    }

    override suspend fun getQuiz(courseId: Long, quizId: Long, forceNetwork: Boolean): DataResult<Quiz> {
        val quiz = quizDao.findById(quizId)?.toApiModel()
        return quiz?.let { DataResult.Success(it) } ?: DataResult.Fail()
    }

    override suspend fun getCourseFeatures(courseId: Long, forceNetwork: Boolean): DataResult<List<String>> {
        val features = courseFeaturesDao.findByCourseId(courseId)?.features
        return features?.let { DataResult.Success(it) } ?: DataResult.Fail()
    }

    override suspend fun loadCourseSettings(courseId: Long, forceNetwork: Boolean): CourseSettings? {
        return courseSettingsDao.findByCourseId(courseId)?.toApiModel()
    }
}
