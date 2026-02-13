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
 */

package com.instructure.pandautils.domain.usecase.accountnotification

import com.instructure.canvasapi2.models.AccountNotification
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.data.repository.accountnotification.AccountNotificationRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class DeleteAccountNotificationUseCaseTest {

    private val accountNotificationRepository: AccountNotificationRepository = mockk(relaxed = true)
    private lateinit var useCase: DeleteAccountNotificationUseCase

    @Before
    fun setup() {
        useCase = DeleteAccountNotificationUseCase(accountNotificationRepository)
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `execute deletes account notification successfully`() = runTest {
        val accountNotificationId = 123L
        val deletedNotification = AccountNotification(
            id = accountNotificationId,
            subject = "Test Notification",
            message = "Test Message",
            icon = "info"
        )

        coEvery {
            accountNotificationRepository.deleteAccountNotification(accountNotificationId)
        } returns DataResult.Success(deletedNotification)

        val result = useCase(DeleteAccountNotificationUseCase.Params(accountNotificationId))

        assertEquals(accountNotificationId, result.id)
        assertEquals("Test Notification", result.subject)
    }

    @Test
    fun `execute passes correct accountNotificationId to repository`() = runTest {
        val accountNotificationId = 456L
        val notification = AccountNotification(id = accountNotificationId)

        coEvery {
            accountNotificationRepository.deleteAccountNotification(accountNotificationId)
        } returns DataResult.Success(notification)

        useCase(DeleteAccountNotificationUseCase.Params(accountNotificationId))

        coVerify { accountNotificationRepository.deleteAccountNotification(accountNotificationId) }
    }

    @Test(expected = IllegalStateException::class)
    fun `execute throws when repository returns failure`() = runTest {
        val accountNotificationId = 789L

        coEvery {
            accountNotificationRepository.deleteAccountNotification(accountNotificationId)
        } returns DataResult.Fail()

        useCase(DeleteAccountNotificationUseCase.Params(accountNotificationId))
    }

    @Test
    fun `execute returns deleted notification from repository`() = runTest {
        val accountNotificationId = 999L
        val deletedNotification = AccountNotification(
            id = accountNotificationId,
            subject = "Deleted Notification",
            message = "This was deleted",
            icon = "warning"
        )

        coEvery {
            accountNotificationRepository.deleteAccountNotification(accountNotificationId)
        } returns DataResult.Success(deletedNotification)

        val result = useCase(DeleteAccountNotificationUseCase.Params(accountNotificationId))

        assertEquals(deletedNotification, result)
    }
}