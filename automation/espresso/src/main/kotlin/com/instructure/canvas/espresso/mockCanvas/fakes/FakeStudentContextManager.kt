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
package com.instructure.canvas.espresso.mockCanvas.fakes

import com.apollographql.apollo.api.Input
import com.apollographql.apollo.api.Response
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvasapi2.QLCallback
import com.instructure.canvasapi2.StudentContextCardQuery
import com.instructure.canvasapi2.StudentContextCardQuery.AsCourse
import com.instructure.canvasapi2.managers.StudentContextManager
import com.instructure.canvasapi2.type.AssignmentState
import com.instructure.canvasapi2.type.EnrollmentType
import com.instructure.canvasapi2.type.GradingType
import com.instructure.canvasapi2.type.SubmissionGradingStatus
import com.instructure.canvasapi2.type.SubmissionType

class FakeStudentContextManager : StudentContextManager {

    override fun getStudentContext(
        courseId: Long,
        userId: Long,
        submissionPageSize: Int,
        forceNetwork: Boolean,
        callback: QLCallback<StudentContextCardQuery.Data>
    ) {
        callback.onResponse(getStudentContextResponse(callback.nextCursor, courseId, userId))
    }

    private fun getStudentContextResponse(nextCursor: String?, courseId: Long, userId: Long): Response<StudentContextCardQuery.Data> {
        val course = MockCanvas.data.courses.values.first { it.id == courseId }
        var user = MockCanvas.data.students.firstOrNull { it.id == userId }

        val isStudent = user != null
        if (user == null) user = MockCanvas.data.teachers.first { it.id == userId }

        val mockData = StudentContextCardQuery.Data(
            AsCourse(
                "Course", course.id.toString(), course.name,
                StudentContextCardQuery.Permissions("CoursePermission", false, true, true, true, true),
                StudentContextCardQuery.Users(
                    "UserConnection",
                    mutableListOf(
                        StudentContextCardQuery.Edge(
                            "UserEdge",
                            StudentContextCardQuery.User(
                                "User",
                                user.id.toString(),
                                user.name,
                                user.shortName,
                                null,
                                null,
                                user.email,
                                mutableListOf(
                                    StudentContextCardQuery.Enrollment(
                                        "Enrollment",
                                        null,
                                        if (isStudent) EnrollmentType.STUDENTENROLLMENT else EnrollmentType.TEACHERENROLLMENT,
                                        StudentContextCardQuery.Section("Section", course.name),
                                        StudentContextCardQuery.Grades("Grades", null, null, "F", 45.5, "F", 3.25, "F", 45.5, "F", 3.25)
                                    )
                                ),
                                null
                            )
                        )
                    )
                ),
                StudentContextCardQuery.Submissions(
                    "SubmissionConnection",
                    StudentContextCardQuery.PageInfo("PageInfo", "Mg", "MQ", false, false),
                    if (isStudent) getSubmissions(userId) else emptyList()
                )
            )
        )

        val nextCursorInput: Input<String> = Input.fromNullable(nextCursor)

        return Response.builder<StudentContextCardQuery.Data>(
            StudentContextCardQuery(
                course.id.toString(),
                user.id.toString(),
                10,
                nextCursorInput
            )
        )
            .data(mockData)
            .build()
    }

    private fun getSubmissions(studentId: Long): List<StudentContextCardQuery.Edge1> {
        val assignments = MockCanvas.data.assignments
        return assignments.values.mapNotNull {
            val submission = MockCanvas.data.submissions[it.id]?.firstOrNull()
            if (submission != null) {
                StudentContextCardQuery.Edge1(
                    "SubmissionEdge",
                    StudentContextCardQuery.Submission(
                        "Submission",
                        submission.id.toString(),
                        submission.score,
                        submission.grade,
                        submission.excused,
                        "unsubmitted",
                        SubmissionGradingStatus.GRADED,
                        true,
                        submission.postedAt,
                        StudentContextCardQuery.User1("User", studentId.toString()),
                        StudentContextCardQuery.Assignment(
                            "Assignment",
                            it.id.toString(),
                            it.name,
                            it.htmlUrl,
                            it.pointsPossible,
                            AssignmentState.PUBLISHED,
                            GradingType.PERCENT,
                            mutableListOf(SubmissionType.ONLINE_TEXT_ENTRY)
                        )
                    )
                )
            } else {
                null
            }
        }
    }
}