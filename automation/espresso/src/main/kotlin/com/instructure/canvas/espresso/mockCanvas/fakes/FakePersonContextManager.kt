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

class FakePersonContextManager : StudentContextManager {

    override fun getStudentContext(
        courseId: Long,
        studentId: Long,
        submissionPageSize: Int,
        forceNetwork: Boolean,
        callback: QLCallback<StudentContextCardQuery.Data>
    ) {
        return getStudentContextResponse(callback)
    }

    private fun getStudentContextResponse(callback: QLCallback<StudentContextCardQuery.Data>) {

        val course = MockCanvas.data.courses.values.first()
        val student = MockCanvas.data.students[0]
        val assignment = MockCanvas.data.assignments[0]
        val submission = MockCanvas.data.submissions.values.first()[0]

        val mockData = StudentContextCardQuery.Data(
            AsCourse("Course", course.id.toString(), course.name,
                StudentContextCardQuery.Permissions("CoursePermission", false, true, true, true, true),
                StudentContextCardQuery.Users("UserConnection", mutableListOf( StudentContextCardQuery.Edge("UserEdge", StudentContextCardQuery.User("User", student.id.toString(), student.name, student.shortName, null, null, student.email, mutableListOf(StudentContextCardQuery.Enrollment("Enrollment", null, EnrollmentType.STUDENTENROLLMENT, StudentContextCardQuery.Section("Section", course.name), StudentContextCardQuery.Grades("Grades", null, null, "F", 45.5, "F", 3.25, "F", 45.5, "F", 3.25))), null)))),
                StudentContextCardQuery.Submissions("SubmissionConnection",
                    StudentContextCardQuery.PageInfo("PageInfo", "Mg", "MQ",false, false),
                    mutableListOf(StudentContextCardQuery.Edge1("SubmissionEdge",
                        StudentContextCardQuery.Submission("Submission", submission.id.toString(), submission.score, submission.grade, submission.excused, "unsubmitted", SubmissionGradingStatus.GRADED, true, submission.postedAt,
                        StudentContextCardQuery.User1("User", student.id.toString()),
                        StudentContextCardQuery.Assignment("Assignment", assignment?.id.toString(), assignment?.name, assignment?.htmlUrl, assignment?.pointsPossible, AssignmentState.PUBLISHED, GradingType.PERCENT, mutableListOf(SubmissionType.ONLINE_TEXT_ENTRY)))))))
        )

        val nextCursorInput: Input<String> = Input.fromNullable(callback.nextCursor)

        val mockResponse = Response.builder<StudentContextCardQuery.Data>(StudentContextCardQuery(course.id.toString(),student.id.toString(), 10, nextCursorInput))
            .data(mockData)
            .build()

        callback.onResponse(mockResponse)

    }
}