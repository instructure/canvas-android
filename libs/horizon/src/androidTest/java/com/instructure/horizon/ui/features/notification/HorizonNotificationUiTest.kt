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
package com.instructure.horizon.ui.features.notification

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.horizon.features.notification.NotificationItem
import com.instructure.horizon.features.notification.NotificationItemCategory
import com.instructure.horizon.features.notification.NotificationRoute
import com.instructure.horizon.features.notification.NotificationScreen
import com.instructure.horizon.features.notification.NotificationUiState
import com.instructure.horizon.horizonui.molecules.StatusChipColor
import com.instructure.horizon.horizonui.platform.LoadingState
import com.instructure.pandautils.utils.localisedFormat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class HorizonNotificationUiTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testLoadingState() {
        val state = NotificationUiState(
            screenState = LoadingState(isLoading = true),
        )
        composeTestRule.setContent {
            NotificationScreen(state, rememberNavController())
        }

        composeTestRule.onNodeWithText("Notifications")
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("LoadingSpinner")
            .assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Navigate back")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun testContentStateTodayFormat() {
        val state = NotificationUiState(
            screenState = LoadingState(),
            notificationItems = listOf(
                NotificationItem(
                    category = NotificationItemCategory("Announcement", StatusChipColor.Sky),
                    courseLabel = "Biology 101",
                    title = "New Announcement",
                    date = Date(),
                    isRead = false,
                    route = NotificationRoute.DeepLink("")
                )
            )
        )
        composeTestRule.setContent {
            NotificationScreen(state, rememberNavController())
        }

        composeTestRule.onNodeWithText("Notifications")
            .assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Navigate back")
            .assertIsDisplayed()
            .assertHasClickAction()

        composeTestRule.onNodeWithText("Announcement")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("New Announcement")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Biology 101")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Today")
            .assertIsDisplayed()
    }

    @Test
    fun testContentStateYesterdayFormat() {
        val state = NotificationUiState(
            screenState = LoadingState(),
            notificationItems = listOf(
                NotificationItem(
                    category = NotificationItemCategory("Announcement", StatusChipColor.Sky),
                    courseLabel = "Biology 101",
                    title = "New Announcement",
                    date = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -1) }.time,
                    isRead = false,
                    route = NotificationRoute.DeepLink("")
                )
            )
        )
        composeTestRule.setContent {
            NotificationScreen(state, rememberNavController())
        }

        composeTestRule.onNodeWithText("Notifications")
            .assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Navigate back")
            .assertIsDisplayed()
            .assertHasClickAction()

        composeTestRule.onNodeWithText("Announcement")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("New Announcement")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Biology 101")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Yesterday")
            .assertIsDisplayed()
    }

    @Test
    fun testContentStateDayOfWeekFormat() {
        val date = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -3) }.time
        val state = NotificationUiState(
            screenState = LoadingState(),
            notificationItems = listOf(
                NotificationItem(
                    category = NotificationItemCategory("Announcement", StatusChipColor.Sky),
                    courseLabel = "Biology 101",
                    title = "New Announcement",
                    date = date,
                    isRead = false,
                    route = NotificationRoute.DeepLink("")
                )
            )
        )
        composeTestRule.setContent {
            NotificationScreen(state, rememberNavController())
        }
        val dateString = SimpleDateFormat("EEEE", Locale.getDefault()).format(date)

        composeTestRule.onNodeWithText("Notifications")
            .assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Navigate back")
            .assertIsDisplayed()
            .assertHasClickAction()

        composeTestRule.onNodeWithText("Announcement")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("New Announcement")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Biology 101")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(dateString)
            .assertIsDisplayed()
    }

    @Test
    fun testContentStateDateFormat() {
        val date = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -7) }.time
        val state = NotificationUiState(
            screenState = LoadingState(),
            notificationItems = listOf(
                NotificationItem(
                    category = NotificationItemCategory("Announcement", StatusChipColor.Sky),
                    courseLabel = "Biology 101",
                    title = "New Announcement",
                    date = date,
                    isRead = false,
                    route = NotificationRoute.DeepLink("")
                )
            )
        )
        composeTestRule.setContent {
            NotificationScreen(state, rememberNavController())
        }
        val dateString = date.localisedFormat("MMM dd, yyyy")

        composeTestRule.onNodeWithText("Notifications")
            .assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Navigate back")
            .assertIsDisplayed()
            .assertHasClickAction()

        composeTestRule.onNodeWithText("Announcement")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("New Announcement")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Biology 101")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(dateString)
            .assertIsDisplayed()
    }
}