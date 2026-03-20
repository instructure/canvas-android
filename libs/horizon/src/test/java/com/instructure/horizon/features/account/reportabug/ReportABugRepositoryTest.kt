/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.features.account.reportabug

import com.instructure.canvasapi2.apis.ErrorReportAPI
import com.instructure.canvasapi2.models.ErrorReportResult
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class ReportABugRepositoryTest {
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val errorReportAPI: ErrorReportAPI.ErrorReportInterface = mockk(relaxed = true)
    private lateinit var repository: ReportABugRepository

    private val testUser = User(
        id = 1L,
        name = "Test User",
        email = "test@example.com"
    )

    private val testErrorReportResult = ErrorReportResult(
        id = 12345L,
        logged = true
    )

    @Before
    fun setup() {
        every { apiPrefs.fullDomain } returns "https://test.instructure.com"
        every { apiPrefs.user } returns testUser

        repository = ReportABugRepository(apiPrefs, errorReportAPI)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `submitErrorReport returns ErrorReportResult on success`() = runTest {
        coEvery {
            errorReportAPI.postErrorReport(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns DataResult.Success(testErrorReportResult)

        val result = repository.submitErrorReport(
            subject = "Test Subject",
            description = "Test Description",
            email = "test@example.com",
            severity = "comment"
        )

        assertEquals(testErrorReportResult, result)
    }

    @Test
    fun `submitErrorReport calls API with correct subject`() = runTest {
        coEvery {
            errorReportAPI.postErrorReport(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns DataResult.Success(testErrorReportResult)

        repository.submitErrorReport(
            subject = "Test Subject",
            description = "Test Description",
            email = "test@example.com",
            severity = "comment"
        )

        coVerify {
            errorReportAPI.postErrorReport(
                subject = "Test Subject",
                url = any(),
                email = any(),
                comments = any(),
                userPerceivedSeverity = any(),
                name = any(),
                userRoles = any(),
                becomeUser = any(),
                body = any(),
                params = any()
            )
        }
    }

    @Test
    fun `submitErrorReport calls API with correct description in comments field`() = runTest {
        coEvery {
            errorReportAPI.postErrorReport(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns DataResult.Success(testErrorReportResult)

        repository.submitErrorReport(
            subject = "Test Subject",
            description = "Test Description",
            email = "test@example.com",
            severity = "comment"
        )

        coVerify {
            errorReportAPI.postErrorReport(
                subject = any(),
                url = any(),
                email = any(),
                comments = "Test Description",
                userPerceivedSeverity = any(),
                name = any(),
                userRoles = any(),
                becomeUser = any(),
                body = any(),
                params = any()
            )
        }
    }

    @Test
    fun `submitErrorReport calls API with correct email`() = runTest {
        coEvery {
            errorReportAPI.postErrorReport(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns DataResult.Success(testErrorReportResult)

        repository.submitErrorReport(
            subject = "Test Subject",
            description = "Test Description",
            email = "test@example.com",
            severity = "comment"
        )

        coVerify {
            errorReportAPI.postErrorReport(
                subject = any(),
                url = any(),
                email = "test@example.com",
                comments = any(),
                userPerceivedSeverity = any(),
                name = any(),
                userRoles = any(),
                becomeUser = any(),
                body = any(),
                params = any()
            )
        }
    }

    @Test
    fun `submitErrorReport calls API with correct severity`() = runTest {
        coEvery {
            errorReportAPI.postErrorReport(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns DataResult.Success(testErrorReportResult)

        repository.submitErrorReport(
            subject = "Test Subject",
            description = "Test Description",
            email = "test@example.com",
            severity = "blocking"
        )

        coVerify {
            errorReportAPI.postErrorReport(
                subject = any(),
                url = any(),
                email = any(),
                comments = any(),
                userPerceivedSeverity = "blocking",
                name = any(),
                userRoles = any(),
                becomeUser = any(),
                body = any(),
                params = any()
            )
        }
    }

    @Test
    fun `submitErrorReport uses fullDomain from ApiPrefs`() = runTest {
        every { apiPrefs.fullDomain } returns "https://custom.instructure.com"
        coEvery {
            errorReportAPI.postErrorReport(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns DataResult.Success(testErrorReportResult)

        repository.submitErrorReport(
            subject = "Test Subject",
            description = "Test Description",
            email = "test@example.com",
            severity = "comment"
        )

        coVerify {
            errorReportAPI.postErrorReport(
                subject = any(),
                url = "https://custom.instructure.com",
                email = any(),
                comments = any(),
                userPerceivedSeverity = any(),
                name = any(),
                userRoles = any(),
                becomeUser = any(),
                body = any(),
                params = any()
            )
        }
    }

    @Test
    fun `submitErrorReport uses user name from ApiPrefs`() = runTest {
        val user = testUser.copy(name = "John Doe")
        every { apiPrefs.user } returns user
        coEvery {
            errorReportAPI.postErrorReport(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns DataResult.Success(testErrorReportResult)

        repository.submitErrorReport(
            subject = "Test Subject",
            description = "Test Description",
            email = "test@example.com",
            severity = "comment"
        )

        coVerify {
            errorReportAPI.postErrorReport(
                subject = any(),
                url = any(),
                email = any(),
                comments = any(),
                userPerceivedSeverity = any(),
                name = "John Doe",
                userRoles = any(),
                becomeUser = any(),
                body = any(),
                params = any()
            )
        }
    }

    @Test
    fun `submitErrorReport uses empty string when user name is null`() = runTest {
        every { apiPrefs.user } returns null
        coEvery {
            errorReportAPI.postErrorReport(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns DataResult.Success(testErrorReportResult)

        repository.submitErrorReport(
            subject = "Test Subject",
            description = "Test Description",
            email = "test@example.com",
            severity = "comment"
        )

        coVerify {
            errorReportAPI.postErrorReport(
                subject = any(),
                url = any(),
                email = any(),
                comments = any(),
                userPerceivedSeverity = any(),
                name = "",
                userRoles = any(),
                becomeUser = any(),
                body = any(),
                params = any()
            )
        }
    }

    @Test
    fun `submitErrorReport passes userRoles as student`() = runTest {
        coEvery {
            errorReportAPI.postErrorReport(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns DataResult.Success(testErrorReportResult)

        repository.submitErrorReport(
            subject = "Test Subject",
            description = "Test Description",
            email = "test@example.com",
            severity = "comment"
        )

        coVerify {
            errorReportAPI.postErrorReport(
                subject = any(),
                url = any(),
                email = any(),
                comments = any(),
                userPerceivedSeverity = any(),
                name = any(),
                userRoles = "student",
                becomeUser = any(),
                body = any(),
                params = any()
            )
        }
    }

    @Test(expected = IllegalStateException::class)
    fun `submitErrorReport throws exception when API call fails`() = runTest {
        coEvery {
            errorReportAPI.postErrorReport(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns DataResult.Fail()

        repository.submitErrorReport(
            subject = "Test Subject",
            description = "Test Description",
            email = "test@example.com",
            severity = "comment"
        )
    }
}
