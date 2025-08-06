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
 *
 */

package com.instructure.parentapp.features.assignments.details

import com.instructure.canvasapi2.apis.AssignmentAPI
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.FeaturesAPI
import com.instructure.canvasapi2.apis.QuizAPI
import com.instructure.canvasapi2.apis.SubmissionAPI
import com.instructure.canvasapi2.managers.graphql.CustomGradeStatusesManager
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.models.ObserveeAssignment
import com.instructure.canvasapi2.models.Quiz
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.parentapp.features.assignment.details.ParentAssignmentDetailsRepository
import com.instructure.parentapp.util.ParentPrefs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test


class ParentAssignmentDetailsRepositoryTest {

    private val coursesInterface: CourseAPI.CoursesInterface = mockk(relaxed = true)
    private val assignmentInterface: AssignmentAPI.AssignmentInterface = mockk(relaxed = true)
    private val quizInterface: QuizAPI.QuizInterface = mockk(relaxed = true)
    private val submissionInterface: SubmissionAPI.SubmissionInterface = mockk(relaxed = true)
    private val featuresApi: FeaturesAPI.FeaturesInterface = mockk(relaxed = true)
    private val parentPrefs: ParentPrefs = mockk(relaxed = true)
    private val customGradeStatusesManager: CustomGradeStatusesManager = mockk(relaxed = true)

    private val repository = ParentAssignmentDetailsRepository(
        coursesInterface,
        assignmentInterface,
        quizInterface,
        submissionInterface,
        featuresApi,
        parentPrefs,
        customGradeStatusesManager
    )

    @Test
    fun `Get course successfully returns data`() = runTest {
        val expected = Course(1)
        coEvery { coursesInterface.getCourseWithGrade(any(), any()) } returns DataResult.Success(expected)

        val courseResult = repository.getCourseWithGrade(1, true)

        Assert.assertEquals(expected, courseResult)
    }

    @Test(expected = IllegalStateException::class)
    fun `Get course failure throws exception`() = runTest {
        coEvery { coursesInterface.getCourseWithGrade(any(), any()) } returns DataResult.Fail()

        repository.getCourseWithGrade(1, true)
    }

    @Test
    fun `Get assignment successfully returns data`() = runTest {
        val observeeAssignment = ObserveeAssignment(1, submissionList = listOf(Submission(id = 1, userId = 1)))
        val expected = observeeAssignment.toAssignmentForObservee()

        every { parentPrefs.currentStudent } returns User(id = 1)
        coEvery { assignmentInterface.getAssignmentIncludeObservees(any(), any(), any()) } returns DataResult.Success(observeeAssignment)

        val assignmentResult = repository.getAssignment(true, 1, 1, true)

        Assert.assertEquals(expected, assignmentResult)
    }

    @Test(expected = IllegalStateException::class)
    fun `Get assignment failure throws exception`() = runTest {
        coEvery { assignmentInterface.getAssignmentIncludeObservees(any(), any(), any()) } returns DataResult.Fail()

        repository.getAssignment(false, 1, 1, true)
    }

    @Test
    fun `Get quiz successfully returns data`() = runTest {
        val expected = Quiz()
        coEvery { quizInterface.getQuiz(any(), any(), any()) } returns DataResult.Success(expected)

        val assignmentResult = repository.getQuiz(1, 1, true)

        Assert.assertEquals(expected, assignmentResult)
    }

    @Test(expected = IllegalStateException::class)
    fun `Get quiz failure throws exception`() = runTest {
        coEvery { quizInterface.getQuiz(any(), any(), any()) } returns DataResult.Fail()

        repository.getQuiz(1, 1, true)
    }

    @Test
    fun `Get LTI by launch url successfully returns data`() = runTest {
        val expected = LTITool()
        coEvery { assignmentInterface.getExternalToolLaunchUrl(any(), any(), any(), any(), any()) } returns DataResult.Success(expected)

        val assignmentResult = repository.getExternalToolLaunchUrl(1, 1, 1, true)

        Assert.assertEquals(expected, assignmentResult)
    }

    @Test(expected = IllegalStateException::class)
    fun `Get LTI by launch url failure throws exception`() = runTest {
        coEvery { assignmentInterface.getExternalToolLaunchUrl(any(), any(), any(), any(), any()) } returns DataResult.Fail()

        repository.getExternalToolLaunchUrl(1, 1, 1, true)
    }

    @Test
    fun `Get LTI by auth url successfully returns data`() = runTest {
        val expected = LTITool()
        coEvery { submissionInterface.getLtiFromAuthenticationUrl(any(), any()) } returns DataResult.Success(expected)

        val assignmentResult = repository.getLtiFromAuthenticationUrl("", true)

        Assert.assertEquals(expected, assignmentResult)
    }

    @Test(expected = IllegalStateException::class)
    fun `Get LTI by auth url failure throws exception`() = runTest {
        coEvery { submissionInterface.getLtiFromAuthenticationUrl(any(), any()) } returns DataResult.Fail()

        repository.getLtiFromAuthenticationUrl("", true)
    }

    @Test
    fun `Assignments enhancements enabled`() = runTest {
        coEvery { featuresApi.getEnabledFeaturesForCourse(any(), any()) } returns DataResult.Success(listOf("assignments_2_student"))

        val result = repository.isAssignmentEnhancementEnabled(1, true)

        Assert.assertTrue(result)
    }

    @Test
    fun `Assignments enhancements disabled`() = runTest {
        coEvery { featuresApi.getEnabledFeaturesForCourse(any(), any()) } returns DataResult.Success(emptyList())

        val result = repository.isAssignmentEnhancementEnabled(1, true)

        Assert.assertFalse(result)
    }

    @Test
    fun `Assignments enhancements disabled when features call fails`() = runTest {
        coEvery { featuresApi.getEnabledFeaturesForCourse(any(), any()) } returns DataResult.Fail()

        val result = repository.isAssignmentEnhancementEnabled(1, true)

        Assert.assertFalse(result)
    }
}
