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

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.models.Quiz
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.mobius.assignmentDetails.submissionDetails.SubmissionDetailsRepository
import com.instructure.student.mobius.assignmentDetails.submissionDetails.datasource.SubmissionDetailsLocalDataSource
import com.instructure.student.mobius.assignmentDetails.submissionDetails.datasource.SubmissionDetailsNetworkDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class SubmissionDetailsRepositoryTest {

    private val localDataSource: SubmissionDetailsLocalDataSource = mockk(relaxed = true)
    private val networkDataSource: SubmissionDetailsNetworkDataSource = mockk(relaxed = true)
    private val networkStateProvider: NetworkStateProvider = mockk(relaxed = true)
    private val featureFlagProvider: FeatureFlagProvider = mockk(relaxed = true)

    private lateinit var repository: SubmissionDetailsRepository

    @Before
    fun setUp() {
        repository = SubmissionDetailsRepository(localDataSource, networkDataSource, networkStateProvider, featureFlagProvider)
        coEvery { featureFlagProvider.offlineEnabled() } returns true
    }

    @Test
    fun `Return observee enrollments if online`() = runTest {
        val expected = DataResult.Success(listOf(Enrollment(1), Enrollment(2)))

        every { networkStateProvider.isOnline() } returns true

        coEvery { networkDataSource.getObserveeEnrollments(any()) } returns expected

        val result = repository.getObserveeEnrollments(true)

        TestCase.assertEquals(expected, result)
        coVerify(exactly = 1) { networkDataSource.getObserveeEnrollments(true) }
        coVerify(exactly = 0) { localDataSource.getObserveeEnrollments(any()) }
    }

    @Test
    fun `Return failed result for observee enrollments if network error`() = runTest {
        every { networkStateProvider.isOnline() } returns true

        coEvery { networkDataSource.getObserveeEnrollments(any()) } returns DataResult.Fail()

        val result = repository.getObserveeEnrollments(false)

        TestCase.assertEquals(DataResult.Fail(), result)
    }

    @Test
    fun `Return observee enrollments if offline`() = runTest {
        val expected = DataResult.Success(listOf(Enrollment(1), Enrollment(2)))

        every { networkStateProvider.isOnline() } returns false

        coEvery { localDataSource.getObserveeEnrollments(any()) } returns expected

        val result = repository.getObserveeEnrollments(true)

        TestCase.assertEquals(expected, result)
        coVerify(exactly = 0) { networkDataSource.getObserveeEnrollments(any()) }
        coVerify(exactly = 1) { localDataSource.getObserveeEnrollments(true) }
    }

    @Test
    fun `Return submission if online`() = runTest {
        val expected = DataResult.Success(Submission(1))

        every { networkStateProvider.isOnline() } returns true

        coEvery { networkDataSource.getSingleSubmission(any(), any(), any(), any()) } returns expected

        val result = repository.getSingleSubmission(1, 1, 1, true)

        TestCase.assertEquals(expected, result)
        coVerify(exactly = 1) { networkDataSource.getSingleSubmission(1, 1, 1, true) }
        coVerify(exactly = 0) { localDataSource.getSingleSubmission(any(), any(), any(), any()) }
    }

    @Test
    fun `Return failed result for submission if network error`() = runTest {
        every { networkStateProvider.isOnline() } returns true

        coEvery { networkDataSource.getSingleSubmission(any(), any(), any(), any()) } returns DataResult.Fail()

        val result = repository.getSingleSubmission(1, 1, 1, false)

        TestCase.assertEquals(DataResult.Fail(), result)
    }

    @Test
    fun `Return submission if offline`() = runTest {
        val expected = DataResult.Success(Submission(1))

        every { networkStateProvider.isOnline() } returns false

        coEvery { localDataSource.getSingleSubmission(any(), any(), any(), any()) } returns expected

        val result = repository.getSingleSubmission(1, 1, 1, true)

        TestCase.assertEquals(expected, result)
        coVerify(exactly = 0) { networkDataSource.getSingleSubmission(any(), any(), any(), any()) }
        coVerify(exactly = 1) { localDataSource.getSingleSubmission(1, 1, 1, true) }
    }

    @Test
    fun `Return assignment if online`() = runTest {
        val expected = DataResult.Success(Assignment(1))

        every { networkStateProvider.isOnline() } returns true

        coEvery { networkDataSource.getAssignment(any(), any(), any()) } returns expected

        val result = repository.getAssignment(1, 1, true)

        TestCase.assertEquals(expected, result)
        coVerify(exactly = 1) { networkDataSource.getAssignment(1, 1, true) }
        coVerify(exactly = 0) { localDataSource.getAssignment(any(), any(), any()) }
    }

    @Test
    fun `Return failed result for assignment if network error`() = runTest {
        every { networkStateProvider.isOnline() } returns true

        coEvery { networkDataSource.getAssignment(any(), any(), any()) } returns DataResult.Fail()

        val result = repository.getAssignment(1, 1, false)

        TestCase.assertEquals(DataResult.Fail(), result)
    }

    @Test
    fun `Return assignment if offline`() = runTest {
        val expected = DataResult.Success(Assignment(1))

        every { networkStateProvider.isOnline() } returns false

        coEvery { localDataSource.getAssignment(any(), any(), any()) } returns expected

        val result = repository.getAssignment(1, 1, true)

        TestCase.assertEquals(expected, result)
        coVerify(exactly = 0) { networkDataSource.getAssignment(any(), any(), any()) }
        coVerify(exactly = 1) { localDataSource.getAssignment(1, 1, true) }
    }

    @Test
    fun `Return external tool launch url if online`() = runTest {
        val expected = DataResult.Success(LTITool(1))

        every { networkStateProvider.isOnline() } returns true

        coEvery { networkDataSource.getExternalToolLaunchUrl(any(), any(), any(), any()) } returns expected

        val result = repository.getExternalToolLaunchUrl(1, 1, 1, true)

        TestCase.assertEquals(expected, result)
        coVerify(exactly = 1) { networkDataSource.getExternalToolLaunchUrl(1, 1, 1, true) }
        coVerify(exactly = 0) { localDataSource.getExternalToolLaunchUrl(any(), any(), any(), any()) }
    }

    @Test
    fun `Return failed result for external tool launch url if network error`() = runTest {
        every { networkStateProvider.isOnline() } returns true

        coEvery { networkDataSource.getExternalToolLaunchUrl(any(), any(), any(), any()) } returns DataResult.Fail()

        val result = repository.getExternalToolLaunchUrl(1, 1, 1, false)

        TestCase.assertEquals(DataResult.Fail(), result)
    }

    @Test
    fun `Return lti from authentication url if online`() = runTest {
        val expected = DataResult.Success(LTITool(1))

        every { networkStateProvider.isOnline() } returns true

        coEvery { networkDataSource.getLtiFromAuthenticationUrl(any(), any()) } returns expected

        val result = repository.getLtiFromAuthenticationUrl("url", true)

        TestCase.assertEquals(expected, result)
        coVerify(exactly = 1) { networkDataSource.getLtiFromAuthenticationUrl("url", true) }
        coVerify(exactly = 0) { localDataSource.getLtiFromAuthenticationUrl(any(), any()) }
    }

    @Test
    fun `Return failed result for lti from authentication url if network error`() = runTest {
        every { networkStateProvider.isOnline() } returns true

        coEvery { networkDataSource.getLtiFromAuthenticationUrl(any(), any()) } returns DataResult.Fail()

        val result = repository.getLtiFromAuthenticationUrl("url", false)

        TestCase.assertEquals(DataResult.Fail(), result)
    }

    @Test
    fun `Return quiz if online`() = runTest {
        val expected = DataResult.Success(Quiz(1))

        every { networkStateProvider.isOnline() } returns true

        coEvery { networkDataSource.getQuiz(any(), any(), any()) } returns expected

        val result = repository.getQuiz(1, 1, true)

        TestCase.assertEquals(expected, result)
        coVerify(exactly = 1) { networkDataSource.getQuiz(1, 1, true) }
        coVerify(exactly = 0) { localDataSource.getQuiz(any(), any(), any()) }
    }

    @Test
    fun `Return failed result for quiz if network error`() = runTest {
        every { networkStateProvider.isOnline() } returns true

        coEvery { networkDataSource.getQuiz(any(), any(), any()) } returns DataResult.Fail()

        val result = repository.getQuiz(1, 1, false)

        TestCase.assertEquals(DataResult.Fail(), result)
    }

    @Test
    fun `Return quiz if offline`() = runTest {
        val expected = DataResult.Success(Quiz(1))

        every { networkStateProvider.isOnline() } returns false

        coEvery { localDataSource.getQuiz(any(), any(), any()) } returns expected

        val result = repository.getQuiz(1, 1, true)

        TestCase.assertEquals(expected, result)
        coVerify(exactly = 0) { networkDataSource.getQuiz(any(), any(), any()) }
        coVerify(exactly = 1) { localDataSource.getQuiz(1, 1, true) }
    }

    @Test
    fun `Return course features if online`() = runTest {
        val expected = DataResult.Success(listOf("feature"))

        every { networkStateProvider.isOnline() } returns true

        coEvery { networkDataSource.getCourseFeatures(any(), any()) } returns expected

        val result = repository.getCourseFeatures(1, true)

        TestCase.assertEquals(expected, result)
        coVerify(exactly = 1) { networkDataSource.getCourseFeatures(1, true) }
        coVerify(exactly = 0) { localDataSource.getCourseFeatures(any(), any()) }
    }

    @Test
    fun `Return failed result for course features if network error`() = runTest {
        every { networkStateProvider.isOnline() } returns true

        coEvery { networkDataSource.getCourseFeatures(any(), any()) } returns DataResult.Fail()

        val result = repository.getCourseFeatures(1, false)

        TestCase.assertEquals(DataResult.Fail(), result)
    }

    @Test
    fun `Return course features if offline`() = runTest {
        val expected = DataResult.Success(listOf("feature"))

        every { networkStateProvider.isOnline() } returns false

        coEvery { localDataSource.getCourseFeatures(any(), any()) } returns expected

        val result = repository.getCourseFeatures(1, true)

        TestCase.assertEquals(expected, result)
        coVerify(exactly = 0) { networkDataSource.getCourseFeatures(any(), any()) }
        coVerify(exactly = 1) { localDataSource.getCourseFeatures(1, true) }
    }

    @Test
    fun `Load curse settings from local storage when device is offline`() = runTest {
        coEvery { networkDataSource.loadCourseSettings(any(), any()) } returns CourseSettings(restrictQuantitativeData = false)
        coEvery { localDataSource.loadCourseSettings(any(), any()) } returns CourseSettings(restrictQuantitativeData = true)
        coEvery { networkStateProvider.isOnline() } returns false

        val result = repository.loadCourseSettings(1, true)

        Assert.assertTrue(result!!.restrictQuantitativeData)
    }

    @Test
    fun `Load curse settings from network when device is online`() = runTest {
        coEvery { networkDataSource.loadCourseSettings(any(), any()) } returns CourseSettings(restrictQuantitativeData = false)
        coEvery { localDataSource.loadCourseSettings(any(), any()) } returns CourseSettings(restrictQuantitativeData = true)
        coEvery { networkStateProvider.isOnline() } returns true

        val result = repository.loadCourseSettings(1, true)

        Assert.assertFalse(result!!.restrictQuantitativeData)
    }
}
