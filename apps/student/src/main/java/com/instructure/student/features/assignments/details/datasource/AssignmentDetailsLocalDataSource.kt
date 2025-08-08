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

import com.instructure.canvasapi2.CustomGradeStatusesQuery
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.models.Quiz
import com.instructure.pandautils.room.offline.daos.CustomGradeStatusDao
import com.instructure.pandautils.room.offline.daos.QuizDao
import com.instructure.pandautils.room.offline.facade.AssignmentFacade
import com.instructure.pandautils.room.offline.facade.CourseFacade

class AssignmentDetailsLocalDataSource(
    private val courseFacade: CourseFacade,
    private val assignmentFacade: AssignmentFacade,
    private val quizDao: QuizDao,
    private val customGradeStatusDao: CustomGradeStatusDao
) : AssignmentDetailsDataSource {

    override suspend fun getCourseWithGrade(courseId: Long, forceNetwork: Boolean): Course {
        return courseFacade.getCourseById(courseId) ?: throw IllegalStateException("Could not load from DB")
    }

    override suspend fun getAssignment(isObserver: Boolean, assignmentId: Long, courseId: Long, forceNetwork: Boolean): Assignment {
        return assignmentFacade.getAssignmentById(assignmentId) ?: throw IllegalStateException("Could not load from DB")
    }

    override suspend fun getQuiz(courseId: Long, quizId: Long, forceNetwork: Boolean): Quiz {
        return quizDao.findById(quizId)?.toApiModel() ?: throw IllegalStateException("Could not load from DB")
    }

    override suspend fun getExternalToolLaunchUrl(courseId: Long, externalToolId: Long, assignmentId: Long, forceNetwork: Boolean): LTITool? {
        return null
    }

    override suspend fun getLtiFromAuthenticationUrl(url: String, forceNetwork: Boolean): LTITool? {
        return null
    }

    override suspend fun getCustomGradeStatuses(courseId: Long, forceNetwork: Boolean): List<CustomGradeStatusesQuery.Node> {
        return customGradeStatusDao.getStatusesForCourse(courseId).map { it.toApiModel() }
    }
}
