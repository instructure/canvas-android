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
import com.instructure.horizon.features.dashboard.widget.DashboardPaginatedWidgetCardButtonRoute
import com.instructure.horizon.features.dashboard.widget.DashboardPaginatedWidgetCardChipState
import com.instructure.horizon.features.dashboard.widget.DashboardPaginatedWidgetCardItemState
import com.instructure.horizon.features.dashboard.widget.DashboardPaginatedWidgetCardState
import com.instructure.horizon.features.dashboard.widget.announcement.DashboardAnnouncementBannerSection
import com.instructure.horizon.features.dashboard.widget.announcement.DashboardAnnouncementBannerUiState
import com.instructure.horizon.horizonui.molecules.StatusChipColor
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar
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
            DashboardAnnouncementBannerSection(uiState, rememberNavController(), rememberNavController())
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
            DashboardAnnouncementBannerSection(uiState, rememberNavController(), rememberNavController())
        }

        val errorMessage = context.getString(R.string.dashboardAnnouncementBannerErrorMessage)

        composeTestRule.onNodeWithText(errorMessage, substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Refresh")
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick()

        assert(refreshCalled) { "Refresh callback should be called when retry button is clicked" }
    }

    @Test
    fun testSuccessStateWithSingleAnnouncementDisplaysCorrectly() {
        val testAnnouncement = DashboardPaginatedWidgetCardItemState(
            chipState = DashboardPaginatedWidgetCardChipState(
                label = "Announcement",
                color = StatusChipColor.Sky
            ),
            title = "Important Announcement",
            source = "Course Name",
            date = Date(),
            route = DashboardPaginatedWidgetCardButtonRoute.MainRoute("")
        )

        val uiState = DashboardAnnouncementBannerUiState(
            state = DashboardItemState.SUCCESS,
            cardState = DashboardPaginatedWidgetCardState(
                listOf(testAnnouncement)
            ),
            onRefresh = { it() }
        )

        composeTestRule.setContent {
            DashboardAnnouncementBannerSection(uiState, rememberNavController(), rememberNavController())
        }

        val announcementLabel = context.getString(R.string.notificationsAnnouncementCategoryLabel)
        val fromLabel = context.getString(R.string.dashboardAnnouncementBannerFrom, "Course Name")

        composeTestRule.onNodeWithText(announcementLabel).assertIsDisplayed()
        composeTestRule.onNodeWithText(fromLabel).assertIsDisplayed()
        composeTestRule.onNodeWithText("Important Announcement").assertIsDisplayed()
    }

    @Test
    fun testSuccessStateWithMultipleAnnouncementsDisplaysAllItems() {
        val announcements = listOf(
            DashboardPaginatedWidgetCardItemState(
                pageState = "1 of 2",
                chipState = DashboardPaginatedWidgetCardChipState(
                    label = "Announcement",
                    color = StatusChipColor.Sky
                ),
                title = "First Announcement",
                source = "Course 1",
                date = Date(),
                route = DashboardPaginatedWidgetCardButtonRoute.MainRoute("")
            ),
            DashboardPaginatedWidgetCardItemState(
                pageState = "2 of 2",
                chipState = DashboardPaginatedWidgetCardChipState(
                    label = "Announcement",
                    color = StatusChipColor.Sky
                ),
                title = "Second Announcement",
                source = "Course 2",
                date = Date(),
                route = DashboardPaginatedWidgetCardButtonRoute.MainRoute("")
            )
        )

        val uiState = DashboardAnnouncementBannerUiState(
            state = DashboardItemState.SUCCESS,
            cardState = DashboardPaginatedWidgetCardState(announcements),
            onRefresh = { it() }
        )

        composeTestRule.setContent {
            DashboardAnnouncementBannerSection(uiState, rememberNavController(), rememberNavController())
        }

        composeTestRule.onNodeWithText("First Announcement").assertIsDisplayed()
        composeTestRule.onNodeWithText(
            context.getString(R.string.dashboardAnnouncementBannerFrom, "Course 1")
        ).assertIsDisplayed()

        composeTestRule.onNodeWithText("1 of 2", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun testSuccessStateWithGlobalAnnouncementWithoutSource() {
        val testAnnouncement = DashboardPaginatedWidgetCardItemState(
            chipState = DashboardPaginatedWidgetCardChipState(
                label = "Announcement",
                color = StatusChipColor.Sky
            ),
            title = "Global Announcement",
            source = null,
            date = Date(),
            route = DashboardPaginatedWidgetCardButtonRoute.MainRoute("")
        )

        val uiState = DashboardAnnouncementBannerUiState(
            state = DashboardItemState.SUCCESS,
            cardState = DashboardPaginatedWidgetCardState(listOf(testAnnouncement)),
            onRefresh = { it() }
        )

        composeTestRule.setContent {
            DashboardAnnouncementBannerSection(uiState, rememberNavController(), rememberNavController())
        }

        val announcementLabel = context.getString(R.string.notificationsAnnouncementCategoryLabel)

        composeTestRule.onNodeWithText(announcementLabel).assertIsDisplayed()
        composeTestRule.onNodeWithText("Global Announcement").assertIsDisplayed()
    }

    @Test
    fun testSuccessStateAnnouncementIsClickable() {
        val testAnnouncement = DashboardPaginatedWidgetCardItemState(
            chipState = DashboardPaginatedWidgetCardChipState(
                label = "Announcement",
                color = StatusChipColor.Sky
            ),
            title = "Test Announcement",
            source = "Test Course",
            date = Date(),
            route = DashboardPaginatedWidgetCardButtonRoute.MainRoute("")
        )

        val uiState = DashboardAnnouncementBannerUiState(
            state = DashboardItemState.SUCCESS,
            cardState = DashboardPaginatedWidgetCardState(listOf(testAnnouncement)),
            onRefresh = { it() }
        )

        composeTestRule.setContent {
            DashboardAnnouncementBannerSection(uiState, rememberNavController(), rememberNavController())
        }


        composeTestRule.onNodeWithText(testAnnouncement.title!!)
            .assertIsDisplayed()
    }

    @Test
    fun testSuccessStateDisplaysDateCorrectly() {
        val testDate = Calendar.getInstance().apply { set(2024, 0, 1) }.time

        val testAnnouncement = DashboardPaginatedWidgetCardItemState(
            chipState = DashboardPaginatedWidgetCardChipState(
                label = "Announcement",
                color = StatusChipColor.Sky
            ),
            title = "Dated Announcement",
            source = "Test Course",
            date = testDate,
            route = DashboardPaginatedWidgetCardButtonRoute.MainRoute("")
        )

        val uiState = DashboardAnnouncementBannerUiState(
            state = DashboardItemState.SUCCESS,
            cardState = DashboardPaginatedWidgetCardState(listOf(testAnnouncement)),
            onRefresh = { it() }
        )

        composeTestRule.setContent {
            DashboardAnnouncementBannerSection(uiState, rememberNavController(), rememberNavController())
        }

        composeTestRule.onNodeWithText("Jan 01, 2024").assertIsDisplayed()
    }

    @Test
    fun testSuccessStateWithAnnouncementWithoutDate() {
        val testAnnouncement = DashboardPaginatedWidgetCardItemState(
            chipState = DashboardPaginatedWidgetCardChipState(
                label = "Announcement",
                color = StatusChipColor.Sky
            ),
            title = "No Date Announcement",
            source = "Test Course",
            date = null,
            route = DashboardPaginatedWidgetCardButtonRoute.MainRoute("")
        )

        val uiState = DashboardAnnouncementBannerUiState(
            state = DashboardItemState.SUCCESS,
            cardState = DashboardPaginatedWidgetCardState(listOf(testAnnouncement)),
            onRefresh = { it() }
        )

        composeTestRule.setContent {
            DashboardAnnouncementBannerSection(uiState, rememberNavController(), rememberNavController())
        }

        composeTestRule.onNodeWithText("No Date Announcement").assertIsDisplayed()
    }

    @Test
    fun testSuccessStateWithLongAnnouncementTitle() {
        val longTitle = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. This is a very long announcement title that should be displayed properly in the widget."
        val testAnnouncement = DashboardPaginatedWidgetCardItemState(
            chipState = DashboardPaginatedWidgetCardChipState(
                label = "Announcement",
                color = StatusChipColor.Sky
            ),
            title = longTitle,
            source = "Test Course",
            date = Date(),
            route = DashboardPaginatedWidgetCardButtonRoute.MainRoute("")
        )

        val uiState = DashboardAnnouncementBannerUiState(
            state = DashboardItemState.SUCCESS,
            cardState = DashboardPaginatedWidgetCardState(listOf(testAnnouncement)),
            onRefresh = { it() }
        )

        composeTestRule.setContent {
            DashboardAnnouncementBannerSection(uiState, rememberNavController(), rememberNavController())
        }

        composeTestRule.onNodeWithText(longTitle).assertIsDisplayed()
    }
}
