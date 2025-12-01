/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.pandautils.data.repository.enrollment

import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class EnrollmentRepositoryTest {

    private val enrollmentApi: EnrollmentAPI.EnrollmentInterface = mockk(relaxed = true)
    private lateinit var repository: EnrollmentRepository

    @Before
    fun setup() {
        repository = EnrollmentRepositoryImpl(enrollmentApi)
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `getSelfEnrollments returns success with enrollments`() = runTest {
        val enrollments = listOf(
            Enrollment(id = 1L, courseId = 100L, userId = 10L),
            Enrollment(id = 2L, courseId = 200L, userId = 10L)
        )
        val expected = DataResult.Success(enrollments)
        coEvery {
            enrollmentApi.getFirstPageSelfEnrollments(any(), any(), any())
        } returns expected

        val result = repository.getSelfEnrollments(
            types = null,
            states = listOf("invited"),
            forceRefresh = false
        )

        assertEquals(expected, result)
        coVerify {
            enrollmentApi.getFirstPageSelfEnrollments(
                null,
                listOf("invited"),
                match { !it.isForceReadFromNetwork }
            )
        }
    }

    @Test
    fun `getSelfEnrollments with forceRefresh passes correct params`() = runTest {
        val expected = DataResult.Success(emptyList<Enrollment>())
        coEvery {
            enrollmentApi.getFirstPageSelfEnrollments(any(), any(), any())
        } returns expected

        repository.getSelfEnrollments(
            types = listOf("StudentEnrollment"),
            states = listOf("active"),
            forceRefresh = true
        )

        coVerify {
            enrollmentApi.getFirstPageSelfEnrollments(
                listOf("StudentEnrollment"),
                listOf("active"),
                match { it.isForceReadFromNetwork }
            )
        }
    }

    @Test
    fun `getSelfEnrollments returns failure`() = runTest {
        val expected = DataResult.Fail()
        coEvery {
            enrollmentApi.getFirstPageSelfEnrollments(any(), any(), any())
        } returns expected

        val result = repository.getSelfEnrollments(
            types = null,
            states = null,
            forceRefresh = false
        )

        assertEquals(expected, result)
    }

    @Test
    fun `handleInvitation accept calls API with accept action`() = runTest {
        val expected = DataResult.Success(Unit)
        coEvery {
            enrollmentApi.handleInvite(any(), any(), any(), any())
        } returns expected

        val result = repository.handleInvitation(
            courseId = 100L,
            enrollmentId = 1L,
            accept = true
        )

        assertEquals(expected, result)
        coVerify {
            enrollmentApi.handleInvite(
                100L,
                1L,
                "accept",
                any()
            )
        }
    }

    @Test
    fun `handleInvitation reject calls API with reject action`() = runTest {
        val expected = DataResult.Success(Unit)
        coEvery {
            enrollmentApi.handleInvite(any(), any(), any(), any())
        } returns expected

        val result = repository.handleInvitation(
            courseId = 200L,
            enrollmentId = 2L,
            accept = false
        )

        assertEquals(expected, result)
        coVerify {
            enrollmentApi.handleInvite(
                200L,
                2L,
                "reject",
                any()
            )
        }
    }

    @Test
    fun `handleInvitation returns failure`() = runTest {
        val expected = DataResult.Fail()
        coEvery {
            enrollmentApi.handleInvite(any(), any(), any(), any())
        } returns expected

        val result = repository.handleInvitation(
            courseId = 100L,
            enrollmentId = 1L,
            accept = true
        )

        assertEquals(expected, result)
    }
}