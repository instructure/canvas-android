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

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.models.Quiz
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.features.assignments.details.StudentAssignmentDetailsRepository
import com.instructure.student.features.assignments.details.datasource.AssignmentDetailsLocalDataSource
import com.instructure.student.features.assignments.details.datasource.AssignmentDetailsNetworkDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class AssignmentDetailsRepositoryTest {

    private val networkDataSource: AssignmentDetailsNetworkDataSource = mockk(relaxed = true)
    private val localDataSource: AssignmentDetailsLocalDataSource = mockk(relaxed = true)
    private val networkStateProvider: NetworkStateProvider = mockk(relaxed = true)
    private val featureFlagProvider: FeatureFlagProvider = mockk(relaxed = true)

    private val repository = StudentAssignmentDetailsRepository(localDataSource, networkDataSource, networkStateProvider, featureFlagProvider)

    @Before
    fun setup() = runTest {
        coEvery { featureFlagProvider.offlineEnabled() } returns true
    }

    @Test
    fun `Get course if device is online`() = runTest {
        val expected = Course(1)
        every { networkStateProvider.isOnline() } returns true
        coEvery { networkDataSource.getCourseWithGrade(any(), any()) } returns expected

        val course = repository.getCourseWithGrade(1, true)

        coVerify { networkDataSource.getCourseWithGrade(any(), any()) }
        Assert.assertEquals(expected, course)
    }

    @Test
    fun `Get course if device is offline`() = runTest {
        val expected = Course(1)
        every { networkStateProvider.isOnline() } returns false
        coEvery { localDataSource.getCourseWithGrade(any(), any()) } returns expected

        val course = repository.getCourseWithGrade(1, true)

        coVerify { localDataSource.getCourseWithGrade(any(), any()) }
        Assert.assertEquals(expected, course)
    }

    @Test
    fun `Get assignment as observer if device is online`() = runTest {
        val expected = Assignment(1)
        every { networkStateProvider.isOnline() } returns true
        coEvery { networkDataSource.getAssignment(true, any(), any(), any()) } returns expected

        val assignment = repository.getAssignment(true, 1, 1, true)

        coVerify { networkDataSource.getAssignment(true, any(), any(), any()) }
        Assert.assertEquals(expected, assignment)
    }

    @Test
    fun `Get assignment as student if device is online`() = runTest {
        val expected = Assignment(1)
        every { networkStateProvider.isOnline() } returns true
        coEvery { networkDataSource.getAssignment(false, any(), any(), any()) } returns expected

        val assignment = repository.getAssignment(false, 1, 1, true)

        coVerify { networkDataSource.getAssignment(false, any(), any(), any()) }
        Assert.assertEquals(expected, assignment)
    }

    @Test
    fun `Get assignment if device is offline`() = runTest {
        val expected = Assignment(1)
        every { networkStateProvider.isOnline() } returns false
        coEvery { localDataSource.getAssignment(any(), any(), any(), any()) } returns expected

        val assignment = repository.getAssignment(false, 1, 1, true)

        coVerify { localDataSource.getAssignment(any(), any(), any(), any()) }
        Assert.assertEquals(expected, assignment)
    }

    @Test
    fun `Get quiz if device is online`() = runTest {
        val expected = Quiz(1)
        every { networkStateProvider.isOnline() } returns true
        coEvery { networkDataSource.getQuiz(any(), any(), any()) } returns expected

        val quiz = repository.getQuiz(1, 1, true)

        coVerify { networkDataSource.getQuiz(any(), any(), any()) }
        Assert.assertEquals(expected, quiz)
    }

    @Test
    fun `Get quiz if device is offline`() = runTest {
        val expected = Quiz(1)
        every { networkStateProvider.isOnline() } returns false
        coEvery { localDataSource.getQuiz(any(), any(), any()) } returns expected

        val assignment = repository.getQuiz(1, 1, true)

        coVerify { localDataSource.getQuiz(any(), any(), any()) }
        Assert.assertEquals(expected, assignment)
    }

    @Test
    fun `Get external tool by launch url if device is online`() = runTest {
        val expected = LTITool(1)
        every { networkStateProvider.isOnline() } returns true
        coEvery { networkDataSource.getExternalToolLaunchUrl(any(), any(), any(), any()) } returns expected

        val ltiTool = repository.getExternalToolLaunchUrl(1, 1, 1, true)

        coVerify { networkDataSource.getExternalToolLaunchUrl(any(), any(), any(), any()) }
        Assert.assertEquals(expected, ltiTool)
    }

    @Test
    fun `Get external tool by launch url if device is offline`() = runTest {
        every { networkStateProvider.isOnline() } returns false
        coEvery { localDataSource.getExternalToolLaunchUrl(any(), any(), any(), any()) } returns null

        val ltiTool = repository.getExternalToolLaunchUrl(1, 1, 1, true)

        Assert.assertEquals(null, ltiTool)
    }

    @Test
    fun `Get external tool by authentication url if device is online`() = runTest {
        val expected = LTITool(1)
        every { networkStateProvider.isOnline() } returns true
        coEvery { networkDataSource.getLtiFromAuthenticationUrl(any(), any()) } returns expected

        val ltiTool = repository.getLtiFromAuthenticationUrl("", true)

        coVerify { networkDataSource.getLtiFromAuthenticationUrl(any(), any()) }
        Assert.assertEquals(expected, ltiTool)
    }

    @Test
    fun `Get external tool by authentication url if device is offline`() = runTest {
        every { networkStateProvider.isOnline() } returns false
        coEvery { localDataSource.getLtiFromAuthenticationUrl(any(), any()) } returns null

        val ltiTool = repository.getLtiFromAuthenticationUrl("", true)

        Assert.assertEquals(null, ltiTool)
    }
}