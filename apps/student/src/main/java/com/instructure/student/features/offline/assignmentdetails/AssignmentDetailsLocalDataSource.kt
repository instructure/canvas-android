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

import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.room.offline.daos.AssignmentDao
import com.instructure.pandautils.room.offline.daos.CourseDao
import com.instructure.pandautils.room.offline.daos.SubmissionDao

class AssignmentDetailsLocalDataSource(
    private val courseDao: CourseDao,
    private val assignmentDao: AssignmentDao,
    private val submissionDao: SubmissionDao
) {

    suspend fun getCourseWithGrade(courseId: Long): Course? {
        return courseDao.findById(courseId)?.toApiModel()
    }
}
