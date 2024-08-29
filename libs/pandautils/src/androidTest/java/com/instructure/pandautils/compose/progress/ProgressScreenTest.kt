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
 *
 */

package com.instructure.pandautils.compose.progress

import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.pandautils.R
import com.instructure.pandautils.features.progress.ProgressState
import com.instructure.pandautils.features.progress.ProgressUiState
import com.instructure.pandautils.features.progress.composables.ProgressScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProgressScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun assertRunningState() {
        composeTestRule.setContent {
            ProgressScreen(
                progressUiState = ProgressUiState(
                    stringResource(id = R.string.allModulesAndItems),
                    stringResource(id = R.string.publishing),
                    10f,
                    "Bulk Update Note",
                    ProgressState.RUNNING
                )
            ) {
                // No-op
            }
        }

        composeTestRule.onNodeWithText("All Modules and Items").assertIsDisplayed()
        composeTestRule.onNodeWithText("Publishing 10%").assertIsDisplayed()
        composeTestRule.onNodeWithText("Note").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bulk Update Note").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
        composeTestRule.onNodeWithText("Done").assertDoesNotExist()
    }

    @Test
    fun assertCompletedState() {
        composeTestRule.setContent {
            ProgressScreen(
                progressUiState = ProgressUiState(
                    stringResource(id = R.string.allModules),
                    stringResource(id = R.string.publishing),
                    100f,
                    "Bulk Update Note",
                    ProgressState.COMPLETED
                )
            ) {
                // No-op
            }
        }

        composeTestRule.onNodeWithText("All Modules").assertIsDisplayed()
        composeTestRule.onNodeWithText("Success!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Note").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bulk Update Note").assertIsDisplayed()
        composeTestRule.onNodeWithText("Done").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").assertDoesNotExist()
    }

    @Test
    fun assertFailedState() {
        composeTestRule.setContent {
            ProgressScreen(
                progressUiState = ProgressUiState(
                    stringResource(id = R.string.selectedModulesAndItems),
                    stringResource(id = R.string.publishing),
                    10f,
                    "Bulk Update Note",
                    ProgressState.FAILED
                )
            ) {
                // No-op
            }
        }

        composeTestRule.onNodeWithText("Selected Modules and Items").assertIsDisplayed()
        composeTestRule.onNodeWithText("Update failed").assertIsDisplayed()
        composeTestRule.onNodeWithText("Note").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bulk Update Note").assertIsDisplayed()
        composeTestRule.onNodeWithText("Done").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").assertDoesNotExist()
    }

    @Test
    fun assertNoteNotDisplayed() {
        composeTestRule.setContent {
            ProgressScreen(
                progressUiState = ProgressUiState(
                    stringResource(id = R.string.allModulesAndItems),
                    stringResource(id = R.string.publishing),
                    10f,
                    null,
                    ProgressState.RUNNING
                )
            ) {
                // No-op
            }
        }

        composeTestRule.onNodeWithText("Note").assertDoesNotExist()
    }
}