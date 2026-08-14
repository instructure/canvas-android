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

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.pandautils.domain.models.studytools.StudyTool
import com.instructure.pandautils.domain.models.studytools.StudyToolOption
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
