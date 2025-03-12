/*
 * Copyright (C) 2024 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */package com.instructure.teacher.unit.features.assignment.submission

import com.instructure.canvasapi2.apis.AssignmentAPI
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.apis.SectionAPI
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.GradeableStudent
import com.instructure.canvasapi2.models.GradeableStudentSubmission
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.GroupAssignee
import com.instructure.canvasapi2.models.Section
import com.instructure.canvasapi2.models.StudentAssignee
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.LinkHeaders
import com.instructure.teacher.features.assignment.submission.AssignmentSubmissionRepository
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.lang.IllegalStateException

class AssignmentSubmissionRepositoryTest {

    private val courseApi: CourseAPI.CoursesInterface = mockk(relaxed = true)
    private val enrollmentApi: EnrollmentAPI.EnrollmentInterface = mockk(relaxed = true)
    private val assignmentApi: AssignmentAPI.AssignmentInterface = mockk(relaxed = true)
    private val sectionApi: SectionAPI.SectionsInterface = mockk(relaxed = true)


    private lateinit var repository: AssignmentSubmissionRepository

    @Before
    fun setup() {
        repository = AssignmentSubmissionRepository(assignmentApi, enrollmentApi, courseApi, sectionApi)

        coEvery {
            sectionApi.getFirstPageSectionsList(any(), any())
        } returns DataResult.Success(listOf(Section(id = 1L, name = "Section 1")))
    }

    @Test
    fun `Create student submissions`() = runTest {
        val users = listOf(
            User(id = 1L),
            User(id = 2L),
            User(id = 3L),
            User(id = 4L)
        )
        val students = listOf(
            GradeableStudent(id = 1L),
            GradeableStudent(id = 2L),
            GradeableStudent(id = 3L),
            GradeableStudent(id = 4L)
        )
        val enrollments = listOf(
            Enrollment(id = 1L, userId = 1L, user = users[0]),
            Enrollment(id = 2L, userId = 2L, user = users[1]),
            Enrollment(id = 3L, userId = 3L, user = users[2]),
            Enrollment(id = 4L, userId = 4L, user = users[3])
        )
        val submissions = listOf(
            Submission(id = 1L, userId = 1L),
            Submission(id = 2L, userId = 2L),
            Submission(id = 3L, userId = 3L),
            Submission(id = 4L, userId = 4L)
        )
        coEvery {
            assignmentApi.getFirstPageGradeableStudentsForAssignment(any(), any(), any())
        } returns DataResult.Success(
            students.subList(0, 2), linkHeaders = LinkHeaders(nextUrl = "nextUrl")
        )

        coEvery {
            assignmentApi.getNextPageGradeableStudents("nextUrl", any())
        } returns DataResult.Success(
            students.subList(2, 4)
        )

        coEvery {
            enrollmentApi.getFirstPageEnrollmentsForCourse(
                any(),
                any(),
                any()
            )
        } returns DataResult.Success(
            enrollments.subList(0, 2), linkHeaders = LinkHeaders(nextUrl = "nextUrl")
        )
        coEvery { enrollmentApi.getNextPage("nextUrl", any()) } returns DataResult.Success(
            enrollments.subList(2, 4)
        )
        coEvery {
            assignmentApi.getFirstPageSubmissionsForAssignment(
                any(),
                any(),
                any()
            )
        } returns DataResult.Success(
            submissions.subList(0, 2), linkHeaders = LinkHeaders(nextUrl = "nextUrl")
        )
        coEvery {
            assignmentApi.getNextPageSubmissions(
                "nextUrl",
                any()
            )
        } returns DataResult.Success(
            submissions.subList(2, 4)
        )

        val expected = listOf(
            GradeableStudentSubmission(
                assignee = StudentAssignee(users[0]),
                submission = submissions[0]
            ),
            GradeableStudentSubmission(
                assignee = StudentAssignee(users[1]),
                submission = submissions[1]
            ),
            GradeableStudentSubmission(
                assignee = StudentAssignee(users[2]),
                submission = submissions[2]
            ),
            GradeableStudentSubmission(
                assignee = StudentAssignee(users[3]),
                submission = submissions[3]
            )
        )
        val result = repository.getGradeableStudentSubmissions(Assignment(1L), 1L, false)

        assertEquals(expected, result)
    }

