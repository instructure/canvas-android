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

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.instructure.horizon.R
import com.instructure.horizon.features.dashboard.DashboardItemState
import com.instructure.horizon.features.dashboard.widget.DashboardPaginatedWidgetCardHeaderState
import com.instructure.horizon.features.dashboard.widget.DashboardPaginatedWidgetCardItemState
import com.instructure.horizon.features.dashboard.widget.DashboardPaginatedWidgetCardState
import com.instructure.horizon.features.dashboard.widget.announcement.DashboardAnnouncementBannerSection
import com.instructure.horizon.features.dashboard.widget.announcement.DashboardAnnouncementBannerUiState
import com.instructure.horizon.horizonui.foundation.HorizonColors
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

        composeTestRule.onNodeWithText(errorMessage, substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Refresh", useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        assert(refreshCalled) { "Refresh callback should be called when retry button is clicked" }
    }

    @Test
    fun testSuccessStateWithSingleAnnouncementDisplaysCorrectly() {
        val testAnnouncement = DashboardPaginatedWidgetCardItemState(
            headerState = DashboardPaginatedWidgetCardHeaderState(
                label = "Announcement",
                color = HorizonColors.Surface.institution().copy(alpha = 0.1f),
                iconRes = R.drawable.campaign
            ),
            title = "Important Announcement",
            source = "Course Name",
            date = Date(),
            route = ""
        )

        val uiState = DashboardAnnouncementBannerUiState(
            state = DashboardItemState.SUCCESS,
            cardState = DashboardPaginatedWidgetCardState(
                listOf(testAnnouncement)
            ),
            onRefresh = { it() }
        )

        composeTestRule.setContent {
            DashboardAnnouncementBannerSection(uiState, rememberNavController())
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
                headerState = DashboardPaginatedWidgetCardHeaderState(
                    label = "Announcement",
                    color = HorizonColors.Surface.institution().copy(alpha = 0.1f),
                    iconRes = R.drawable.campaign
                ),
                title = "First Announcement",
                source = "Course 1",
                date = Date(),
                route = ""
            ),
            DashboardPaginatedWidgetCardItemState(
                headerState = DashboardPaginatedWidgetCardHeaderState(
                    label = "Announcement",
                    color = HorizonColors.Surface.institution().copy(alpha = 0.1f),
                    iconRes = R.drawable.campaign
                ),
                title = "Second Announcement",
                source = "Course 2",
                date = Date(),
                route = ""
            )
        )

        val uiState = DashboardAnnouncementBannerUiState(
            state = DashboardItemState.SUCCESS,
            cardState = DashboardPaginatedWidgetCardState(announcements),
            onRefresh = { it() }
        )

        composeTestRule.setContent {
            DashboardAnnouncementBannerSection(uiState, rememberNavController())
        }

        composeTestRule.onNodeWithText("First Announcement").assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.dashboardAnnouncementBannerFrom, "Course 1")).assertIsDisplayed()

        composeTestRule.onAllNodesWithText("1 of 2").onFirst().assertIsDisplayed()
    }

    @Test
    fun testSuccessStateWithGlobalAnnouncementWithoutSource() {
        val testAnnouncement = DashboardPaginatedWidgetCardItemState(
            headerState = DashboardPaginatedWidgetCardHeaderState(
                label = "Announcement",
                color = HorizonColors.Surface.institution().copy(alpha = 0.1f),
                iconRes = R.drawable.campaign
            ),
            title = "Global Announcement",
            source = null,
            date = Date(),
            route = ""
        )

        val uiState = DashboardAnnouncementBannerUiState(
            state = DashboardItemState.SUCCESS,
            cardState = DashboardPaginatedWidgetCardState(listOf(testAnnouncement)),
            onRefresh = { it() }
        )

        composeTestRule.setContent {
            DashboardAnnouncementBannerSection(uiState, rememberNavController())
        }

        val announcementLabel = context.getString(R.string.notificationsAnnouncementCategoryLabel)

        composeTestRule.onNodeWithText(announcementLabel).assertIsDisplayed()
        composeTestRule.onNodeWithText("Global Announcement").assertIsDisplayed()
    }

    @Test
    fun testSuccessStateAnnouncementIsClickable() {
        val testAnnouncement = DashboardPaginatedWidgetCardItemState(
            headerState = DashboardPaginatedWidgetCardHeaderState(
                label = "Announcement",
                color = HorizonColors.Surface.institution().copy(alpha = 0.1f),
                iconRes = R.drawable.campaign
            ),
            title = "Test Announcement",
            source = "Test Course",
            date = Date(),
            route = ""
        )

        val uiState = DashboardAnnouncementBannerUiState(
            state = DashboardItemState.SUCCESS,
            cardState = DashboardPaginatedWidgetCardState(listOf(testAnnouncement)),
            onRefresh = { it() }
        )

        composeTestRule.setContent {
            DashboardAnnouncementBannerSection(uiState, rememberNavController())
        }


        composeTestRule.onNodeWithText(testAnnouncement.title!!)
            .assertIsDisplayed()
    }

    @Test
    fun testSuccessStateDisplaysDateCorrectly() {
        val testDate = Calendar.getInstance().apply { set(2024, 0, 1) }.time

        val testAnnouncement = DashboardPaginatedWidgetCardItemState(
            headerState = DashboardPaginatedWidgetCardHeaderState(
                label = "Announcement",
                color = HorizonColors.Surface.institution().copy(alpha = 0.1f),
                iconRes = R.drawable.campaign
            ),
            title = "Dated Announcement",
            source = "Test Course",
            date = testDate,
            route = ""
        )

        val uiState = DashboardAnnouncementBannerUiState(
            state = DashboardItemState.SUCCESS,
            cardState = DashboardPaginatedWidgetCardState(listOf(testAnnouncement)),
            onRefresh = { it() }
        )

        composeTestRule.setContent {
            DashboardAnnouncementBannerSection(uiState, rememberNavController())
        }

        composeTestRule.onNodeWithText("Jan 01, 2024").assertIsDisplayed()
    }

    @Test
    fun testSuccessStateWithAnnouncementWithoutDate() {
        val testAnnouncement = DashboardPaginatedWidgetCardItemState(
            headerState = DashboardPaginatedWidgetCardHeaderState(
                label = "Announcement",
                color = HorizonColors.Surface.institution().copy(alpha = 0.1f),
                iconRes = R.drawable.campaign
            ),
            title = "No Date Announcement",
            source = "Test Course",
            date = null,
            route = ""
        )

        val uiState = DashboardAnnouncementBannerUiState(
            state = DashboardItemState.SUCCESS,
            cardState = DashboardPaginatedWidgetCardState(listOf(testAnnouncement)),
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
        val testAnnouncement = DashboardPaginatedWidgetCardItemState(
            headerState = DashboardPaginatedWidgetCardHeaderState(
                label = "Announcement",
                color = HorizonColors.Surface.institution().copy(alpha = 0.1f),
                iconRes = R.drawable.campaign
            ),
            title = longTitle,
            source = "Test Course",
            date = Date(),
            route = ""
        )

        val uiState = DashboardAnnouncementBannerUiState(
            state = DashboardItemState.SUCCESS,
            cardState = DashboardPaginatedWidgetCardState(listOf(testAnnouncement)),
            onRefresh = { it() }
        )

        composeTestRule.setContent {
            DashboardAnnouncementBannerSection(uiState, rememberNavController())
        }

        composeTestRule.onNodeWithText(longTitle).assertIsDisplayed()
    }
}
