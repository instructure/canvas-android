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
package com.instructure.horizon.ui.features.dashboard.widget.announcement

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.instructure.horizon.R
import com.instructure.horizon.features.dashboard.DashboardItemState
import com.instructure.horizon.features.dashboard.widget.announcement.AnnouncementBannerItem
import com.instructure.horizon.features.dashboard.widget.announcement.AnnouncementType
import com.instructure.horizon.features.dashboard.widget.announcement.DashboardAnnouncementBannerSection
import com.instructure.horizon.features.dashboard.widget.announcement.DashboardAnnouncementBannerUiState
import com.instructure.horizon.features.dashboard.widget.announcement.card.DashboardAnnouncementBannerCardState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
class DashboardAnnouncementBannerWidgetUiTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun testLoadingStateDisplaysShimmerEffect() {
        val uiState = DashboardAnnouncementBannerUiState(
            state = DashboardItemState.LOADING,
            onRefresh = { it() }
        )

        composeTestRule.setContent {
            DashboardAnnouncementBannerSection(uiState, rememberNavController())
        }

        composeTestRule.waitForIdle()
    }

    @Test
    fun testErrorStateDisplaysErrorMessageAndRetryButton() {
        var refreshCalled = false
        val uiState = DashboardAnnouncementBannerUiState(
            state = DashboardItemState.ERROR,
            onRefresh = { onComplete ->
                refreshCalled = true
                onComplete()
            }
        )

        composeTestRule.setContent {
            DashboardAnnouncementBannerSection(uiState, rememberNavController())
        }

        val errorMessage = context.getString(R.string.dashboardAnnouncementBannerErrorMessage)
        val retryLabel = context.getString(R.string.retry)

        composeTestRule.onNodeWithText(errorMessage, substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText(retryLabel)
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick()

        assert(refreshCalled) { "Refresh callback should be called when retry button is clicked" }
    }

    @Test
    fun testSuccessStateWithSingleAnnouncementDisplaysCorrectly() {
        val testAnnouncement = AnnouncementBannerItem(
            title = "Important Announcement",
            source = "Course Name",
            date = Date(),
            type = AnnouncementType.COURSE,
            route = "/test"
        )

        val uiState = DashboardAnnouncementBannerUiState(
            state = DashboardItemState.SUCCESS,
            cardState = DashboardAnnouncementBannerCardState(
                announcements = listOf(testAnnouncement)
            ),
            onRefresh = { it() }
        )

        composeTestRule.setContent {
            DashboardAnnouncementBannerSection(uiState, rememberNavController())
        }

        val announcementLabel = context.getString(R.string.notificationsAnnouncementCategoryLabel)
        val fromLabel = context.getString(R.string.dashboardAnnouncementBannerFrom, "Course Name")
        val goToLabel = context.getString(R.string.dashboardAnnouncementBannerGoToAnnouncement)

        composeTestRule.onNodeWithText(announcementLabel).assertIsDisplayed()
        composeTestRule.onNodeWithText(fromLabel).assertIsDisplayed()
        composeTestRule.onNodeWithText("Important Announcement").assertIsDisplayed()
        composeTestRule.onNodeWithText(goToLabel).assertIsDisplayed()
    }

    @Test
    fun testSuccessStateWithMultipleAnnouncementsDisplaysAllItems() {
        val announcements = listOf(
            AnnouncementBannerItem(
                title = "First Announcement",
                source = "Course 1",
                date = Date(),
                type = AnnouncementType.COURSE,
                route = "/test1"
            ),
            AnnouncementBannerItem(
                title = "Second Announcement",
                source = "Course 2",
                date = Date(),
                type = AnnouncementType.COURSE,
                route = "/test2"
            )
        )

        val uiState = DashboardAnnouncementBannerUiState(
            state = DashboardItemState.SUCCESS,
            cardState = DashboardAnnouncementBannerCardState(
                announcements = announcements
            ),
            onRefresh = { it() }
        )

        composeTestRule.setContent {
            DashboardAnnouncementBannerSection(uiState, rememberNavController())
        }

        composeTestRule.onNodeWithText("First Announcement").assertIsDisplayed()
        composeTestRule.onNodeWithText("Second Announcement").assertIsDisplayed()
        composeTestRule.onNodeWithText(
            context.getString(R.string.dashboardAnnouncementBannerFrom, "Course 1")
        ).assertIsDisplayed()
        composeTestRule.onNodeWithText(
            context.getString(R.string.dashboardAnnouncementBannerFrom, "Course 2")
        ).assertIsDisplayed()
    }

    @Test
    fun testSuccessStateWithGlobalAnnouncementWithoutSource() {
        val testAnnouncement = AnnouncementBannerItem(
            title = "Global Announcement",
            source = null,
            date = Date(),
            type = AnnouncementType.GLOBAL,
            route = "/test"
        )

        val uiState = DashboardAnnouncementBannerUiState(
            state = DashboardItemState.SUCCESS,
            cardState = DashboardAnnouncementBannerCardState(
                announcements = listOf(testAnnouncement)
            ),
            onRefresh = { it() }
        )

        composeTestRule.setContent {
            DashboardAnnouncementBannerSection(uiState, rememberNavController())
        }

        val announcementLabel = context.getString(R.string.notificationsAnnouncementCategoryLabel)
        val goToLabel = context.getString(R.string.dashboardAnnouncementBannerGoToAnnouncement)

        composeTestRule.onNodeWithText(announcementLabel).assertIsDisplayed()
        composeTestRule.onNodeWithText("Global Announcement").assertIsDisplayed()
        composeTestRule.onNodeWithText(goToLabel).assertIsDisplayed()
    }

    @Test
    fun testSuccessStateGoToAnnouncementButtonIsClickable() {
        val testAnnouncement = AnnouncementBannerItem(
            title = "Test Announcement",
            source = "Test Course",
            date = Date(),
            type = AnnouncementType.COURSE,
            route = "/test"
        )

        val uiState = DashboardAnnouncementBannerUiState(
            state = DashboardItemState.SUCCESS,
            cardState = DashboardAnnouncementBannerCardState(
                announcements = listOf(testAnnouncement)
            ),
            onRefresh = { it() }
        )

        composeTestRule.setContent {
            DashboardAnnouncementBannerSection(uiState, rememberNavController())
        }

        val goToLabel = context.getString(R.string.dashboardAnnouncementBannerGoToAnnouncement)

        composeTestRule.onNodeWithText(goToLabel)
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun testSuccessStateDisplaysDateCorrectly() {
        val testDate = Date(1704067200000L)
        val testAnnouncement = AnnouncementBannerItem(
            title = "Dated Announcement",
            source = "Test Course",
            date = testDate,
            type = AnnouncementType.COURSE,
            route = "/test"
        )

        val uiState = DashboardAnnouncementBannerUiState(
            state = DashboardItemState.SUCCESS,
            cardState = DashboardAnnouncementBannerCardState(
                announcements = listOf(testAnnouncement)
            ),
            onRefresh = { it() }
        )

        composeTestRule.setContent {
            DashboardAnnouncementBannerSection(uiState, rememberNavController())
        }

        composeTestRule.onNodeWithText("Jan 01, 2024").assertIsDisplayed()
    }

    @Test
    fun testSuccessStateWithAnnouncementWithoutDate() {
        val testAnnouncement = AnnouncementBannerItem(
            title = "No Date Announcement",
            source = "Test Course",
            date = null,
            type = AnnouncementType.COURSE,
            route = "/test"
        )

        val uiState = DashboardAnnouncementBannerUiState(
            state = DashboardItemState.SUCCESS,
            cardState = DashboardAnnouncementBannerCardState(
                announcements = listOf(testAnnouncement)
            ),
            onRefresh = { it() }
        )

        composeTestRule.setContent {
            DashboardAnnouncementBannerSection(uiState, rememberNavController())
        }

        composeTestRule.onNodeWithText("No Date Announcement").assertIsDisplayed()
    }

    @Test
    fun testSuccessStateWithLongAnnouncementTitle() {
        val longTitle = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. This is a very long announcement title that should be displayed properly in the widget."
        val testAnnouncement = AnnouncementBannerItem(
            title = longTitle,
            source = "Test Course",
            date = Date(),
            type = AnnouncementType.COURSE,
            route = "/test"
        )

        val uiState = DashboardAnnouncementBannerUiState(
            state = DashboardItemState.SUCCESS,
            cardState = DashboardAnnouncementBannerCardState(
                announcements = listOf(testAnnouncement)
            ),
            onRefresh = { it() }
        )

        composeTestRule.setContent {
            DashboardAnnouncementBannerSection(uiState, rememberNavController())
        }

        composeTestRule.onNodeWithText(longTitle).assertIsDisplayed()
    }

    @Test
    fun testSuccessStateWithMixedAnnouncementTypes() {
        val announcements = listOf(
            AnnouncementBannerItem(
                title = "Course Announcement",
                source = "Course Name",
                date = Date(),
                type = AnnouncementType.COURSE,
                route = "/test1"
            ),
            AnnouncementBannerItem(
                title = "Global Announcement",
                source = null,
                date = Date(),
                type = AnnouncementType.GLOBAL,
                route = "/test2"
            )
        )

        val uiState = DashboardAnnouncementBannerUiState(
            state = DashboardItemState.SUCCESS,
            cardState = DashboardAnnouncementBannerCardState(
                announcements = announcements
            ),
            onRefresh = { it() }
        )

        composeTestRule.setContent {
            DashboardAnnouncementBannerSection(uiState, rememberNavController())
        }

        composeTestRule.onNodeWithText("Course Announcement").assertIsDisplayed()
        composeTestRule.onNodeWithText("Global Announcement").assertIsDisplayed()
        composeTestRule.onNodeWithText(
            context.getString(R.string.dashboardAnnouncementBannerFrom, "Course Name")
        ).assertIsDisplayed()
    }
}