    @Test
    fun `Create group submissions`() = runTest {
        val users = listOf(
            User(id = 1L),
            User(id = 2L),
            User(id = 3L),
            User(id = 4L)
        )
        val students = listOf(
            GradeableStudent(id = 1L),
            GradeableStudent(id = 2L),
            GradeableStudent(id = 3L),
            GradeableStudent(id = 4L)
        )
        val enrollments = listOf(
            Enrollment(id = 1L, userId = 1L, user = users[0]),
            Enrollment(id = 2L, userId = 2L, user = users[1]),
            Enrollment(id = 3L, userId = 3L, user = users[2]),
            Enrollment(id = 4L, userId = 4L, user = users[3])
        )
        val groups = listOf(
            Group(id = 1L, groupCategoryId = 1L, users = users.subList(0, 2)),
            Group(id = 2L, groupCategoryId = 1L, users = users.subList(2, 3)),
        )
        val submissions = listOf(
            Submission(id = 1L, group = groups[0]),
            Submission(id = 2L, group = groups[1]),
            Submission(id = 3L, userId = 4L)
        )

        coEvery {
            assignmentApi.getFirstPageGradeableStudentsForAssignment(any(), any(), any())
        } returns DataResult.Success(students)
        coEvery {
            enrollmentApi.getFirstPageEnrollmentsForCourse(any(), any(), any())
        } returns DataResult.Success(enrollments)
        coEvery {
            assignmentApi.getFirstPageSubmissionsForAssignment(any(), any(), any())
        } returns DataResult.Success(submissions)
        coEvery {
            courseApi.getFirstPageGroups(any(), any())
        } returns DataResult.Success(groups)

        val expectedGroupSubmissions = listOf(
            GradeableStudentSubmission(
                assignee = GroupAssignee(groups[0], groups[0].users),
                submission = submissions[0]
            ),
            GradeableStudentSubmission(
                assignee = GroupAssignee(groups[1], groups[1].users),
                submission = submissions[1]
            ),
        )
        val expectedIndividualSubmission = listOf(
            GradeableStudentSubmission(
                assignee = StudentAssignee(users[3]),
                submission = submissions[2]
            )
        )

        val result = repository.getGradeableStudentSubmissions(
            Assignment(1L, groupCategoryId = 1L),
            1L,
            false
        )

        assertEquals(expectedGroupSubmissions + expectedIndividualSubmission, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `Throw exception when getting students fails`() = runTest {
        coEvery {
            assignmentApi.getFirstPageGradeableStudentsForAssignment(
                any(),
                any(),
                any()
            )
        } returns DataResult.Fail()

        repository.getGradeableStudentSubmissions(Assignment(1L), 1L, false)
    }

    @Test(expected = IllegalStateException::class)
    fun `Throw exception when getting enrollments fails`() = runTest {
        coEvery {
            assignmentApi.getFirstPageGradeableStudentsForAssignment(
                any(),
                any(),
                any()
            )
        } returns DataResult.Success(emptyList())

        coEvery {
            enrollmentApi.getFirstPageEnrollmentsForCourse(
                any(),
                any(),
                any()
            )
        } returns DataResult.Fail()

        repository.getGradeableStudentSubmissions(Assignment(1L), 1L, false)
    }

    @Test(expected = IllegalStateException::class)
    fun `Throw exception when getting submissions fails`() = runTest {
        coEvery {
            assignmentApi.getFirstPageGradeableStudentsForAssignment(
                any(),
                any(),
                any()
            )
        } returns DataResult.Success(emptyList())

        coEvery {
            enrollmentApi.getFirstPageEnrollmentsForCourse(
                any(),
                any(),
                any()
            )
        } returns DataResult.Success(emptyList())

        coEvery {
            assignmentApi.getFirstPageSubmissionsForAssignment(
                any(),
                any(),
                any()
            )
        } returns DataResult.Fail()

        repository.getGradeableStudentSubmissions(Assignment(1L), 1L, false)
    }

    @Test(expected = IllegalStateException::class)
    fun `Throw exception when getting groups fails`() = runTest {
        coEvery {
            assignmentApi.getFirstPageGradeableStudentsForAssignment(
                any(),
                any(),
                any()
            )
        } returns DataResult.Success(emptyList())

        coEvery {
            enrollmentApi.getFirstPageEnrollmentsForCourse(
                any(),
                any(),
                any()
            )
        } returns DataResult.Success(emptyList())

        coEvery {
            assignmentApi.getFirstPageSubmissionsForAssignment(
                any(),
                any(),
                any()
            )
        } returns DataResult.Success(emptyList())

        coEvery {
            courseApi.getFirstPageGroups(
                any(),
                any()
            )
        } returns DataResult.Fail()

        repository.getGradeableStudentSubmissions(Assignment(1L, groupCategoryId = 1L), 1L, false)
    }
}