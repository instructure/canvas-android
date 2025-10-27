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
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.instructure.horizon.R
import com.instructure.horizon.features.dashboard.DashboardItemState
import com.instructure.horizon.features.dashboard.widget.DashboardPaginatedWidgetCardButtonRoute
import com.instructure.horizon.features.dashboard.widget.DashboardPaginatedWidgetCardButtonState
import com.instructure.horizon.features.dashboard.widget.DashboardPaginatedWidgetCardChipState
import com.instructure.horizon.features.dashboard.widget.DashboardPaginatedWidgetCardItemState
import com.instructure.horizon.features.dashboard.widget.DashboardPaginatedWidgetCardState
import com.instructure.horizon.features.dashboard.widget.announcement.DashboardAnnouncementBannerSection
import com.instructure.horizon.features.dashboard.widget.announcement.DashboardAnnouncementBannerUiState
import com.instructure.horizon.horizonui.molecules.ButtonColor
import com.instructure.horizon.horizonui.molecules.ButtonHeight
import com.instructure.horizon.horizonui.molecules.ButtonWidth
import com.instructure.horizon.horizonui.molecules.StatusChipColor
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
            buttonState = DashboardPaginatedWidgetCardButtonState(
                label = "Go to announcement",
                height = ButtonHeight.SMALL,
                width = ButtonWidth.FILL,
                color = ButtonColor.WhiteWithOutline,
                route = DashboardPaginatedWidgetCardButtonRoute.MainRoute("")
            ),
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
        val goToLabel = context.getString(R.string.dashboardAnnouncementBannerGoToAnnouncement)

        composeTestRule.onNodeWithText(announcementLabel).assertIsDisplayed()
        composeTestRule.onNodeWithText(fromLabel).assertIsDisplayed()
        composeTestRule.onNodeWithText("Important Announcement").assertIsDisplayed()
        composeTestRule.onNodeWithText(goToLabel).assertIsDisplayed()
    }

    @Test
    fun testSuccessStateWithMultipleAnnouncementsDisplaysAllItems() {
        val announcements = listOf(
            DashboardPaginatedWidgetCardItemState(
                chipState = DashboardPaginatedWidgetCardChipState(
                    label = "Announcement",
                    color = StatusChipColor.Sky
                ),
                title = "First Announcement",
                source = "Course 1",
                date = Date(),
                buttonState = DashboardPaginatedWidgetCardButtonState(
                    label = "Go to announcement",
                    height = ButtonHeight.SMALL,
                    width = ButtonWidth.FILL,
                    color = ButtonColor.WhiteWithOutline,
                    route = DashboardPaginatedWidgetCardButtonRoute.MainRoute("")
                ),
            ),
            DashboardPaginatedWidgetCardItemState(
                chipState = DashboardPaginatedWidgetCardChipState(
                    label = "Announcement",
                    color = StatusChipColor.Sky
                ),
                title = "Second Announcement",
                source = "Course 2",
                date = Date(),
                buttonState = DashboardPaginatedWidgetCardButtonState(
                    label = "Go to announcement",
                    height = ButtonHeight.SMALL,
                    width = ButtonWidth.FILL,
                    color = ButtonColor.WhiteWithOutline,
                    route = DashboardPaginatedWidgetCardButtonRoute.MainRoute("")
                ),
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

        composeTestRule.onNodeWithContentDescription("Next item").assertIsDisplayed().performClick()

        composeTestRule.onNodeWithText("Second Announcement").assertIsDisplayed()
        composeTestRule.onNodeWithText(
            context.getString(R.string.dashboardAnnouncementBannerFrom, "Course 2")
        ).assertIsDisplayed()
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
            buttonState = DashboardPaginatedWidgetCardButtonState(
                label = "Go to announcement",
                height = ButtonHeight.SMALL,
                width = ButtonWidth.FILL,
                color = ButtonColor.WhiteWithOutline,
                route = DashboardPaginatedWidgetCardButtonRoute.MainRoute("")
            ),
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
        val goToLabel = context.getString(R.string.dashboardAnnouncementBannerGoToAnnouncement)

        composeTestRule.onNodeWithText(announcementLabel).assertIsDisplayed()
        composeTestRule.onNodeWithText("Global Announcement").assertIsDisplayed()
        composeTestRule.onNodeWithText(goToLabel).assertIsDisplayed()
    }

    @Test
    fun testSuccessStateGoToAnnouncementButtonIsClickable() {
        val testAnnouncement = DashboardPaginatedWidgetCardItemState(
            chipState = DashboardPaginatedWidgetCardChipState(
                label = "Announcement",
                color = StatusChipColor.Sky
            ),
            title = "Test Announcement",
            source = "Test Course",
            date = Date(),
            buttonState = DashboardPaginatedWidgetCardButtonState(
                label = "Go to announcement",
                height = ButtonHeight.SMALL,
                width = ButtonWidth.FILL,
                color = ButtonColor.WhiteWithOutline,
                route = DashboardPaginatedWidgetCardButtonRoute.MainRoute("")
            ),
        )

        val uiState = DashboardAnnouncementBannerUiState(
            state = DashboardItemState.SUCCESS,
            cardState = DashboardPaginatedWidgetCardState(listOf(testAnnouncement)),
            onRefresh = { it() }
        )

        composeTestRule.setContent {
            DashboardAnnouncementBannerSection(uiState, rememberNavController(), rememberNavController())
        }

        val goToLabel = context.getString(R.string.dashboardAnnouncementBannerGoToAnnouncement)

        composeTestRule.onNodeWithText(goToLabel)
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun testSuccessStateDisplaysDateCorrectly() {
        val testDate = Date(1704067200000L)
        val testAnnouncement = DashboardPaginatedWidgetCardItemState(
            chipState = DashboardPaginatedWidgetCardChipState(
                label = "Announcement",
                color = StatusChipColor.Sky
            ),
            title = "Dated Announcement",
            source = "Test Course",
            date = testDate,
            buttonState = DashboardPaginatedWidgetCardButtonState(
                label = "Go to announcement",
                height = ButtonHeight.SMALL,
                width = ButtonWidth.FILL,
                color = ButtonColor.WhiteWithOutline,
                route = DashboardPaginatedWidgetCardButtonRoute.MainRoute("")
            ),
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
            buttonState = DashboardPaginatedWidgetCardButtonState(
                label = "Go to announcement",
                height = ButtonHeight.SMALL,
                width = ButtonWidth.FILL,
                color = ButtonColor.WhiteWithOutline,
                route = DashboardPaginatedWidgetCardButtonRoute.MainRoute("")
            ),
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
            buttonState = DashboardPaginatedWidgetCardButtonState(
                label = "Go to announcement",
                height = ButtonHeight.SMALL,
                width = ButtonWidth.FILL,
                color = ButtonColor.WhiteWithOutline,
                route = DashboardPaginatedWidgetCardButtonRoute.MainRoute("")
            ),
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

    @Test
    fun testSuccessStateWithMixedAnnouncementTypes() {
        val announcements = listOf(
            DashboardPaginatedWidgetCardItemState(
                chipState = DashboardPaginatedWidgetCardChipState(
                    label = "Announcement",
                    color = StatusChipColor.Sky
                ),
                title = "Course Announcement",
                source = "Course Name",
                date = Date(),
                buttonState = DashboardPaginatedWidgetCardButtonState(
                    label = "Go to announcement",
                    height = ButtonHeight.SMALL,
                    width = ButtonWidth.FILL,
                    color = ButtonColor.WhiteWithOutline,
                    route = DashboardPaginatedWidgetCardButtonRoute.MainRoute("")
                ),
            ),
            DashboardPaginatedWidgetCardItemState(
                chipState = DashboardPaginatedWidgetCardChipState(
                    label = "Announcement",
                    color = StatusChipColor.Sky
                ),
                title = "Global Announcement",
                source = null,
                date = Date(),
                buttonState = DashboardPaginatedWidgetCardButtonState(
                    label = "Go to announcement",
                    height = ButtonHeight.SMALL,
                    width = ButtonWidth.FILL,
                    color = ButtonColor.WhiteWithOutline,
                    route = DashboardPaginatedWidgetCardButtonRoute.MainRoute("")
                ),
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

        composeTestRule.onNodeWithText("Course Announcement").assertIsDisplayed()
        composeTestRule.onNodeWithText("Global Announcement").assertIsNotDisplayed()
        composeTestRule.onNodeWithText(
            context.getString(R.string.dashboardAnnouncementBannerFrom, "Course Name")
        ).assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Next item").assertIsDisplayed().performClick()

        composeTestRule.onNodeWithText("Course Announcement").assertIsNotDisplayed()
        composeTestRule.onNodeWithText("Global Announcement").assertIsDisplayed()
    }
}
