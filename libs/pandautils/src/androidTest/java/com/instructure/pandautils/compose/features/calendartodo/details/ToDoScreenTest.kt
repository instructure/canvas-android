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
package com.instructure.pandautils.compose.features.calendartodo.details

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.espresso.assertTextColor
import com.instructure.pandautils.features.calendartodo.details.ToDoUiState
import com.instructure.pandautils.features.calendartodo.details.composables.ToDoScreen
import com.instructure.pandautils.features.reminder.ReminderItem
import com.instructure.pandautils.features.reminder.ReminderViewState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@OptIn(ExperimentalFoundationApi::class)
@RunWith(AndroidJUnit4::class)
class ToDoScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun assertToolbar() {
        composeTestRule.setContent {
            ToDoScreen(
                title = "To Do",
                toDoUiState = ToDoUiState(),
                actionHandler = {},
                navigationActionClick = {},
            )
        }

        val toolbar = composeTestRule.onNodeWithTag("toolbar")
        toolbar.assertExists()
        composeTestRule.onNode(hasParent(hasTestTag("toolbar")).and(hasText("To Do")))
            .assertIsDisplayed()
        val backButton =
            composeTestRule.onNode(hasParent(hasTestTag("toolbar")).and(hasContentDescription("Back")))
        backButton
            .assertIsDisplayed()
            .assertHasClickAction()
        val overflowButton =
            composeTestRule.onNode(hasParent(hasTestTag("toolbar")).and(hasContentDescription("More options")))
        overflowButton
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun assertToolbarActions() {
        composeTestRule.setContent {
            ToDoScreen(
                title = "To Do",
                toDoUiState = ToDoUiState(),
                actionHandler = {},
                navigationActionClick = {},
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

        composeTestRule.onNode(hasText("Delete To Do?")).assertIsDisplayed()
        composeTestRule.onNode(hasText("This will permanently delete your To Do item."))
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
    fun assertTitle() {
        composeTestRule.setContent {
            ToDoScreen(
                title = "Toolbar title",
                toDoUiState = ToDoUiState(
                    title = "To Do title",
                ),
                actionHandler = {},
                navigationActionClick = {},
            )
        }

        composeTestRule.onNode(hasText("To Do title")).assertIsDisplayed()
    }

    @SuppressLint("MissingColorAlphaChannel")
    @Test
    fun assertCanvasContext() {
        composeTestRule.setContent {
            ToDoScreen(
                title = "Toolbar title",
                toDoUiState = ToDoUiState(
                    title = "To Do title",
                    contextName = "Canvas Context",
                    contextColor = 0x000000,
                ),
                actionHandler = {},
                navigationActionClick = {},
            )
        }

        composeTestRule.onNode(hasText("Canvas Context"))
            .assertIsDisplayed()
            .assertTextColor(Color(0x000000))
    }

    @Test
    fun assertDate() {
        var dateTitle = ""
        composeTestRule.setContent {
            val context = LocalContext.current
            dateTitle = Date().let {
                val dateText = DateHelper.dayMonthDateFormat.format(it)
                val timeText = DateHelper.getFormattedTime(context, it)
                "$dateText at $timeText"
            }

            ToDoScreen(
                title = "Toolbar title",
                toDoUiState = ToDoUiState(
                    title = "To Do title",
                    date = dateTitle,
                ),
                actionHandler = {},
                navigationActionClick = {},
            )
        }

        composeTestRule.onNode(hasText(dateTitle)).assertIsDisplayed()
    }

    @Test
    fun assertDescription() {
        composeTestRule.setContent {
            ToDoScreen(
                title = "Toolbar title",
                toDoUiState = ToDoUiState(
                    title = "To Do title",
                    description = "To Do description",
                ),
                actionHandler = {},
                navigationActionClick = {},
            )
        }

        composeTestRule.onNode(hasText("To Do description")).assertIsDisplayed()
    }

    @Test
    fun assertSnackbar() {
        composeTestRule.setContent {
            ToDoScreen(
                title = "Toolbar title",
                toDoUiState = ToDoUiState(
                    title = "To Do title",
                    errorSnack = "Error message",
                ),
                actionHandler = {},
                navigationActionClick = {},
            )
        }

        composeTestRule.onNode(hasText("Error message")).assertIsDisplayed()
    }

    @Test
    fun assertReminder() {
        composeTestRule.setContent {
            ToDoScreen(
                title = "Todo",
                toDoUiState = ToDoUiState(
                    title = "Todo title",
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
                navigationActionClick = {},
            )
        }

        composeTestRule.onNode(hasText("Reminder")).assertIsDisplayed()
        composeTestRule.onNode(hasText("Reminder title")).assertIsDisplayed()
        composeTestRule.onNode(hasContentDescription("Add reminder")).assertIsDisplayed()
    }
}