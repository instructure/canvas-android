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
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.AlertType
import com.instructure.parentapp.features.alerts.list.AlertsItemUiState
import com.instructure.parentapp.features.alerts.list.AlertsScreen
import com.instructure.parentapp.features.alerts.list.AlertsUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.Locale

@ExperimentalMaterialApi
@RunWith(AndroidJUnit4::class)
class AlertsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun assertError() {
        composeTestRule.setContent {
            AlertsScreen(uiState = AlertsUiState(
                alerts = emptyList(),
                isLoading = false,
                isError = true,
                isRefreshing = false,
                studentColor = Color.BLUE,
            ), actionHandler = {})
        }


        composeTestRule.onNodeWithText("There was an error loading your student’s alerts.")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertHasClickAction().assertIsDisplayed()
    }

    @Test
    fun assertLoading() {
        composeTestRule.setContent {
            AlertsScreen(uiState = AlertsUiState(
                alerts = emptyList(),
                isLoading = true,
                isError = false,
                isRefreshing = false,
                studentColor = Color.BLUE,
            ), actionHandler = {})
        }

        composeTestRule.onNodeWithTag("loading").assertIsDisplayed()
    }

    @Test
    fun assertEmpty() {
        composeTestRule.setContent {
            AlertsScreen(uiState = AlertsUiState(
                alerts = emptyList(),
                isLoading = false,
                isError = false,
                isRefreshing = false,
                studentColor = Color.BLUE,
            ), actionHandler = {})
        }

        composeTestRule.onNodeWithText("No Alerts").assertIsDisplayed()
        composeTestRule.onNodeWithText("There's nothing to be notified of yet.").assertIsDisplayed()
    }

    @Test
    fun assertRefreshing() {
        composeTestRule.setContent {
            AlertsScreen(uiState = AlertsUiState(
                alerts = emptyList(),
                isLoading = false,
                isError = false,
                isRefreshing = true,
                studentColor = Color.BLUE,
            ), actionHandler = {})
        }

        composeTestRule.onNodeWithTag("pullRefreshIndicator").assertIsDisplayed()
    }

    @Test
    fun assertContent() {
        val items = listOf(
            AlertsItemUiState(
                alertId = 1,
                contextId = 10,
                title = "Alert 1",
                alertType = AlertType.ASSIGNMENT_GRADE_LOW,
                date = Date.from(Instant.parse("2023-09-15T09:02:00Z")),
                observerAlertThreshold = "20%",
                lockedForUser = false,
                unread = false,
                htmlUrl = null
            ),
            AlertsItemUiState(
                alertId = 2,
                contextId = 20,
                title = "Alert 2",
                alertType = AlertType.ASSIGNMENT_GRADE_HIGH,
                date = Date.from(Instant.parse("2023-09-16T09:02:00Z")),
                observerAlertThreshold = "80%",
                lockedForUser = false,
                unread = true,
                htmlUrl = null
            ),
            AlertsItemUiState(
                alertId = 3,
                contextId = 30,
                title = "Alert 3",
                alertType = AlertType.COURSE_GRADE_LOW,
                date = Date.from(Instant.parse("2023-09-17T09:02:00Z")),
                observerAlertThreshold = "20%",
                lockedForUser = false,
                unread = false,
                htmlUrl = null
            ),
            AlertsItemUiState(
                alertId = 4,
                contextId = 40,
                title = "Alert 4",
                alertType = AlertType.COURSE_GRADE_HIGH,
                date = Date.from(Instant.parse("2023-09-18T09:02:00Z")),
                observerAlertThreshold = "50%",
                lockedForUser = false,
                unread = true,
                htmlUrl = null
            ),
            AlertsItemUiState(
                alertId = 5,
                contextId = 50,
                title = "Alert 5",
                alertType = AlertType.ASSIGNMENT_MISSING,
                date = Date.from(Instant.parse("2023-09-19T09:02:00Z")),
                observerAlertThreshold = null,
                lockedForUser = false,
                unread = false,
                htmlUrl = null
            ),
            AlertsItemUiState(
                alertId = 6,
                contextId = 60,
                title = "Alert 6",
                alertType = AlertType.COURSE_ANNOUNCEMENT,
                date = Date.from(Instant.parse("2023-09-20T09:02:00Z")),
                observerAlertThreshold = null,
                lockedForUser = false,
                unread = true,
                htmlUrl = null
            ),
            AlertsItemUiState(
                alertId = 7,
                contextId = 70,
                title = "Alert 7",
                alertType = AlertType.INSTITUTION_ANNOUNCEMENT,
                date = Date.from(Instant.parse("2023-09-21T09:02:00Z")),
                observerAlertThreshold = null,
                lockedForUser = false,
                unread = false,
                htmlUrl = null
            )
        )

        composeTestRule.setContent {
            AlertsScreen(uiState = AlertsUiState(
                alerts = items,
                isLoading = false,
                isError = false,
                isRefreshing = false,
                studentColor = Color.BLUE,
            ), actionHandler = {})
        }

        items.forEach {
            composeTestRule.onNodeWithText(it.title).assertIsDisplayed()
            composeTestRule.onNodeWithText(parseAlertType(it.alertType, it.observerAlertThreshold)).assertIsDisplayed()
            composeTestRule.onNodeWithText(parseDate(it.date!!)).assertIsDisplayed()
            composeTestRule.onNode(hasAnyDescendant(hasText(it.title)).and(hasTestTag("alertItem")), useUnmergedTree = true).assertHasClickAction()
        }
    }

    private fun parseDate(date: Date): String {
        val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        return "${dateFormat.format(date)} at ${timeFormat.format(date)}"
    }

    private fun parseAlertType(alertType: AlertType, threshold: String?): String {
        return when(alertType) {
            AlertType.ASSIGNMENT_GRADE_LOW -> "Assignment Grade Below $threshold"
            AlertType.ASSIGNMENT_GRADE_HIGH -> "Assignment Grade Above $threshold"
            AlertType.COURSE_GRADE_LOW -> "Course Grade Below $threshold"
            AlertType.COURSE_GRADE_HIGH -> "Course Grade Above $threshold"
            AlertType.ASSIGNMENT_MISSING -> "Assignment missing"
            AlertType.COURSE_ANNOUNCEMENT -> "Course Announcement"
            AlertType.INSTITUTION_ANNOUNCEMENT -> "Global Announcement"
            else -> "Unknown"
        }
    }

}