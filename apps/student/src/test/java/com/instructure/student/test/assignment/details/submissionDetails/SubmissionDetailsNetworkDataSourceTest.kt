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

package com.instructure.student.test.assignment.details.submissionDetails

import com.instructure.canvasapi2.apis.*
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.student.mobius.assignmentDetails.submissionDetails.datasource.SubmissionDetailsNetworkDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import junit.framework.TestCase
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class SubmissionDetailsNetworkDataSourceTest {

    private val enrollmentApi: EnrollmentAPI.EnrollmentInterface = mockk(relaxed = true)
    private val submissionApi: SubmissionAPI.SubmissionInterface = mockk(relaxed = true)
    private val assignmentApi: AssignmentAPI.AssignmentInterface = mockk(relaxed = true)
    private val quizApi: QuizAPI.QuizInterface = mockk(relaxed = true)
    private val featuresApi: FeaturesAPI.FeaturesInterface = mockk(relaxed = true)
    private val courseApi: CourseAPI.CoursesInterface = mockk(relaxed = true)
    private lateinit var networkDataSource: SubmissionDetailsNetworkDataSource

    @Before
    fun setup() {
        mockkObject(ApiPrefs)
        every { ApiPrefs.overrideDomains } returns mutableMapOf()
        networkDataSource = SubmissionDetailsNetworkDataSource(enrollmentApi, submissionApi, assignmentApi, quizApi, featuresApi, courseApi)
    }

    @After
    fun teardown() {
        unmockkObject(ApiPrefs)
    }

    @Test
    fun `Return observee enrollment api model list`() = runTest {
        val expected = DataResult.Success(listOf(Enrollment(1), Enrollment(2)))
        coEvery { enrollmentApi.firstPageObserveeEnrollments(any()) } returns expected

        val result = networkDataSource.getObserveeEnrollments(true)

        TestCase.assertEquals(expected, result)
        coVerify(exactly = 1) {
            enrollmentApi.firstPageObserveeEnrollments(RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = true))
        }
    }

    @Test
    fun `Return failed data result if observee enrollments call fails`() = runTest {
        coEvery { enrollmentApi.firstPageObserveeEnrollments(any()) } returns DataResult.Fail()

        val result = networkDataSource.getObserveeEnrollments(true)

        TestCase.assertEquals(DataResult.Fail(), result)
    }

    @Test
    fun `Return submission api model`() = runTest {
        val expected = DataResult.Success(Submission(1))
        coEvery { submissionApi.getSingleSubmission(any(), any(), any(), any()) } returns expected

        val result = networkDataSource.getSingleSubmission(1, 1, 1, true)

        TestCase.assertEquals(expected, result)
        coVerify(exactly = 1) {
            submissionApi.getSingleSubmission(1, 1, 1, RestParams(isForceReadFromNetwork = true))
        }
    }

    @Test
    fun `Return failed data result if submission call fails`() = runTest {
        coEvery { submissionApi.getSingleSubmission(any(), any(), any(), any()) } returns DataResult.Fail()

        val result = networkDataSource.getSingleSubmission(1, 1, 1, true)

        TestCase.assertEquals(DataResult.Fail(), result)
    }

    @Test
    fun `Return assignment api model`() = runTest {
        val expected = DataResult.Success(Assignment(1))
        coEvery { assignmentApi.getAssignment(any(), any(), any()) } returns expected

        val result = networkDataSource.getAssignment(1, 1, true)

        TestCase.assertEquals(expected, result)
        coVerify(exactly = 1) {
            assignmentApi.getAssignment(1, 1, RestParams(isForceReadFromNetwork = true))
        }
    }

    @Test
    fun `Return failed data result if assignment call fails`() = runTest {
        coEvery { assignmentApi.getAssignment(any(), any(), any()) } returns DataResult.Fail()

        val result = networkDataSource.getAssignment(1, 1, true)

        TestCase.assertEquals(DataResult.Fail(), result)
    }

    @Test
    fun `Return external tool launch url api model`() = runTest {
        val expected = DataResult.Success(LTITool(1))
        coEvery { assignmentApi.getExternalToolLaunchUrl(any(), any(), any(), any(), any()) } returns expected

        val result = networkDataSource.getExternalToolLaunchUrl(1, 1, 1, true)

        TestCase.assertEquals(expected, result)
        coVerify(exactly = 1) {
            assignmentApi.getExternalToolLaunchUrl(1, 1, 1, restParams = RestParams(isForceReadFromNetwork = true))
        }
    }

    @Test
    fun `Return failed data result if external tool launch url call fails`() = runTest {
        coEvery { assignmentApi.getExternalToolLaunchUrl(any(), any(), any(), any(), any()) } returns DataResult.Fail()

        val result = networkDataSource.getExternalToolLaunchUrl(1, 1, 1, true)

        TestCase.assertEquals(DataResult.Fail(), result)
    }

    @Test
    fun `Return lti api model from authentication url`() = runTest {
        val expected = DataResult.Success(LTITool(1))
        coEvery { submissionApi.getLtiFromAuthenticationUrl(any(), any()) } returns expected

        val result = networkDataSource.getLtiFromAuthenticationUrl("url", true)

        TestCase.assertEquals(expected, result)
        coVerify(exactly = 1) {
            submissionApi.getLtiFromAuthenticationUrl("url", RestParams(isForceReadFromNetwork = true))
        }
    }

    @Test
    fun `Return failed data result if lti from authentication url call fails`() = runTest {
        coEvery { submissionApi.getLtiFromAuthenticationUrl(any(), any()) } returns DataResult.Fail()

        val result = networkDataSource.getLtiFromAuthenticationUrl("url", true)

        TestCase.assertEquals(DataResult.Fail(), result)
    }

    @Test
    fun `Return quiz api model`() = runTest {
        val expected = DataResult.Success(Quiz(1))
        coEvery { quizApi.getQuiz(any(), any(), any()) } returns expected

        val result = networkDataSource.getQuiz(1, 1, true)

        TestCase.assertEquals(expected, result)
        coVerify(exactly = 1) {
            quizApi.getQuiz(1, 1, RestParams(isForceReadFromNetwork = true))
        }
    }

    @Test
    fun `Return failed data result if quiz call fails`() = runTest {
        coEvery { quizApi.getQuiz(any(), any(), any()) } returns DataResult.Fail()

        val result = networkDataSource.getQuiz(1, 1, true)

        TestCase.assertEquals(DataResult.Fail(), result)
    }

    @Test
    fun `Return course features api model`() = runTest {
        val expected = DataResult.Success(listOf("feature"))
        coEvery { featuresApi.getEnabledFeaturesForCourse(any(), any()) } returns expected

        val result = networkDataSource.getCourseFeatures(1, true)

        TestCase.assertEquals(expected, result)
        coVerify(exactly = 1) {
            featuresApi.getEnabledFeaturesForCourse(1, RestParams(isForceReadFromNetwork = true))
        }
    }

    @Test
    fun `Return failed data result if course features call fails`() = runTest {
        coEvery { featuresApi.getEnabledFeaturesForCourse(any(), any()) } returns DataResult.Fail()

        val result = networkDataSource.getCourseFeatures(1, true)

        TestCase.assertEquals(DataResult.Fail(), result)
    }

    @Test
    fun `Load course settings returns succesful api model`() = runTest {
        val expected = CourseSettings(restrictQuantitativeData = true)

        coEvery { courseApi.getCourseSettings(any(), any()) } returns DataResult.Success(expected)

        val result = networkDataSource.loadCourseSettings(1, true)

        Assert.assertEquals(expected, result)
    }

    @Test
    fun `Load course settings failure returns null`() = runTest {
        coEvery { courseApi.getCourseSettings(any(), any()) } returns DataResult.Fail()

        val result = networkDataSource.loadCourseSettings(1, true)

        Assert.assertNull(result)
    }
}
