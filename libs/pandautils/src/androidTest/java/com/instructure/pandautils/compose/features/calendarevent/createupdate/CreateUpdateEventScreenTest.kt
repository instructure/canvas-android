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
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.User
import com.instructure.pandautils.compose.composables.SelectContextUiState
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

        val toolbar = composeTestRule.onNodeWithTag("toolbar")
        toolbar.assertExists()
        composeTestRule.onNode(
            hasParent(hasTestTag("toolbar")).and(hasText("New Event"))
        ).assertIsDisplayed()
        val backButton = composeTestRule.onNode(
            hasParent(hasTestTag("toolbar")).and(hasContentDescription("Close"))
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

        composeTestRule.onNode(hasText("Add title"))
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNode(hasText("Date"))
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNode(hasText("From"))
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNode(hasText("Start Time"))
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNode(hasText("To"))
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNode(hasText("End Time"))
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNode(hasText("Frequency"))
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNode(hasText("Calendar"))
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNode(hasText("Location"))
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNode(hasText("Address"))
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNode(hasText("Details"))
            .performScrollTo()
            .assertIsDisplayed()
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
            .performScrollTo()
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

        composeTestRule.onNode(hasText("Jan 5"))
            .performScrollTo()
            .assertIsDisplayed()
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

        composeTestRule.onNode(hasText("10:30 AM"))
            .performScrollTo()
            .assertIsDisplayed()
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

        composeTestRule.onNode(hasText("11:30 AM"))
            .performScrollTo()
            .assertIsDisplayed()
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

        composeTestRule.onNode(hasText("Daily")).assertIsDisplayed()
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

        composeTestRule.onNodeWithTag("singleChoiceAlertDialog").assertIsDisplayed()
        val matcher = hasAnyAncestor(hasTestTag("singleChoiceAlertDialog"))
        composeTestRule.onNode(matcher.and(hasText("Frequency"))).assertIsDisplayed()
        composeTestRule.onNode(matcher.and(hasText("Cancel")))
            .assertIsDisplayed()
            .assertHasClickAction()
        composeTestRule.onNode(matcher.and(hasText("Does Not Repeat")), true).assertIsDisplayed()
        composeTestRule.onNode(matcher.and(hasText("Daily")), true).assertIsDisplayed()
    }

    @Test
    fun assertCalendar() {
        composeTestRule.setContent {
            AndroidThreeTen.init(LocalContext.current)
            CreateUpdateEventScreen(
                title = "New Event",
                actionHandler = {},
                uiState = CreateUpdateEventUiState(
                    selectContextUiState = SelectContextUiState(
                        selectedCanvasContext = CanvasContext.currentUserContext(User(name = "User Name"))
                    )
                )
            )
        }

        composeTestRule.onNode(hasText("User Name")).assertIsDisplayed()
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

        composeTestRule.onNode(hasText("Location Name")).assertIsDisplayed()
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

        composeTestRule.onNode(hasText("Address Name")).assertIsDisplayed()
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
