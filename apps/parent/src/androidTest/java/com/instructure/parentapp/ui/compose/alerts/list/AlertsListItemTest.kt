/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.parentapp.ui.compose.alerts.list

import android.graphics.Color
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.AlertType
import com.instructure.composeTest.hasDrawable
import com.instructure.parentapp.R
import com.instructure.parentapp.features.alerts.list.AlertsItemUiState
import com.instructure.parentapp.features.alerts.list.AlertsListItem
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class AlertsListItemTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun assertAssignmentMissing() {
        composeTestRule.setContent {
            AlertsListItem(alert = AlertsItemUiState(
                alertId = 1L,
                contextId = 10L,
                title = "Assignment Missing title",
                alertType = AlertType.ASSIGNMENT_MISSING,
                date = Date(),
                observerAlertThreshold = null,
                lockedForUser = false,
                unread = true,
                htmlUrl = null
            ), userColor = Color.BLUE, actionHandler = {})
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Assignment missing").assertIsDisplayed()
        composeTestRule.onNodeWithText("Assignment Missing title").assertIsDisplayed()
        composeTestRule.onNode(hasDrawable(R.drawable.ic_warning)).assertIsDisplayed()
        composeTestRule.onNodeWithText(parseDate(Date())).assertIsDisplayed()
        composeTestRule.onNodeWithTag("alertItem").assertHasClickAction()
    }

    @Test
    fun assertAssignmentGradeHigh() {
        composeTestRule.setContent {
            AlertsListItem(alert = AlertsItemUiState(
                alertId = 1L,
                contextId = 10L,
                title = "Assignment Grade High title",
                alertType = AlertType.ASSIGNMENT_GRADE_HIGH,
                date = Date(),
                observerAlertThreshold = "90%",
                lockedForUser = false,
                unread = true,
                htmlUrl = null
            ), userColor = Color.BLUE, actionHandler = {})
        }

        composeTestRule.onNodeWithText("Assignment Grade Above 90%").assertIsDisplayed()
        composeTestRule.onNodeWithText("Assignment Grade High title").assertIsDisplayed()
        composeTestRule.onNode(hasDrawable(R.drawable.ic_info)).assertIsDisplayed()
        composeTestRule.onNodeWithText(parseDate(Date())).assertIsDisplayed()
        composeTestRule.onNodeWithTag("alertItem").assertHasClickAction()
    }

    @Test
    fun assertAssignmentGradeLow() {
        composeTestRule.setContent {
            AlertsListItem(alert = AlertsItemUiState(
                alertId = 1L,
                contextId = 10L,
                title = "Assignment Grade Low title",
                alertType = AlertType.ASSIGNMENT_GRADE_LOW,
                date = Date(),
                observerAlertThreshold = "60%",
                lockedForUser = false,
                unread = true,
                htmlUrl = null
            ), userColor = Color.BLUE, actionHandler = {})
        }

        composeTestRule.onNodeWithText("Assignment Grade Below 60%").assertIsDisplayed()
        composeTestRule.onNodeWithText("Assignment Grade Low title").assertIsDisplayed()
        composeTestRule.onNode(hasDrawable(R.drawable.ic_warning)).assertIsDisplayed()
        composeTestRule.onNodeWithText(parseDate(Date())).assertIsDisplayed()
        composeTestRule.onNodeWithTag("alertItem").assertHasClickAction()
    }

    @Test
    fun assertCourseGradeHigh() {
        composeTestRule.setContent {
            AlertsListItem(alert = AlertsItemUiState(
                alertId = 1L,
                contextId = 10L,
                title = "Course Grade High title",
                alertType = AlertType.COURSE_GRADE_HIGH,
                date = Date(),
                observerAlertThreshold = "90%",
                lockedForUser = false,
                unread = true,
                htmlUrl = null
            ), userColor = Color.BLUE, actionHandler = {})
        }

        composeTestRule.onNodeWithText("Course Grade Above 90%").assertIsDisplayed()
        composeTestRule.onNodeWithText("Course Grade High title").assertIsDisplayed()
        composeTestRule.onNode(hasDrawable(R.drawable.ic_info)).assertIsDisplayed()
        composeTestRule.onNodeWithText(parseDate(Date())).assertIsDisplayed()
        composeTestRule.onNodeWithTag("alertItem").assertHasClickAction()
    }

    @Test
    fun assertCourseGradeLow() {
        composeTestRule.setContent {
            AlertsListItem(alert = AlertsItemUiState(
                alertId = 1L,
                contextId = 10L,
                title = "Course Grade Low title",
                alertType = AlertType.COURSE_GRADE_LOW,
                date = Date(),
                observerAlertThreshold = "60%",
                lockedForUser = false,
                unread = true,
                htmlUrl = null
            ), userColor = Color.BLUE, actionHandler = {})
        }

        composeTestRule.onNodeWithText("Course Grade Below 60%").assertIsDisplayed()
        composeTestRule.onNodeWithText("Course Grade Low title").assertIsDisplayed()
        composeTestRule.onNode(hasDrawable(R.drawable.ic_warning)).assertIsDisplayed()
        composeTestRule.onNodeWithText(parseDate(Date())).assertIsDisplayed()
        composeTestRule.onNodeWithTag("alertItem").assertHasClickAction()
    }

    @Test
    fun assertCourseAnnouncement() {
        composeTestRule.setContent {
            AlertsListItem(alert = AlertsItemUiState(
                alertId = 1L,
                contextId = 10L,
                title = "Course Announcement title",
                alertType = AlertType.COURSE_ANNOUNCEMENT,
                date = Date(),
                observerAlertThreshold = null,
                lockedForUser = false,
                unread = true,
                htmlUrl = null
            ), userColor = Color.BLUE, actionHandler = {})
        }

        composeTestRule.onNodeWithText("Course Announcement").assertIsDisplayed()
        composeTestRule.onNodeWithText("Course Announcement title").assertIsDisplayed()
        composeTestRule.onNode(hasDrawable(R.drawable.ic_info)).assertIsDisplayed()
        composeTestRule.onNodeWithText(parseDate(Date())).assertIsDisplayed()
        composeTestRule.onNodeWithTag("alertItem").assertHasClickAction()
    }

    @Test
    fun assertInstitutionAnnouncement() {
        composeTestRule.setContent {
            AlertsListItem(alert = AlertsItemUiState(
                alertId = 1L,
                contextId = 10L,
                title = "Global Announcement title",
                alertType = AlertType.INSTITUTION_ANNOUNCEMENT,
                date = Date(),
                observerAlertThreshold = null,
                lockedForUser = false,
                unread = true,
                htmlUrl = null
            ), userColor = Color.BLUE, actionHandler = {})
        }

        composeTestRule.onNodeWithText("Global Announcement").assertIsDisplayed()
        composeTestRule.onNodeWithText("Global Announcement title").assertIsDisplayed()
        composeTestRule.onNode(hasDrawable(R.drawable.ic_info)).assertIsDisplayed()
        composeTestRule.onNodeWithText(parseDate(Date())).assertIsDisplayed()
        composeTestRule.onNodeWithTag("alertItem").assertHasClickAction()
    }

    @Test
    fun assertLockedForUser() {
        composeTestRule.setContent {
            AlertsListItem(alert = AlertsItemUiState(
                alertId = 1L,
                contextId = 10L,
                title = "Locked for User title",
                alertType = AlertType.ASSIGNMENT_MISSING,
                date = Date(),
                observerAlertThreshold = null,
                lockedForUser = true,
                unread = true,
                htmlUrl = null
            ), userColor = Color.BLUE, actionHandler = {})
        }

        composeTestRule.onNodeWithText("Assignment missing").assertIsDisplayed()
        composeTestRule.onNodeWithText("Locked for User title").assertIsDisplayed()
        composeTestRule.onNode(hasDrawable(R.drawable.ic_lock_lined)).assertIsDisplayed()
        composeTestRule.onNodeWithText(parseDate(Date())).assertIsDisplayed()
        composeTestRule.onNodeWithTag("alertItem").assertHasClickAction()
    }

    @Test
    fun assertRead() {
        composeTestRule.setContent {
            AlertsListItem(alert = AlertsItemUiState(
                alertId = 1L,
                contextId = 10L,
                title = "Global Announcement title",
                alertType = AlertType.INSTITUTION_ANNOUNCEMENT,
                date = Date(),
                observerAlertThreshold = null,
                lockedForUser = false,
                unread = false,
                htmlUrl = null
            ), userColor = Color.BLUE, actionHandler = {})
        }

        composeTestRule.onNodeWithText("Global Announcement").assertIsDisplayed()
        composeTestRule.onNodeWithText("Global Announcement title").assertIsDisplayed()
        composeTestRule.onNode(hasDrawable(R.drawable.ic_info)).assertIsDisplayed()
        composeTestRule.onNodeWithText(parseDate(Date())).assertIsDisplayed()
        composeTestRule.onNodeWithTag("alertItem").assertHasClickAction()
        composeTestRule.onNodeWithTag("unreadIndicator").assertIsNotDisplayed()
    }

    private fun parseDate(date: Date): String {
        val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        return "${dateFormat.format(date)} at ${timeFormat.format(date)}"
    }
}