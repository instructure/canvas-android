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

package com.instructure.parentapp.features.grades

import com.instructure.canvasapi2.apis.AssignmentAPI
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.CourseGrade
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.GradingPeriod
import com.instructure.canvasapi2.models.GradingPeriodResponse
import com.instructure.canvasapi2.models.ObserveeAssignment
import com.instructure.canvasapi2.models.ObserveeAssignmentGroup
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.LinkHeaders
import com.instructure.parentapp.util.ParentPrefs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test


class ParentGradesRepositoryTest {

    private val assignmentApi: AssignmentAPI.AssignmentInterface = mockk(relaxed = true)
    private val courseApi: CourseAPI.CoursesInterface = mockk(relaxed = true)
    private val parentPrefs: ParentPrefs = mockk(relaxed = true)

    private lateinit var repository: ParentGradesRepository

    @Before
    fun setup() {
        every { parentPrefs.currentStudent } returns User(id = 1)
    }

    @Test
    fun `Get assignment groups successfully returns data`() = runTest {
        val expected = listOf(
            AssignmentGroup(
                id = 1,
                name = "Group 1",
                assignments = listOf(
                    Assignment(
                        id = 11,
                        published = true,
                        submission = Submission(id = 111, userId = 1)
                    )
                )
            ),
            AssignmentGroup(
                id = 2,
                name = "Group 2",
                assignments = listOf(
                    Assignment(
                        id = 21,
                        published = true,
                        submission = Submission(id = 211, userId = 1)
                    )
                )
            )
        )

        coEvery {
            assignmentApi.getFirstPageAssignmentGroupListWithAssignmentsForObserver(
                1, 1, RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = false)
            )
        } returns DataResult.Success(expected.map {
            it.toObserveeAssignmentGroup()
        })

        createRepository()

        val result = repository.loadAssignmentGroups(1, 1, false)

