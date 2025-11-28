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
package com.instructure.pandautils.domain.usecase.accountnotification

import com.instructure.canvasapi2.models.Account
import com.instructure.canvasapi2.models.AccountNotification
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.data.repository.accountnotification.AccountNotificationRepository
import com.instructure.pandautils.data.repository.user.UserRepository
import com.instructure.pandautils.utils.ThemePrefs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Date

class LoadInstitutionalAnnouncementsUseCaseTest {

    private val accountNotificationRepository: AccountNotificationRepository = mockk(relaxed = true)
    private val userRepository: UserRepository = mockk(relaxed = true)
    private lateinit var useCase: LoadInstitutionalAnnouncementsUseCase

    @Before
    fun setup() {
        mockkObject(ThemePrefs)
        every { ThemePrefs.brandColor } returns 0xFF0000FF.toInt()
        every { ThemePrefs.mobileLogoUrl } returns "https://example.com/logo.png"

        useCase = LoadInstitutionalAnnouncementsUseCase(
            accountNotificationRepository,
            userRepository,
            ThemePrefs
        )
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `execute returns sorted announcements limited to 5`() = runTest {
        val date1 = Date(1000L)
        val date2 = Date(2000L)
        val date3 = Date(3000L)
        val date4 = Date(4000L)
        val date5 = Date(5000L)
        val date6 = Date(6000L)

        val notifications = listOf(
            AccountNotification(id = 1L, subject = "Announcement 1", message = "Message 1", icon = "info", startAt = "1970-01-01T00:00:01Z"),
            AccountNotification(id = 2L, subject = "Announcement 2", message = "Message 2", icon = "warning", startAt = "1970-01-01T00:00:03Z"),
            AccountNotification(id = 3L, subject = "Announcement 3", message = "Message 3", icon = "calendar", startAt = "1970-01-01T00:00:06Z"),
            AccountNotification(id = 4L, subject = "Announcement 4", message = "Message 4", icon = "question", startAt = "1970-01-01T00:00:02Z"),
            AccountNotification(id = 5L, subject = "Announcement 5", message = "Message 5", icon = "error", startAt = "1970-01-01T00:00:05Z"),
            AccountNotification(id = 6L, subject = "Announcement 6", message = "Message 6", icon = "info", startAt = "1970-01-01T00:00:04Z")
        )

        coEvery {
            accountNotificationRepository.getAccountNotifications(false)
        } returns DataResult.Success(notifications)

        coEvery {
            userRepository.getAccount(false)
        } returns DataResult.Success(Account(id = 1L, name = "Test Institution"))

        val result = useCase(LoadInstitutionalAnnouncementsParams(forceRefresh = false))

        assertEquals(5, result.size)
        assertEquals(3L, result[0].id)
        assertEquals("Announcement 3", result[0].subject)
        assertEquals(5L, result[1].id)
        assertEquals(6L, result[2].id)
        assertEquals(2L, result[3].id)
        assertEquals(4L, result[4].id)
    }

    @Test
    fun `execute with forceRefresh true passes correct params`() = runTest {
        val notifications = listOf(
            AccountNotification(id = 1L, subject = "Announcement 1", message = "Message 1", icon = "info", startAt = "1970-01-01T00:00:01Z")
        )

        coEvery {
            accountNotificationRepository.getAccountNotifications(true)
        } returns DataResult.Success(notifications)

        coEvery {
            userRepository.getAccount(true)
        } returns DataResult.Success(Account(id = 1L, name = "Test Institution"))

        val result = useCase(LoadInstitutionalAnnouncementsParams(forceRefresh = true))

        assertEquals(1, result.size)
    }

    @Test
    fun `execute returns empty list when no notifications`() = runTest {
        coEvery {
            accountNotificationRepository.getAccountNotifications(false)
        } returns DataResult.Success(emptyList())

        coEvery {
            userRepository.getAccount(false)
        } returns DataResult.Success(Account(id = 1L, name = "Test Institution"))

        val result = useCase(LoadInstitutionalAnnouncementsParams(forceRefresh = false))

        assertEquals(0, result.size)
    }

    @Test
    fun `execute maps notification fields correctly`() = runTest {
        val notifications = listOf(
            AccountNotification(
                id = 123L,
                subject = "Test Subject",
                message = "Test Message",
                icon = "warning",
                startAt = "1970-01-01T00:00:01Z"
            )
        )

        coEvery {
            accountNotificationRepository.getAccountNotifications(false)
        } returns DataResult.Success(notifications)

        coEvery {
            userRepository.getAccount(false)
        } returns DataResult.Success(Account(id = 1L, name = "Canvas University"))

        val result = useCase(LoadInstitutionalAnnouncementsParams(forceRefresh = false))

        assertEquals(1, result.size)
        assertEquals(123L, result[0].id)
        assertEquals("Test Subject", result[0].subject)
        assertEquals("Test Message", result[0].message)
        assertEquals("Canvas University", result[0].institutionName)
        assertEquals("warning", result[0].icon)
        assertEquals("https://example.com/logo.png", result[0].logoUrl)
    }

    @Test(expected = IllegalStateException::class)
    fun `execute throws exception when repository fails`() = runTest {
        coEvery {
            accountNotificationRepository.getAccountNotifications(any())
        } returns DataResult.Fail()

        useCase(LoadInstitutionalAnnouncementsParams(forceRefresh = false))
    }

    @Test
    fun `execute handles notifications with null startDate`() = runTest {
        val notifications = listOf(
            AccountNotification(id = 1L, subject = "No Date", message = "Message 1", icon = "info", startAt = ""),
            AccountNotification(id = 2L, subject = "With Date", message = "Message 2", icon = "info", startAt = "1970-01-01T00:00:02Z")
        )

        coEvery {
            accountNotificationRepository.getAccountNotifications(false)
        } returns DataResult.Success(notifications)

        coEvery {
            userRepository.getAccount(false)
        } returns DataResult.Success(Account(id = 1L, name = "Test Institution"))

        val result = useCase(LoadInstitutionalAnnouncementsParams(forceRefresh = false))

        assertEquals(2, result.size)
        assertEquals(2L, result[0].id)
        assertEquals(1L, result[1].id)
        assertEquals(null, result[1].startDate)
    }

    @Test
    fun `execute returns only first 5 items when more than 5 notifications`() = runTest {
        val notifications = (1..10).map {
            AccountNotification(
                id = it.toLong(),
                subject = "Announcement $it",
                message = "Message $it",
                icon = "info",
                startAt = "1970-01-01T00:00:${it.toString().padStart(2, '0')}Z"
            )
        }

        coEvery {
            accountNotificationRepository.getAccountNotifications(false)
        } returns DataResult.Success(notifications)

        coEvery {
            userRepository.getAccount(false)
        } returns DataResult.Success(Account(id = 1L, name = "Test Institution"))

        val result = useCase(LoadInstitutionalAnnouncementsParams(forceRefresh = false))

        assertEquals(5, result.size)
        assertEquals(10L, result[0].id)
        assertEquals(9L, result[1].id)
        assertEquals(8L, result[2].id)
        assertEquals(7L, result[3].id)
        assertEquals(6L, result[4].id)
    }

    @Test
    fun `execute handles failed user repository call gracefully`() = runTest {
        val notifications = listOf(
            AccountNotification(id = 1L, subject = "Announcement 1", message = "Message 1", icon = "info", startAt = "1970-01-01T00:00:01Z")
        )

        coEvery {
            accountNotificationRepository.getAccountNotifications(false)
        } returns DataResult.Success(notifications)

        coEvery {
            userRepository.getAccount(false)
        } returns DataResult.Fail()

        val result = useCase(LoadInstitutionalAnnouncementsParams(forceRefresh = false))

        assertEquals(1, result.size)
        assertEquals("", result[0].institutionName)
    }

    @Test
    fun `execute uses empty logo URL when ThemePrefs mobileLogoUrl is empty`() = runTest {
        val notifications = listOf(
            AccountNotification(id = 1L, subject = "Announcement 1", message = "Message 1", icon = "info", startAt = "1970-01-01T00:00:01Z")
        )

        every { ThemePrefs.mobileLogoUrl } returns ""

        coEvery {
            accountNotificationRepository.getAccountNotifications(false)
        } returns DataResult.Success(notifications)

        coEvery {
            userRepository.getAccount(false)
        } returns DataResult.Success(Account(id = 1L, name = "Test Institution"))

        val result = useCase(LoadInstitutionalAnnouncementsParams(forceRefresh = false))

        assertEquals(1, result.size)
        assertEquals("", result[0].logoUrl)
    }

    @Test
    fun `execute correctly maps institution name from account`() = runTest {
        val notifications = listOf(
            AccountNotification(id = 1L, subject = "Announcement 1", message = "Message 1", icon = "info", startAt = "1970-01-01T00:00:01Z")
        )

        coEvery {
            accountNotificationRepository.getAccountNotifications(false)
        } returns DataResult.Success(notifications)

        coEvery {
            userRepository.getAccount(false)
        } returns DataResult.Success(Account(id = 1L, name = "Instructure University"))

        val result = useCase(LoadInstitutionalAnnouncementsParams(forceRefresh = false))

        assertEquals(1, result.size)
        assertEquals("Instructure University", result[0].institutionName)
    }
}