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
package com.instructure.student.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.instructure.canvasapi2.apis.UnreadCountAPI
import com.instructure.canvasapi2.models.UnreadConversationCount
import com.instructure.canvasapi2.models.UnreadNotificationCount
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CallbackViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val lifecycleOwner: LifecycleOwner = mockk(relaxed = true)
    private val lifecycleRegistry = LifecycleRegistry(lifecycleOwner)
    private val testDispatcher = UnconfinedTestDispatcher()

    private val unreadCountApi: UnreadCountAPI.UnreadCountsInterface = mockk(relaxed = true)

    private lateinit var viewModel: CallbackViewModel

    @Before
    fun setup() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        Dispatchers.setMain(testDispatcher)

        coEvery { unreadCountApi.getUnreadConversationCount(any()) } returns DataResult.Success(
            UnreadConversationCount("2")
        )

        coEvery { unreadCountApi.getNotificationsCount(any()) } returns DataResult.Success(
            listOf(UnreadNotificationCount(unreadCount = 3))
        )

        viewModel = CallbackViewModel(unreadCountApi)
    }

    @Test
    fun `updateNotificationCount should emit correct value to flow`() = runTest {
        viewModel.updateNotificationCount()
        assertEquals(3, viewModel.unreadNotificationCountFlow.value)
    }

    @Test
    fun `updateMessageCount should emit correct value to flow`() = runTest {
        viewModel.updateMessageCount()
        assertEquals(2, viewModel.unreadMessageCountFlow.value)
    }
}
