/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.pandautils.compose.features.studytools

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.pandautils.domain.models.studytools.StudyTool
import com.instructure.pandautils.domain.models.studytools.StudyToolOption
import com.instructure.pandautils.domain.models.studytools.StudyToolResult
import com.instructure.pandautils.features.studytools.StudyToolState
import com.instructure.pandautils.features.studytools.StudyToolsPanel
import com.instructure.pandautils.features.studytools.StudyToolsScreenState
import com.instructure.pandautils.features.studytools.StudyToolsUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StudyToolsPanelTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val summarize = StudyToolOption(StudyTool.SUMMARIZE, "Summarize")
    private val menuState = StudyToolsUiState(screenState = StudyToolsScreenState.Content(listOf(summarize)))

    /**
     * The phone sheet is measured at full height so it has a full-page stage to drag to, which means
     * anything pinned to the bottom of its content sits below the screen while the sheet is only
     * partially expanded. Opening a tool has to take the sheet to its full stage, or the regenerate link
     * and the feedback icons are unreachable with nothing on screen hinting they exist.
     */
    @Test
    fun assertOpeningAToolBringsItsFooterOnScreen() {
        var uiState by mutableStateOf(menuState)
        composeTestRule.setContent {
            StudyToolsPanel(uiState = uiState, isTablet = false)
        }

        composeTestRule.onNodeWithTag("studyToolsMenu").assertIsDisplayed()

        uiState = uiState.copy(
            selectedTool = summarize,
            toolState = StudyToolState.Ready(StudyToolResult.Summary("A summary."))
        )
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("studyToolsSummary").assertIsDisplayed()
        composeTestRule.onNodeWithTag("studyToolsRegenerate").assertIsDisplayed()
        composeTestRule.onNodeWithTag("studyToolsFeedbackLiked").assertIsDisplayed()
        composeTestRule.onNodeWithTag("studyToolsFeedbackDisliked").assertIsDisplayed()
    }

    @Test
    fun assertTabletDrawerShowsTheToolFooter() {
        composeTestRule.setContent {
            StudyToolsPanel(
                uiState = menuState.copy(
                    selectedTool = summarize,
                    toolState = StudyToolState.Ready(StudyToolResult.Summary("A summary."))
                ),
                isTablet = true
            )
        }

        composeTestRule.onNodeWithTag("studyToolsDrawer").assertIsDisplayed()
        composeTestRule.onNodeWithTag("studyToolsSummary").assertIsDisplayed()
        composeTestRule.onNodeWithTag("studyToolsRegenerate").assertIsDisplayed()
    }

    @Test
    fun assertMenuIsShownWhenNoToolIsSelected() {
        composeTestRule.setContent {
            StudyToolsPanel(uiState = menuState, isTablet = false)
        }

        composeTestRule.onNodeWithTag("studyToolsMenu").assertIsDisplayed()
        composeTestRule.onNodeWithTag("studyTool_summarize").assertIsDisplayed()
        composeTestRule.onNodeWithTag("studyToolsRegenerate").assertDoesNotExist()
    }
}
