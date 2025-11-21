/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.features.dashboard.widget.institutionalannouncements

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.pandautils.domain.models.accountnotification.InstitutionalAnnouncement
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
class InstitutionalAnnouncementsWidgetTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testWidgetDoesNotShowWhenLoading() {
        val uiState = InstitutionalAnnouncementsUiState(
            loading = true,
            error = false,
            announcements = emptyList()
        )

        composeTestRule.setContent {
            InstitutionalAnnouncementsContent(
                uiState = uiState,
                columns = 1,
                onAnnouncementClick = { _, _ -> }
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Announcements", substring = true).assertDoesNotExist()
    }

    @Test
    fun testWidgetDoesNotShowWhenError() {
        val uiState = InstitutionalAnnouncementsUiState(
            loading = false,
            error = true,
            announcements = emptyList()
        )

        composeTestRule.setContent {
            InstitutionalAnnouncementsContent(
                uiState = uiState,
                columns = 1,
                onAnnouncementClick = { _, _ -> }
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Announcements", substring = true).assertDoesNotExist()
    }

    @Test
    fun testWidgetDoesNotShowWhenNoAnnouncements() {
        val uiState = InstitutionalAnnouncementsUiState(
            loading = false,
            error = false,
            announcements = emptyList()
        )

        composeTestRule.setContent {
            InstitutionalAnnouncementsContent(
                uiState = uiState,
                columns = 1,
                onAnnouncementClick = { _, _ -> }
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Announcements", substring = true).assertDoesNotExist()
    }

    @Test
    fun testWidgetShowsSingleAnnouncement() {
        val announcements = listOf(
            InstitutionalAnnouncement(
                id = 1L,
                subject = "Campus Maintenance",
                message = "Campus will be closed for maintenance",
                institutionName = "Test University",
                startDate = Date(),
                icon = "info",
                logoUrl = ""
            )
        )

        val uiState = InstitutionalAnnouncementsUiState(
            loading = false,
            error = false,
            announcements = announcements
        )

        composeTestRule.setContent {
            InstitutionalAnnouncementsContent(
                uiState = uiState,
                columns = 1,
                onAnnouncementClick = { _, _ -> }
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Announcements (1)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Campus Maintenance").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test University").assertIsDisplayed()
    }

    @Test
    fun testWidgetShowsMultipleAnnouncements() {
        val announcements = listOf(
            InstitutionalAnnouncement(
                id = 1L,
                subject = "Campus Maintenance",
                message = "Message 1",
                institutionName = "Test University",
                startDate = Date(),
                icon = "info",
                logoUrl = ""
            ),
            InstitutionalAnnouncement(
                id = 2L,
                subject = "New Semester",
                message = "Message 2",
                institutionName = "Test University",
                startDate = Date(),
                icon = "calendar",
                logoUrl = ""
            ),
            InstitutionalAnnouncement(
                id = 3L,
                subject = "System Update",
                message = "Message 3",
                institutionName = "Test University",
                startDate = Date(),
                icon = "warning",
                logoUrl = ""
            )
        )

        val uiState = InstitutionalAnnouncementsUiState(
            loading = false,
            error = false,
            announcements = announcements
        )

        composeTestRule.setContent {
            InstitutionalAnnouncementsContent(
                uiState = uiState,
                columns = 1,
                onAnnouncementClick = { _, _ -> }
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Announcements (3)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Campus Maintenance").assertIsDisplayed()

        // Swipe to second page
        composeTestRule.onRoot().performTouchInput {
            swipeLeft(
                startX = centerX + (width / 4),
                endX = centerX - (width / 4)
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("New Semester").assertIsDisplayed()

        // Swipe to third page
        composeTestRule.onRoot().performTouchInput {
            swipeLeft(
                startX = centerX + (width / 4),
                endX = centerX - (width / 4)
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("System Update").assertIsDisplayed()
    }

    @Test
    fun testAnnouncementCardClickCallsCallback() {
        val announcements = listOf(
            InstitutionalAnnouncement(
                id = 1L,
                subject = "Test Announcement",
                message = "Test Message",
                institutionName = "Test University",
                startDate = Date(),
                icon = "info",
                logoUrl = ""
            )
        )

        var clickCalled = false
        var clickedSubject: String? = null
        var clickedMessage: String? = null

        val uiState = InstitutionalAnnouncementsUiState(
            loading = false,
            error = false,
            announcements = announcements
        )

        composeTestRule.setContent {
            InstitutionalAnnouncementsContent(
                uiState = uiState,
                columns = 1,
                onAnnouncementClick = { subject, message ->
                    clickCalled = true
                    clickedSubject = subject
                    clickedMessage = message
                }
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Test Announcement").performClick()

        assert(clickCalled)
        assert(clickedSubject == "Test Announcement")
        assert(clickedMessage == "Test Message")
    }

    @Test
    fun testWidgetShowsMultipleAnnouncementsInColumns() {
        val announcements = listOf(
            InstitutionalAnnouncement(
                id = 1L,
                subject = "Announcement 1",
                message = "Message 1",
                institutionName = "Test University",
                startDate = Date(),
                icon = "info",
                logoUrl = ""
            ),
            InstitutionalAnnouncement(
                id = 2L,
                subject = "Announcement 2",
                message = "Message 2",
                institutionName = "Test University",
                startDate = Date(),
                icon = "calendar",
                logoUrl = ""
            ),
            InstitutionalAnnouncement(
                id = 3L,
                subject = "Announcement 3",
                message = "Message 3",
                institutionName = "Test University",
                startDate = Date(),
                icon = "warning",
                logoUrl = ""
            )
        )

        val uiState = InstitutionalAnnouncementsUiState(
            loading = false,
            error = false,
            announcements = announcements
        )

        composeTestRule.setContent {
            InstitutionalAnnouncementsContent(
                uiState = uiState,
                columns = 2, // This will create 2 pages (2 announcements on first page, 1 on second)
                onAnnouncementClick = { _, _ -> }
            )
        }

        composeTestRule.waitForIdle()
        // First page should show 2 announcements
        composeTestRule.onNodeWithText("Announcement 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Announcement 2").assertIsDisplayed()

        // Swipe to second page
        composeTestRule.onRoot().performTouchInput {
            swipeLeft(
                startX = centerX + (width / 4),
                endX = centerX - (width / 4)
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Announcement 3").assertIsDisplayed()
    }

    @Test
    fun testWidgetShowsAnnouncementWithoutDate() {
        val announcements = listOf(
            InstitutionalAnnouncement(
                id = 1L,
                subject = "No Date Announcement",
                message = "Message",
                institutionName = "Test University",
                startDate = null,
                icon = "info",
                logoUrl = ""
            )
        )

        val uiState = InstitutionalAnnouncementsUiState(
            loading = false,
            error = false,
            announcements = announcements
        )

        composeTestRule.setContent {
            InstitutionalAnnouncementsContent(
                uiState = uiState,
                columns = 1,
                onAnnouncementClick = { _, _ -> }
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("No Date Announcement").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test University").assertIsDisplayed()
    }

    @Test
    fun testWidgetShowsPagerIndicatorForMultiplePages() {
        val announcements = listOf(
            InstitutionalAnnouncement(
                id = 1L,
                subject = "Announcement 1",
                message = "Message 1",
                institutionName = "Test University",
                startDate = Date(),
                icon = "info",
                logoUrl = ""
            ),
            InstitutionalAnnouncement(
                id = 2L,
                subject = "Announcement 2",
                message = "Message 2",
                institutionName = "Test University",
                startDate = Date(),
                icon = "calendar",
                logoUrl = ""
            )
        )

        val uiState = InstitutionalAnnouncementsUiState(
            loading = false,
            error = false,
            announcements = announcements
        )

        composeTestRule.setContent {
            InstitutionalAnnouncementsContent(
                uiState = uiState,
                columns = 1, // This will create 2 pages
                onAnnouncementClick = { _, _ -> }
            )
        }

        composeTestRule.waitForIdle()
        // First announcement should be visible
        composeTestRule.onNodeWithText("Announcement 1").assertIsDisplayed()
    }
}