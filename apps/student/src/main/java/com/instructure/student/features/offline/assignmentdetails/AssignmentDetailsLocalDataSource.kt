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

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Quiz
import com.instructure.pandautils.room.offline.daos.QuizDao
import com.instructure.pandautils.room.offline.facade.AssignmentFacade
import com.instructure.pandautils.room.offline.facade.CourseFacade

class AssignmentDetailsLocalDataSource(
    private val courseFacade: CourseFacade,
    private val assignmentFacade: AssignmentFacade,
    private val quizDao: QuizDao
) {

    suspend fun getCourseWithGrade(courseId: Long): Course? {
        return courseFacade.getCourseById(courseId)
    }

    suspend fun getAssignment(assignmentId: Long): Assignment? {
        return assignmentFacade.getAssignmentById(assignmentId)
    }

    suspend fun getQuiz(quizId: Long): Quiz? {
        return quizDao.findById(quizId)?.toApiModel()
    }
}
