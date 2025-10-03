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
import com.instructure.canvasapi2.models.AccountNotification
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.StreamItem
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.canvasapi2.utils.toDate
import com.instructure.horizon.R
import com.instructure.horizon.features.inbox.HorizonInboxItemType
import com.instructure.horizon.features.inbox.navigation.HorizonInboxRoute
import com.instructure.horizon.horizonui.molecules.StatusChipColor
import com.instructure.testutils.ViewModelTestRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationViewModelTest {

    @get:Rule
    val viewModelTestRule = ViewModelTestRule()

    private val context: Context = mockk(relaxed = true)
    private val repository: NotificationRepository = mockk(relaxed = true)

    private val course = Course(1L, "Course 1")
    private val globalAnnouncement = AccountNotification(
        id = 1L,
        subject = "Global Announcement 1",
        startAt = Date().toApiString(),
    )
    val streamItems = listOf(
        StreamItem(
            id = 1,
            type = "Message",
            title = "Scored item",
            score = 5.0,
            course_id = course.id,
            htmlUrl = "deeplinkUrl1",
            assignment = Assignment(
                id = 1L,
                name = "Assignment 1",
                htmlUrl = "assignmentUrl1"
            )
        ),
        StreamItem(
            id = 2,
            type = "Message",
            notificationCategory = "Grading Policies",
            title = "Grading period item",
            course_id = course.id,
            htmlUrl = "deeplinkUrl2"
        ),
        StreamItem(
            id = 3,
            type = "Message",
            notificationCategory = "Due date",
            title = "Due Date",
            course_id = course.id,
            htmlUrl = "deeplinkUrl3"
        ),
        StreamItem(
            id = 4,
            type = "Announcement",
            course_id = course.id,
            htmlUrl = "deeplinkUrl4",
            title = "Announcement 1"
        ),
    )

    @Before
    fun setup() {
        every { context.getString(R.string.notificationsAnnouncementCategoryLabel) } returns "Announcement"
        every { context.getString(R.string.notificationsDueDateCategoryLabel) } returns "Due date"
        every { context.getString(R.string.notificationsScoreCategoryLabel) } returns "Score"
        every { context.getString(R.string.notificationsScoreChangedCategoryLabel) } returns "Score changed"
        every { context.getString(R.string.notificationsScoredItemTitle, "Scored item") } returns "Scored item's score is now available"
        coEvery { repository.getCourse(course.id) } returns course
        coEvery { repository.getGlobalAnnouncements(any()) } returns listOf(globalAnnouncement)
        coEvery { repository.getNotifications(any()) } returns streamItems
    }

    @Test
    fun `Test ViewModel mapping to NotificationItems`() {
        val viewModel = getViewModel()
        val state = viewModel.uiState.value
        assertEquals(5, state.notificationItems.size)
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
                route = NotificationRoute.DeepLink("assignmentUrl1")
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
                route = NotificationRoute.DeepLink("deeplinkUrl2")
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
                route = NotificationRoute.DeepLink("deeplinkUrl3")
            )
        ))
        assertTrue(state.notificationItems.contains(
            NotificationItem(
                category = NotificationItemCategory(
                    "Announcement",
                    StatusChipColor.Sky
                ),
                title = "Announcement 1",
                courseLabel = course.name,
                date = null,
                isRead = false,
                route = NotificationRoute.DeepLink("deeplinkUrl4")
            )
        ))
        assertTrue(state.notificationItems.contains(
            NotificationItem(
                category = NotificationItemCategory(
                    "Announcement",
                    StatusChipColor.Sky
                ),
                title = "Global Announcement 1",
                courseLabel = null,
                date = globalAnnouncement.startAt.toDate(),
                isRead = true,
                route = NotificationRoute.ExplicitRoute(
                    HorizonInboxRoute.InboxDetails.route(
                        type = HorizonInboxItemType.AccountNotification,
                        id = 1L,
                        courseId = null
                    )
                )
            )
        ))
    }

    private fun getViewModel(): NotificationViewModel {
        return NotificationViewModel(context, repository)
    }
}
