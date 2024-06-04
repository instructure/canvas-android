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

package com.instructure.pandautils.compose.features.calendartodo.createupdate

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
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
import com.instructure.pandautils.compose.composables.SelectCalendarUiState
import com.instructure.pandautils.features.calendartodo.createupdate.CreateUpdateToDoUiState
import com.instructure.pandautils.features.calendartodo.createupdate.composables.CreateUpdateToDoScreenWrapper
import com.jakewharton.threetenabp.AndroidThreeTen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime


@RunWith(AndroidJUnit4::class)
class CreateUpdateToDoScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun assertToolbar() {
        composeTestRule.setContent {
            AndroidThreeTen.init(LocalContext.current)
            CreateUpdateToDoScreenWrapper(
                title = "New To Do",
                actionHandler = {},
                uiState = CreateUpdateToDoUiState()
            )
        }

        val toolbar = composeTestRule.onNodeWithTag("toolbar")
        toolbar.assertExists()
        composeTestRule.onNode(
            hasParent(hasTestTag("toolbar")).and(hasText("New To Do"))
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
            CreateUpdateToDoScreenWrapper(
                title = "New To Do",
                actionHandler = {},
                uiState = CreateUpdateToDoUiState()
            )
        }

        composeTestRule.onNode(hasText("Add title"))
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNode(hasText("Date"))
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNode(hasText("Time"))
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNode(hasText("Calendar"))
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
            CreateUpdateToDoScreenWrapper(
                title = "New To Do",
                actionHandler = {},
                uiState = CreateUpdateToDoUiState(
                    saving = true
                )
            )
        }

        composeTestRule.onNodeWithTag("savingProgressIndicator").assertIsDisplayed()
    }

    @Test
    fun assertTitle() {
        composeTestRule.setContent {
            AndroidThreeTen.init(LocalContext.current)
            CreateUpdateToDoScreenWrapper(
                title = "New To Do",
                actionHandler = {},
                uiState = CreateUpdateToDoUiState(
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
            CreateUpdateToDoScreenWrapper(
                title = "New To Do",
                actionHandler = {},
                uiState = CreateUpdateToDoUiState(
                    date = LocalDate.of(2024, 1, 5)
                )
            )
        }

        composeTestRule.onNode(hasText("Jan 5"))
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun assertTime() {
        composeTestRule.setContent {
            AndroidThreeTen.init(LocalContext.current)
            CreateUpdateToDoScreenWrapper(
                title = "New To Do",
                actionHandler = {},
                uiState = CreateUpdateToDoUiState(
                    time = LocalTime.of(10, 30)
                )
            )
        }

        composeTestRule.onNode(hasText("10:30 AM"))
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun assertCanvasContext() {
        composeTestRule.setContent {
            AndroidThreeTen.init(LocalContext.current)
            CreateUpdateToDoScreenWrapper(
                title = "New To Do",
                actionHandler = {},
                uiState = CreateUpdateToDoUiState(
                    selectCalendarUiState = SelectCalendarUiState(
                        selectedCanvasContext = CanvasContext.currentUserContext(User(name = "User Name"))
                    )
                )
            )
        }

        composeTestRule.onNode(hasText("User Name")).assertIsDisplayed()
    }

    @Test
    fun assertDetails() {
        composeTestRule.setContent {
            AndroidThreeTen.init(LocalContext.current)
            CreateUpdateToDoScreenWrapper(
                title = "New To Do",
                actionHandler = {},
                uiState = CreateUpdateToDoUiState(
                    details = "Details text"
                )
            )
        }

        composeTestRule.onNode(hasText("Details text"))
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun assertUnsavedChangesDialog() {
        composeTestRule.setContent {
            AndroidThreeTen.init(LocalContext.current)
            CreateUpdateToDoScreenWrapper(
                title = "New To Do",
                actionHandler = {},
                uiState = CreateUpdateToDoUiState(
                    showUnsavedChangesDialog = true
                )
            )
        }

        composeTestRule.onNode(hasText("Exit without saving?")).assertIsDisplayed()
    }
}
