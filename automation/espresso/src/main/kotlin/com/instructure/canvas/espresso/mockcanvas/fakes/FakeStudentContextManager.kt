/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.canvas.espresso.mockcanvas.fakes

import com.instructure.canvas.espresso.mockcanvas.MockCanvas
import com.instructure.canvasapi2.StudentContextCardQuery
import com.instructure.canvasapi2.managers.StudentContextManager
import com.instructure.canvasapi2.type.AssignmentState
import com.instructure.canvasapi2.type.EnrollmentType
import com.instructure.canvasapi2.type.GradingType
import com.instructure.canvasapi2.type.SubmissionGradingStatus
import com.instructure.canvasapi2.type.SubmissionType

class FakeStudentContextManager : StudentContextManager {

    override val hasNextPage: Boolean = false

    override suspend fun getStudentContext(
        courseId: Long,
        userId: Long,
        submissionPageSize: Int,
        forceNetwork: Boolean
    ): StudentContextCardQuery.Data {
        val course = MockCanvas.data.courses.values.first { it.id == courseId }
        var user = MockCanvas.data.students.firstOrNull { it.id == userId }

        val isStudent = user != null
        if (user == null) user = MockCanvas.data.teachers.first { it.id == userId }

        val mockData = StudentContextCardQuery.Data(
            StudentContextCardQuery.Course(
                "Course",
                StudentContextCardQuery.OnCourse(
                    course.id.toString(), course.name,
                    StudentContextCardQuery.Permissions(false, true, true, true, true),
                    StudentContextCardQuery.Users(
                        mutableListOf(
                            StudentContextCardQuery.Edge(
                                StudentContextCardQuery.User(
                                    user.id.toString(),
                                    user.name,
                                    user.shortName,
                                    null,
                                    null,
                                    user.email,
                                    mutableListOf(
                                        StudentContextCardQuery.Enrollment(
                                            null,
                                            if (isStudent) EnrollmentType.StudentEnrollment else EnrollmentType.TeacherEnrollment,
                                            StudentContextCardQuery.Section(course.name),
                                            StudentContextCardQuery.Grades(null, null, "F", 45.5, "F", 3.25, "F", 45.5, "F", 3.25)
                                        )
                                    ),
                                    null
                                )
                            )
                        )
                    ),
                    StudentContextCardQuery.Submissions(
                        StudentContextCardQuery.PageInfo("Mg", "MQ", false, false),
                        if (isStudent) getSubmissions(userId) else emptyList()
                    )
                )
            )

        )

        return mockData
    }

    override suspend fun getNextPage(
        courseId: Long,
        userId: Long,
        forceNetwork: Boolean
    ): StudentContextCardQuery.Data? {
        return null
    }

    private fun getSubmissions(studentId: Long): List<StudentContextCardQuery.Edge1> {
        val assignments = MockCanvas.data.assignments
        return assignments.values.mapNotNull {
            val submission = MockCanvas.data.submissions[it.id]?.firstOrNull()
            if (submission != null) {
                StudentContextCardQuery.Edge1(
                    StudentContextCardQuery.Submission(
                        submission.id.toString(),
                        submission.score,
                        submission.grade,
                        submission.excused,
                        "unsubmitted",
                        SubmissionGradingStatus.graded,
                        true,
                        submission.postedAt,
                        StudentContextCardQuery.User1(studentId.toString()),
                        StudentContextCardQuery.Assignment(
                            it.id.toString(),
                            it.name,
                            it.htmlUrl,
                            it.pointsPossible,
                            AssignmentState.published,
                            GradingType.percent,
                            mutableListOf(SubmissionType.online_text_entry)
                        ),
                        null
                    )
                )
            } else {
                null
            }
        }
    }
}