        Assert.assertEquals(expected, result)
    }

    @Test
    fun `Get assignment groups with pagination successfully returns data`() = runTest {
        val page1 = listOf(
            AssignmentGroup(
                id = 1,
                name = "Group 1",
                assignments = listOf(
                    Assignment(
                        id = 11,
                        published = true,
                        submission = Submission(id = 111, userId = 1)
                    )
                )
            )
        )
        val page2 = listOf(
            AssignmentGroup(
                id = 2,
                name = "Group 2",
                assignments = listOf(
                    Assignment(
                        id = 21,
                        published = true,
                        submission = Submission(id = 211, userId = 1)
                    )
                )
            )
        )

        coEvery {
            assignmentApi.getFirstPageAssignmentGroupListWithAssignmentsForObserver(
                1, 1, RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = true)
            )
        } returns DataResult.Success(
            page1.map { it.toObserveeAssignmentGroup() },
            linkHeaders = LinkHeaders(nextUrl = "page_2_url")
        )
        coEvery {
            assignmentApi.getNextPageAssignmentGroupListWithAssignmentsForObserver(
                "page_2_url", RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = true)
            )
        } returns DataResult.Success(page2.map { it.toObserveeAssignmentGroup() })

        createRepository()

        val result = repository.loadAssignmentGroups(1, 1, true)

        Assert.assertEquals(page1 + page2, result)
    }

    @Test
    fun `Get assignment groups filters out unpublished assignments`() = runTest {
        val assignmentGroups = listOf(
            AssignmentGroup(
                id = 1,
                name = "Group 1",
                assignments = listOf(
                    Assignment(
                        id = 11,
                        published = false,
                        submission = Submission(id = 111, userId = 1)
                    ),
                    Assignment(
                        id = 12,
                        published = true,
                        submission = Submission(id = 121, userId = 1)
                    ),
                )
            ),
            AssignmentGroup(
                id = 2,
                name = "Group 2",
                assignments = listOf(
                    Assignment(
                        id = 21,
                        published = false,
                        submission = Submission(id = 211, userId = 1)
                    )
                )
            )
        )

        coEvery {
            assignmentApi.getFirstPageAssignmentGroupListWithAssignmentsForObserver(
                1, 1, RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = false)
            )
        } returns DataResult.Success(assignmentGroups.map {
            it.toObserveeAssignmentGroup()
        })

        createRepository()

        val result = repository.loadAssignmentGroups(1, 1, false)

        val expected = assignmentGroups.map { group -> group.copy(assignments = group.assignments.filter { it.published }) }
        Assert.assertEquals(expected, result)
    }

    @Test
    fun `Get assignment groups maps the submission belonging to the student`() = runTest {
        val assignmentGroups = listOf(
            AssignmentGroup(
                id = 1,
                name = "Group 1",
                assignments = listOf(
                    Assignment(
                        id = 11,
                        published = true,
                        submission = Submission(id = 111, userId = 1)
                    ),
                    Assignment(
                        id = 12,
                        published = true,
                        submission = Submission(id = 121, userId = 2)
                    )
                )
            )
        )

        coEvery {
            assignmentApi.getFirstPageAssignmentGroupListWithAssignmentsForObserver(
                1, 1, RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = false)
            )
        } returns DataResult.Success(assignmentGroups.map {
            it.toObserveeAssignmentGroup()
        })

        createRepository()

        val result = repository.loadAssignmentGroups(1, 1, false)

        val expected = listOf(
            AssignmentGroup(
                id = 1,
                name = "Group 1",
                assignments = listOf(
                    Assignment(
                        id = 11,
                        published = true,
                        submission = Submission(id = 111, userId = 1)
                    ),
                    Assignment(
                        id = 12,
                        published = true,
                        submission = null
                    )
                )
            )
        )
        Assert.assertEquals(expected, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `Get assignment groups throws exception when call fails`() = runTest {
        coEvery {
            assignmentApi.getFirstPageAssignmentGroupListWithAssignmentsForObserver(
                1, 1, RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = true)
            )
        } returns DataResult.Fail()

        createRepository()

        repository.loadAssignmentGroups(1, 1, true)
    }

    @Test
    fun `Get grading periods successfully returns data`() = runTest {
        val expected = listOf(
            GradingPeriod(id = 1, title = "Period 1"),
            GradingPeriod(id = 2, title = "Period 2")
        )

        coEvery {
            courseApi.getGradingPeriodsForCourse(1, RestParams(isForceReadFromNetwork = false))
        } returns DataResult.Success(GradingPeriodResponse(expected))

        createRepository()

        val result = repository.loadGradingPeriods(1, false)

        Assert.assertEquals(expected, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `Get grading periods throws exception when call fails`() = runTest {
        coEvery {
            courseApi.getGradingPeriodsForCourse(1, RestParams(isForceReadFromNetwork = true))
        } returns DataResult.Fail()

        createRepository()

        repository.loadGradingPeriods(1, true)
    }

    @Test
    fun `Get enrollments successfully returns data`() = runTest {
        val expected = listOf(
            Enrollment(id = 1, userId = 1),
            Enrollment(id = 2, userId = 2)
        )

        coEvery {
            courseApi.getObservedUserEnrollmentsForGradingPeriod(
                1, 1, 1,
                RestParams(isForceReadFromNetwork = false)
            )
        } returns DataResult.Success(expected)

        createRepository()

        val result = repository.loadEnrollments(1, 1, false)

        Assert.assertEquals(expected, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `Get enrollments throws exception when call fails`() = runTest {
        coEvery {
            courseApi.getObservedUserEnrollmentsForGradingPeriod(
                1, 1, 1,
                RestParams(isForceReadFromNetwork = true)
            )
        } returns DataResult.Fail()

        createRepository()

        repository.loadEnrollments(1, 1, true)
    }

    @Test
    fun `Get course successfully returns data`() = runTest {
        val expected = Course(id = 1)

        coEvery {
            courseApi.getCourseWithGrade(1, RestParams(isForceReadFromNetwork = false))
        } returns DataResult.Success(expected)

        createRepository()

        val result = repository.loadCourse(1, false)

        Assert.assertEquals(expected, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `Get course throws exception when call fails`() = runTest {
        coEvery {
            courseApi.getCourseWithGrade(1, RestParams(isForceReadFromNetwork = true))
        } returns DataResult.Fail()

        createRepository()

        repository.loadCourse(1, true)
    }

    @Test
    fun `Course grade calculated correctly`() = runTest {
        val expected = CourseGrade(
            currentGrade = "A",
            currentScore = 100.0,
            finalGrade = "B",
            finalScore = 80.0,
            isLocked = false,
            noCurrentGrade = false,
            noFinalGrade = false
        )

        createRepository()

        val course = Course(id = 1)
        val enrollments = listOf(
            Enrollment(
                id = 1,
                userId = 1,
                computedCurrentGrade = "A",
                computedCurrentScore = 100.0,
                computedFinalGrade = "B",
                computedFinalScore = 80.0
            )
        )

        val result = repository.getCourseGrade(course, 1, enrollments, null)
        Assert.assertEquals(expected, result)
    }

    private fun AssignmentGroup.toObserveeAssignmentGroup() = ObserveeAssignmentGroup(
        id = id,
        name = name,
        assignments = assignments.map { assignment ->
            ObserveeAssignment(
                id = assignment.id,
                published = assignment.published,
                submissionList = assignment.submission?.let {
                    listOf(it)
                }.orEmpty()
            )
        }
    )

    private fun createRepository() {
        repository = ParentGradesRepository(assignmentApi, courseApi, parentPrefs)
    }
}
