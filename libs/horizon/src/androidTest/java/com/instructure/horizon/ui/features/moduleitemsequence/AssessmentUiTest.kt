/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.horizon.ui.features.moduleitemsequence

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.horizon.features.moduleitemsequence.content.assessment.AssessmentContentScreen
import com.instructure.horizon.features.moduleitemsequence.content.assessment.AssessmentUiState
import com.instructure.horizon.horizonui.platform.LoadingState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AssessmentUiTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testStartQuizButtonDisplays() {
        val uiState = AssessmentUiState(
            assessmentName = "Math Quiz",
            loadingState = LoadingState(isLoading = false)
        )

        composeTestRule.setContent {
            AssessmentContentScreen(uiState = uiState)
        }

        composeTestRule.onNodeWithText("Start quiz")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun testAssessmentNameDisplays() {
        val uiState = AssessmentUiState(
            assessmentName = "Final Exam",
            showAssessmentDialog = true,
            loadingState = LoadingState(isLoading = false)
        )

        composeTestRule.setContent {
            AssessmentContentScreen(uiState = uiState)
        }

        composeTestRule.onNodeWithText("Final Exam")
            .assertIsDisplayed()
    }

    @Test
    fun testLoadingStateDisplaysSpinner() {
        val uiState = AssessmentUiState(
            assessmentName = "Quiz",
            loadingState = LoadingState(isLoading = true)
        )

        composeTestRule.setContent {
            AssessmentContentScreen(uiState = uiState)
        }

        composeTestRule.onNodeWithTag("LoadingSpinner")
            .assertIsDisplayed()
    }

    @Test
    fun testAssessmentDialogDisplaysWhenOpen() {
        val uiState = AssessmentUiState(
            assessmentName = "Quiz",
            showAssessmentDialog = true,
            urlToLoad = "https://example.com/quiz",
            loadingState = LoadingState(isLoading = false)
        )

        composeTestRule.setContent {
            AssessmentContentScreen(uiState = uiState)
        }

        composeTestRule.onNodeWithTag("AssessmentDialog")
            .assertIsDisplayed()
    }

    @Test
    fun testAssessmentLoadingIndicatorDisplays() {
        val uiState = AssessmentUiState(
            assessmentName = "Quiz",
            showAssessmentDialog = true,
            assessmentLoading = true,
            loadingState = LoadingState(isLoading = false)
        )

        composeTestRule.setContent {
            AssessmentContentScreen(uiState = uiState)
        }

        composeTestRule.onNodeWithTag("LoadingSpinner")
            .assertIsDisplayed()
    }

    @Test
    fun testCompletionLoadingDisplays() {
        val uiState = AssessmentUiState(
            assessmentName = "Quiz",
            assessmentCompletionLoading = true,
            showAssessmentDialog = true,
            loadingState = LoadingState(isLoading = false)
        )

        composeTestRule.setContent {
            AssessmentContentScreen(uiState = uiState)
        }

        composeTestRule.onNodeWithTag("LoadingSpinner")
            .assertIsDisplayed()
    }

    @Test
    fun testCloseAssessmentButtonDisplays() {
        val uiState = AssessmentUiState(
            assessmentName = "Quiz",
            showAssessmentDialog = true,
            urlToLoad = "https://example.com/quiz",
            loadingState = LoadingState(isLoading = false)
        )

        composeTestRule.setContent {
            AssessmentContentScreen(uiState = uiState)
        }

        composeTestRule.onNodeWithContentDescription("Close")
            .assertIsDisplayed()
            .assertHasClickAction()
    }
}
