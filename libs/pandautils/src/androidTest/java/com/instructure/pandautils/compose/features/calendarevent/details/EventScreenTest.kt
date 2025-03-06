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
 *
 */

package com.instructure.pandautils.compose.features.calendarevent.details

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.pandautils.features.calendarevent.details.EventUiState
import com.instructure.pandautils.features.calendarevent.details.ToolbarUiState
import com.instructure.pandautils.features.calendarevent.details.composables.EventScreen
import com.instructure.pandautils.features.reminder.ReminderItem
import com.instructure.pandautils.features.reminder.ReminderViewState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date


@RunWith(AndroidJUnit4::class)
class EventScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun assertToolbar() {
        composeTestRule.setContent {
            EventScreen(
                title = "Event",
                eventUiState = EventUiState(),
                actionHandler = {},
                navigationAction = {},
                applyOnWebView = {}
            )
        }

        val toolbar = composeTestRule.onNodeWithTag("toolbar")
        toolbar.assertExists()
        composeTestRule.onNode(
            hasParent(hasTestTag("toolbar")).and(hasText("Event"))
        ).assertIsDisplayed()
        val backButton = composeTestRule.onNode(
            hasParent(hasTestTag("toolbar")).and(hasContentDescription("Back"))
        )
        backButton
            .assertIsDisplayed()
            .assertHasClickAction()
        val overflowButton = composeTestRule.onNode(
            hasParent(hasTestTag("toolbar")).and(
                hasContentDescription("More options")
            )
        )
        overflowButton.assertIsNotDisplayed()
    }

    @Test
    fun assertToolbarActions() {
        composeTestRule.setContent {
            EventScreen(
                title = "Event",
                eventUiState = EventUiState(
                    toolbarUiState = ToolbarUiState(
                        editAllowed = true,
                        deleteAllowed = true
                    )
                ),
                actionHandler = {},
                navigationAction = {},
                applyOnWebView = {}
            )
        }

        composeTestRule.onNodeWithTag("toolbar").assertExists()
        val overflowButton =
            composeTestRule.onNode(hasParent(hasTestTag("toolbar")).and(hasContentDescription("More options")))
        overflowButton
            .assertIsDisplayed()
            .performClick()
        composeTestRule.waitForIdle()
        val editButton = composeTestRule.onNode(hasText("Edit"))
        val deleteButton = composeTestRule.onNode(hasText("Delete"))
        editButton
            .assertIsDisplayed()
            .assertHasClickAction()
        deleteButton
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick()

        composeTestRule.onNode(hasText("Delete Event?")).assertIsDisplayed()
        composeTestRule.onNode(hasText("This will permanently delete your event."))
            .assertIsDisplayed()
        val cancelDelete = composeTestRule.onNode(hasText("Cancel"))
        cancelDelete
            .assertIsDisplayed()
            .assertHasClickAction()
        val confirmDelete = composeTestRule.onNode(hasText("Delete"))
        confirmDelete
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun assertCanvasContext() {
        composeTestRule.setContent {
            EventScreen(
                title = "Event",
                eventUiState = EventUiState(
                    toolbarUiState = ToolbarUiState(
                        toolbarColor = 0x000000,
                        subtitle = "Canvas Context"
                    )
                ),
                actionHandler = {},
                navigationAction = {},
                applyOnWebView = {}
            )
        }

        composeTestRule.onNode(hasText("Canvas Context")).assertIsDisplayed()
    }

    @Test
    fun assertTitle() {
        composeTestRule.setContent {
            EventScreen(
                title = "Event",
                eventUiState = EventUiState(
                    title = "Event title"
                ),
                actionHandler = {},
                navigationAction = {},
                applyOnWebView = {}
            )
        }

        composeTestRule.onNode(hasText("Event title")).assertIsDisplayed()
    }

    @Test
    fun assertDate() {
        var dateText = ""
        composeTestRule.setContent {
            dateText = DateHelper.getFormattedDate(LocalContext.current, Date()).orEmpty()
            EventScreen(
                title = "Event",
                eventUiState = EventUiState(
                    date = dateText
                ),
                actionHandler = {},
                navigationAction = {},
                applyOnWebView = {}
            )
        }

        composeTestRule.onNode(hasText(dateText)).assertIsDisplayed()
    }

    @Test
    fun assertRecurrence() {
        composeTestRule.setContent {
            EventScreen(
                title = "Event",
                eventUiState = EventUiState(
                    recurrence = "Daily, 365 times"
                ),
                actionHandler = {},
                navigationAction = {},
                applyOnWebView = {}
            )
        }

        composeTestRule.onNode(hasText("Daily, 365 times")).assertIsDisplayed()
    }

    @Test
    fun assertLocation() {
        composeTestRule.setContent {
            EventScreen(
                title = "Event",
                eventUiState = EventUiState(
                    location = "Test location"
                ),
                actionHandler = {},
                navigationAction = {},
                applyOnWebView = {}
            )
        }

        composeTestRule.onNode(hasText("Test location")).assertIsDisplayed()
    }

    @Test
    fun assertAddress() {
        composeTestRule.setContent {
            EventScreen(
                title = "Event",
                eventUiState = EventUiState(
                    address = "Test address"
                ),
                actionHandler = {},
                navigationAction = {},
                applyOnWebView = {}
            )
        }

        composeTestRule.onNode(hasText("Test address")).assertIsDisplayed()
    }

    @Test
    fun assertSnackbar() {
        composeTestRule.setContent {
            EventScreen(
                title = "Event",
                eventUiState = EventUiState(
                    errorSnack = "Error message"
                ),
                actionHandler = {},
                navigationAction = {},
                applyOnWebView = {}
            )
        }

        composeTestRule.onNode(hasText("Error message")).assertIsDisplayed()
    }

    @Test
    fun assertReminder() {
        composeTestRule.setContent {
            EventScreen(
                title = "Event",
                eventUiState = EventUiState(
                    title = "Event title",
                    reminderUiState = ReminderViewState(
                        reminders = listOf(
                            ReminderItem(
                                id = 1,
                                title = "Reminder title",
                                date = Date()
                            )
                        )
                    )
                ),
                actionHandler = {},
                navigationAction = {},
                applyOnWebView = {}
            )
        }

        composeTestRule.onNode(hasText("Reminder")).assertIsDisplayed()
        composeTestRule.onNode(hasText("Reminder title")).assertIsDisplayed()
        composeTestRule.onNode(hasContentDescription("Add reminder")).assertIsDisplayed()
    }

    @Test
    fun assertMessageFab() {
        composeTestRule.setContent {
            EventScreen(
                title = "Event",
                eventUiState = EventUiState(
                    isMessageFabEnabled = true
                ),
                actionHandler = {},
                navigationAction = {},
                applyOnWebView = {}
            )
        }

        composeTestRule.onNode(hasContentDescription("Send a message about this event"))
            .assertIsDisplayed()
            .assertHasClickAction()
    }
}
