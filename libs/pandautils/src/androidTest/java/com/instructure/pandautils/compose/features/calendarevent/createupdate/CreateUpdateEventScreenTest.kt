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

package com.instructure.pandautils.compose.features.calendarevent.createupdate

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onChildAt
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.User
import com.instructure.pandautils.compose.composables.SelectCalendarUiState
import com.instructure.pandautils.features.calendarevent.createupdate.CreateUpdateEventUiState
import com.instructure.pandautils.features.calendarevent.createupdate.SelectFrequencyUiState
import com.instructure.pandautils.features.calendarevent.createupdate.composables.CreateUpdateEventScreen
import com.jakewharton.threetenabp.AndroidThreeTen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime


@RunWith(AndroidJUnit4::class)
class CreateUpdateEventScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun assertToolbar() {
        composeTestRule.setContent {
            AndroidThreeTen.init(LocalContext.current)
            CreateUpdateEventScreen(
                title = "New Event",
                actionHandler = {},
                uiState = CreateUpdateEventUiState()
            )
        }

        val toolbar = composeTestRule.onNodeWithTag("Toolbar")
        toolbar.assertExists()
        composeTestRule.onNode(
            hasParent(hasTestTag("Toolbar")).and(hasText("New Event"))
        ).assertIsDisplayed()
        val backButton = composeTestRule.onNode(
            hasParent(hasTestTag("Toolbar")).and(hasContentDescription("Close"))
        )
        backButton
            .assertIsDisplayed()
            .assertHasClickAction()
        val saveButton = composeTestRule.onNode(hasText("Save"))
        saveButton
            .assertIsDisplayed()
            .assertHasClickAction()
            .assertIsNotEnabled()
    }

    @Test
    fun assertHints() {
        composeTestRule.setContent {
            AndroidThreeTen.init(LocalContext.current)
            CreateUpdateEventScreen(
                title = "New Event",
                actionHandler = {},
                uiState = CreateUpdateEventUiState()
            )
        }

        composeTestRule.onNodeWithText("Add title").assertIsDisplayed()
        composeTestRule.onNodeWithText("Date").assertIsDisplayed()
        composeTestRule.onNodeWithText("From").assertIsDisplayed()
        composeTestRule.onNodeWithText("Start Time").assertIsDisplayed()
        composeTestRule.onNodeWithText("To").assertIsDisplayed()
        composeTestRule.onNodeWithText("End Time").assertIsDisplayed()
        composeTestRule.onNodeWithText("Frequency").assertIsDisplayed()
        composeTestRule.onNodeWithText("Calendar").assertIsDisplayed()
        composeTestRule.onNodeWithText("Location").assertIsDisplayed()
        composeTestRule.onNodeWithText("Address").assertIsDisplayed()
        composeTestRule.onNodeWithText("Details").assertIsDisplayed()
    }

    @Test
    fun assertSaveProgress() {
        composeTestRule.setContent {
            AndroidThreeTen.init(LocalContext.current)
            CreateUpdateEventScreen(
                title = "New Event",
                actionHandler = {},
                uiState = CreateUpdateEventUiState(saving = true)
            )
        }

        composeTestRule.onNodeWithTag("savingProgressIndicator").assertIsDisplayed()
    }

    @Test
    fun assertTitle() {
        composeTestRule.setContent {
            AndroidThreeTen.init(LocalContext.current)
            CreateUpdateEventScreen(
                title = "New Event",
                actionHandler = {},
                uiState = CreateUpdateEventUiState(
                    title = "Title"
                )
            )
        }

        composeTestRule.onNodeWithTag("addTitleField")
            .assertIsDisplayed()
            .assertTextEquals("Title")
    }

    @Test
    fun assertDate() {
        composeTestRule.setContent {
            AndroidThreeTen.init(LocalContext.current)
            CreateUpdateEventScreen(
                title = "New Event",
                actionHandler = {},
                uiState = CreateUpdateEventUiState(
                    date = LocalDate.of(2024, 1, 5)
                )
            )
        }

        composeTestRule.onNodeWithText("Jan 5").assertIsDisplayed()
    }

    @Test
    fun assertStartTime() {
        composeTestRule.setContent {
            AndroidThreeTen.init(LocalContext.current)
            CreateUpdateEventScreen(
                title = "New Event",
                actionHandler = {},
                uiState = CreateUpdateEventUiState(
                    startTime = LocalTime.of(10, 30)
                )
            )
        }

        composeTestRule.onNodeWithText("10:30 AM").assertIsDisplayed()
    }

    @Test
    fun assertEndTime() {
        composeTestRule.setContent {
            AndroidThreeTen.init(LocalContext.current)
            CreateUpdateEventScreen(
                title = "New Event",
                actionHandler = {},
                uiState = CreateUpdateEventUiState(
                    startTime = LocalTime.of(11, 30)
                )
            )
        }

        composeTestRule.onNodeWithText("11:30 AM").assertIsDisplayed()
    }

    @Test
    fun assertFrequency() {
        composeTestRule.setContent {
            AndroidThreeTen.init(LocalContext.current)
            CreateUpdateEventScreen(
                title = "New Event",
                actionHandler = {},
                uiState = CreateUpdateEventUiState(
                    selectFrequencyUiState = SelectFrequencyUiState(
                        selectedFrequency = "Daily",
                        frequencies = mapOf("Daily" to null)
                    )
                )
            )
        }

        composeTestRule.onNodeWithText("Daily").assertIsDisplayed()
    }

    @Test
    fun assertFrequencyDialog() {
        composeTestRule.setContent {
            AndroidThreeTen.init(LocalContext.current)
            CreateUpdateEventScreen(
                title = "New Event",
                actionHandler = {},
                uiState = CreateUpdateEventUiState(
                    selectFrequencyUiState = SelectFrequencyUiState(
                        selectedFrequency = "Daily",
                        frequencies = mapOf(
                            "Does Not Repeat" to null,
                            "Daily" to null
                        ),
                        showFrequencyDialog = true
                    )
                )
            )
        }

        val dialog = composeTestRule.onNodeWithTag("SingleChoiceAlertDialog").assertIsDisplayed()
        dialog.onChildAt(0).assert(hasText("Frequency")).assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel")
            .assertIsDisplayed()
            .assertHasClickAction()
        composeTestRule.onNodeWithText("Does Not Repeat")
        composeTestRule.onNodeWithText("Daily")
    }

    @Test
    fun assertCalendar() {
        composeTestRule.setContent {
            AndroidThreeTen.init(LocalContext.current)
            CreateUpdateEventScreen(
                title = "New Event",
                actionHandler = {},
                uiState = CreateUpdateEventUiState(
                    selectCalendarUiState = SelectCalendarUiState(
                        selectedCanvasContext = CanvasContext.currentUserContext(User(name = "User Name"))
                    )
                )
            )
        }

        composeTestRule.onNodeWithText("User Name").assertIsDisplayed()
    }

    @Test
    fun assertLocation() {
        composeTestRule.setContent {
            AndroidThreeTen.init(LocalContext.current)
            CreateUpdateEventScreen(
                title = "New Event",
                actionHandler = {},
                uiState = CreateUpdateEventUiState(
                    location = "Location Name"
                )
            )
        }

        composeTestRule.onNodeWithText("Location Name").assertIsDisplayed()
    }

    @Test
    fun assertAddress() {
        composeTestRule.setContent {
            AndroidThreeTen.init(LocalContext.current)
            CreateUpdateEventScreen(
                title = "New Event",
                actionHandler = {},
                uiState = CreateUpdateEventUiState(
                    address = "Address Name"
                )
            )
        }

        composeTestRule.onNodeWithText("Address Name").assertIsDisplayed()
    }

    @Test
    fun assertSnackbar() {
        composeTestRule.setContent {
            AndroidThreeTen.init(LocalContext.current)
            CreateUpdateEventScreen(
                title = "New Event",
                actionHandler = {},
                uiState = CreateUpdateEventUiState(
                    errorSnack = "Error message"
                )
            )
        }

        composeTestRule.onNode(hasText("Error message")).assertIsDisplayed()
    }
}
