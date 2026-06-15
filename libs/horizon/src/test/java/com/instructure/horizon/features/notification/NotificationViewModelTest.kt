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
package com.instructure.horizon.features.notification

import android.content.Context
import com.instructure.horizon.R
import com.instructure.horizon.domain.usecase.GetNotificationStreamUseCase
import com.instructure.horizon.horizonui.molecules.StatusChipColor
import com.instructure.horizon.model.NotificationStreamItem
import com.instructure.horizon.offline.HorizonOfflineState
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationViewModelTest {
    private val context: Context = mockk(relaxed = true)
    private val getNotificationStreamUseCase: GetNotificationStreamUseCase = mockk(relaxed = true)
    private val offlineState: HorizonOfflineState = mockk(relaxed = true)
    private val isOfflineFlow = MutableStateFlow(false)
    private val testDispatcher = UnconfinedTestDispatcher()

    private val courseId = 1L
    private val courseName = "Course 1"
    val items = listOf(
        NotificationStreamItem(
            id = 1,
            type = "Message",
            notificationCategory = "",
            title = "Scored item",
            courseId = courseId,
            courseName = courseName,
            isRead = false,
            score = 5.0,
            grade = null,
            updatedAt = null,
            htmlUrl = "deeplinkUrl1",
            assignmentHtmlUrl = "assignmentUrl1",
        ),
        NotificationStreamItem(
            id = 2,
            type = "Message",
            notificationCategory = "Grading Policies",
            title = "Grading period item",
            courseId = courseId,
            courseName = courseName,
            isRead = false,
            score = -1.0,
            grade = null,
            updatedAt = null,
            htmlUrl = "deeplinkUrl2",
            assignmentHtmlUrl = null,
        ),
        NotificationStreamItem(
            id = 3,
            type = "Message",
            notificationCategory = "Due Date",
            title = "Due Date",
            courseId = courseId,
            courseName = courseName,
            isRead = false,
            score = -1.0,
            grade = null,
            updatedAt = null,
            htmlUrl = "deeplinkUrl3",
            assignmentHtmlUrl = null,
        ),
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { context.getString(R.string.notificationsAnnouncementCategoryLabel) } returns "Announcement"
        every { context.getString(R.string.notificationsDueDateCategoryLabel) } returns "Due date"
        every { context.getString(R.string.notificationsScoreCategoryLabel) } returns "Score"
        every { context.getString(R.string.notificationsScoreChangedCategoryLabel) } returns "Score changed"
        every { context.getString(R.string.notificationsScoredItemTitle, "Scored item") } returns "Scored item's score is now available"
        every { offlineState.isOffline } returns isOfflineFlow
        coEvery { getNotificationStreamUseCase(any()) } returns items
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `Test ViewModel mapping to NotificationItems`() {
        val viewModel = getViewModel()
        val state = viewModel.uiState.value
        assertEquals(3, state.notificationItems.size)
        assertTrue(state.notificationItems.contains(
            NotificationItem(
                category = NotificationItemCategory(
                    "Score changed",
                    StatusChipColor.Violet
                ),
                title = "Scored item's score is now available",
                courseLabel = null,
                date = null,
                isRead = false,
                deepLink = "assignmentUrl1"
            )
        ))
        assertTrue(state.notificationItems.contains(
            NotificationItem(
                category = NotificationItemCategory(
                    "Score",
                    StatusChipColor.Violet
                ),
                title = "Grading period item",
                courseLabel = null,
                date = null,
                isRead = false,
                deepLink = "deeplinkUrl2"
            )
        ))
        assertTrue(state.notificationItems.contains(
            NotificationItem(
                category = NotificationItemCategory(
                    "Due date",
                    StatusChipColor.Honey
                ),
                title = "Due Date",
                courseLabel = null,
                date = null,
                isRead = false,
                deepLink = "deeplinkUrl3"
            )
        ))
        assertFalse(state.notificationItems.contains(
            NotificationItem(
                category = NotificationItemCategory(
                    "Announcement",
                    StatusChipColor.Sky
                ),
                title = "Announcement 1",
                courseLabel = courseName,
                date = null,
                isRead = false,
                deepLink = "deeplinkUrl4"
            )
        ))
    }

    private fun getViewModel(): NotificationViewModel {
        return NotificationViewModel(
            context,
            getNotificationStreamUseCase,
            offlineState,
        )
    }
}
