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
package com.instructure.pandautils.data.repository.accountnotification

import com.instructure.canvasapi2.apis.AccountNotificationAPI
import com.instructure.canvasapi2.models.AccountNotification
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

class AccountNotificationRepositoryTest {

    private val accountNotificationApi: AccountNotificationAPI.AccountNotificationInterface = mockk(relaxed = true)
    private lateinit var repository: AccountNotificationRepository

    @Before
    fun setup() {
        repository = AccountNotificationRepositoryImpl(accountNotificationApi)
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `getAccountNotifications returns success with notification list`() = runTest {
        val notifications = listOf(
            AccountNotification(id = 1L, subject = "Announcement 1", message = "Message 1", icon = "info", startAt = "1970-01-01T00:00:01Z"),
            AccountNotification(id = 2L, subject = "Announcement 2", message = "Message 2", icon = "warning", startAt = "1970-01-01T00:00:02Z")
        )
        val expected = DataResult.Success(notifications)
        coEvery {
            accountNotificationApi.getAccountNotifications(any(), any(), any())
        } returns expected

        val result = repository.getAccountNotifications(forceRefresh = false)

        assertEquals(expected, result)
        coVerify {
            accountNotificationApi.getAccountNotifications(
                params = match { !it.isForceReadFromNetwork && it.usePerPageQueryParam },
                includePast = false,
                showIsClosed = false
            )
        }
    }

    @Test
    fun `getAccountNotifications with forceRefresh passes correct params`() = runTest {
        val notifications = listOf(
            AccountNotification(id = 1L, subject = "Announcement 1", message = "Message 1", icon = "info", startAt = "1970-01-01T00:00:01Z")
        )
        val expected = DataResult.Success(notifications)
        coEvery {
            accountNotificationApi.getAccountNotifications(any(), any(), any())
        } returns expected

        repository.getAccountNotifications(forceRefresh = true)

        coVerify {
            accountNotificationApi.getAccountNotifications(
                params = match { it.isForceReadFromNetwork && it.usePerPageQueryParam },
                includePast = false,
                showIsClosed = false
            )
        }
    }

    @Test
    fun `getAccountNotifications returns failure`() = runTest {
        val expected = DataResult.Fail()
        coEvery {
            accountNotificationApi.getAccountNotifications(any(), any(), any())
        } returns expected

        val result = repository.getAccountNotifications(forceRefresh = false)

        assertEquals(expected, result)
    }

    @Test
    fun `getAccountNotifications with forceRefresh false uses cache`() = runTest {
        val notifications = listOf(
            AccountNotification(id = 1L, subject = "Cached Announcement", message = "Cached Message", icon = "info", startAt = "1970-01-01T00:00:01Z")
        )
        val expected = DataResult.Success(notifications)
        coEvery {
            accountNotificationApi.getAccountNotifications(any(), any(), any())
        } returns expected

        val result = repository.getAccountNotifications(forceRefresh = false)

        assertEquals(expected, result)
        coVerify(exactly = 1) {
            accountNotificationApi.getAccountNotifications(
                params = match { !it.isForceReadFromNetwork },
                includePast = false,
                showIsClosed = false
            )
        }
    }

    @Test
    fun `getAccountNotifications handles empty list`() = runTest {
        val expected = DataResult.Success(emptyList<AccountNotification>())
        coEvery {
            accountNotificationApi.getAccountNotifications(any(), any(), any())
        } returns expected

        val result = repository.getAccountNotifications(forceRefresh = false)

        assertEquals(expected, result)
        assertEquals(0, (result as DataResult.Success).data.size)
    }

    @Test
    fun `getAccountNotifications handles multiple consecutive calls`() = runTest {
        val notifications1 = listOf(
            AccountNotification(id = 1L, subject = "First Call", message = "Message 1", icon = "info", startAt = "1970-01-01T00:00:01Z")
        )
        val notifications2 = listOf(
            AccountNotification(id = 2L, subject = "Second Call", message = "Message 2", icon = "warning", startAt = "1970-01-01T00:00:02Z")
        )
        coEvery {
            accountNotificationApi.getAccountNotifications(any(), any(), any())
        } returnsMany listOf(DataResult.Success(notifications1), DataResult.Success(notifications2))

        val result1 = repository.getAccountNotifications(forceRefresh = false)
        val result2 = repository.getAccountNotifications(forceRefresh = true)

        assertEquals("First Call", (result1 as DataResult.Success).data[0].subject)
        assertEquals("Second Call", (result2 as DataResult.Success).data[0].subject)
    }

    @Test
    fun `deleteAccountNotification returns success`() = runTest {
        val notification = AccountNotification(id = 1L, subject = "Announcement 1", message = "Message 1", icon = "info", startAt = "1970-01-01T00:00:01Z")
        val expected = DataResult.Success(notification)
        coEvery {
            accountNotificationApi.deleteAccountNotification(any(), any())
        } returns expected

        val result = repository.deleteAccountNotification(accountNotificationId = 1L)

        assertEquals(expected, result)
        coVerify {
            accountNotificationApi.deleteAccountNotification(1L, any())
        }
    }

    @Test
    fun `deleteAccountNotification returns failure`() = runTest {
        val expected = DataResult.Fail()
        coEvery {
            accountNotificationApi.deleteAccountNotification(any(), any())
        } returns expected

        val result = repository.deleteAccountNotification(accountNotificationId = 1L)

        assertEquals(expected, result)
    }

    @Test
    fun `deleteAccountNotification passes correct id`() = runTest {
        val notification = AccountNotification(id = 123L, subject = "Announcement", message = "Message", icon = "info", startAt = "1970-01-01T00:00:01Z")
        val expected = DataResult.Success(notification)
        coEvery {
            accountNotificationApi.deleteAccountNotification(any(), any())
        } returns expected

        val result = repository.deleteAccountNotification(accountNotificationId = 123L)

        assertEquals(expected, result)
        coVerify {
            accountNotificationApi.deleteAccountNotification(123L, any())
        }
    }
}