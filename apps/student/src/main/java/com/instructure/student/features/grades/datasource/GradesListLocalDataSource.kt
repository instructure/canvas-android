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

package com.instructure.student.features.grades.datasource

import com.instructure.canvasapi2.models.*
import com.instructure.pandautils.room.offline.facade.AssignmentFacade
import com.instructure.pandautils.room.offline.facade.CourseFacade
import com.instructure.pandautils.room.offline.facade.EnrollmentFacade
import com.instructure.pandautils.room.offline.facade.SubmissionFacade

class GradesListLocalDataSource(
    private val courseFacade: CourseFacade,
    private val enrollmentFacade: EnrollmentFacade,
    private val assignmentFacade: AssignmentFacade,
    private val submissionFacade: SubmissionFacade
) : GradesListDataSource {

    override suspend fun getCourseWithGrade(courseId: Long, forceNetwork: Boolean): Course {
        return courseFacade.getCourseById(courseId) ?: throw IllegalStateException("Could not load from DB")
    }

    override suspend fun getObserveeEnrollments(forceNetwork: Boolean): List<Enrollment> {
        return enrollmentFacade.getAllEnrollments()
    }

    override suspend fun getAssignmentGroupsWithAssignmentsForGradingPeriod(
        courseId: Long,
        gradingPeriodId: Long,
        scopeToStudent: Boolean,
        forceNetwork: Boolean
    ): List<AssignmentGroup> {
        return assignmentFacade.getAssignmentGroupsWithAssignmentsForGradingPeriod(courseId, gradingPeriodId)
    }

    override suspend fun getSubmissionsForMultipleAssignments(
        studentId: Long,
        courseId: Long,
        assignmentIds: List<Long>,
        forceNetwork: Boolean
    ): List<Submission> {
        return submissionFacade.findByAssignmentIds(assignmentIds)
    }

    override suspend fun getCoursesWithSyllabus(forceNetwork: Boolean): List<Course> {
        return courseFacade.getAllCourses()
    }

    override suspend fun getGradingPeriodsForCourse(courseId: Long, forceNetwork: Boolean): List<GradingPeriod> {
        return courseFacade.getGradingPeriodsByCourseId(courseId)
    }

    override suspend fun getUserEnrollmentsForGradingPeriod(
        courseId: Long,
        userId: Long,
        gradingPeriodId: Long,
        forceNetwork: Boolean
    ): List<Enrollment> {
        return enrollmentFacade.getEnrollmentsByGradingPeriodId(gradingPeriodId)
    }

    override suspend fun getAssignmentGroupsWithAssignments(courseId: Long, forceNetwork: Boolean): List<AssignmentGroup> {
        return assignmentFacade.getAssignmentGroupsWithAssignments(courseId)
    }
}
