/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.pandautils.compose.features.grades.gradepreferences

import android.graphics.Color
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasAnySibling
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.GradingPeriod
import com.instructure.pandautils.features.grades.gradepreferences.GradePreferencesScreen
import com.instructure.pandautils.features.grades.gradepreferences.GradePreferencesUiState
import com.instructure.pandautils.features.grades.gradepreferences.SortBy
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class GradePreferencesScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun assertGradePreferencesScreenContent() {
        composeTestRule.setContent {
            GradePreferencesScreen(
                uiState = GradePreferencesUiState(
                    selectedGradingPeriod = null,
                    sortBy = SortBy.DUE_DATE,
                    courseName = "Test Course",
                    canvasContextColor = Color.RED,
                    gradingPeriods = listOf(
                        GradingPeriod(1, "Period 1"),
                        GradingPeriod(2, "Period 2")
                    )
                ),
                onPreferenceChangeSaved = { _, _ -> },
                navigationActionClick = {}
            )
        }

        composeTestRule.onNodeWithTag("GradePreferencesToolbar")
            .assertIsDisplayed()
        composeTestRule.onNode(hasParent(hasTestTag("GradePreferencesToolbar")).and(hasContentDescription("Close")))
            .assertIsDisplayed()
            .assertHasClickAction()
        composeTestRule.onNodeWithText("Grade Preferences")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Test Course")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Save")
            .assertIsDisplayed()
            .assertIsNotEnabled()

        composeTestRule.onNodeWithText("Grading Period")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("All Grading Periods")
            .assertIsDisplayed()
            .assertHasClickAction()
        composeTestRule.onNode(hasAnySibling(hasText("All Grading Periods")), useUnmergedTree = true)
            .assertIsSelected()
        composeTestRule.onNodeWithText("Period 1")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Period 2")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Sort By")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Due Date")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Group")
            .assertIsDisplayed()
    }

    @Test
    fun assertGradingPeriodSelection() {
        composeTestRule.setContent {
            GradePreferencesScreen(
                uiState = GradePreferencesUiState(
                    selectedGradingPeriod = GradingPeriod(2, "Period 2"),
                    sortBy = SortBy.DUE_DATE,
                    courseName = "Test Course",
                    canvasContextColor = Color.RED,
                    gradingPeriods = listOf(
                        GradingPeriod(1, "Period 1"),
                        GradingPeriod(2, "Period 2")
                    )
                ),
                onPreferenceChangeSaved = { _, _ -> },
                navigationActionClick = {}
            )
        }

        composeTestRule.onNodeWithText("Period 1")
            .assertIsDisplayed()
            .assertHasClickAction()
        composeTestRule.onNode(hasAnySibling(hasText("Period 1")), useUnmergedTree = true)
            .assertIsNotSelected()
        composeTestRule.onNodeWithText("Period 2")
            .assertIsDisplayed()
            .assertHasClickAction()
        composeTestRule.onNode(hasAnySibling(hasText("Period 2")), useUnmergedTree = true)
            .assertIsSelected()
    }

    @Test
    fun assertSortBySelection() {
        val testState = GradePreferencesUiState(
            selectedGradingPeriod = null,
            sortBy = SortBy.GROUP,
            courseName = "Test Course",
            canvasContextColor = Color.RED,
            gradingPeriods = emptyList()
        )

        composeTestRule.setContent {
            GradePreferencesScreen(
                uiState = testState,
                onPreferenceChangeSaved = { _, _ -> },
                navigationActionClick = {}
            )
        }

        composeTestRule.onNodeWithText("Due Date")
            .assertIsDisplayed()
            .assertHasClickAction()
        composeTestRule.onNode(hasAnySibling(hasText("Due Date")), useUnmergedTree = true)
            .assertIsNotSelected()
        composeTestRule.onNodeWithText("Group")
            .assertIsDisplayed()
            .assertHasClickAction()
        composeTestRule.onNode(hasAnySibling(hasText("Group")), useUnmergedTree = true)
            .assertIsSelected()
    }

    @Test
    fun assertSaveButtonEnablesOnChanges() {
        composeTestRule.setContent {
            GradePreferencesScreen(
                uiState = GradePreferencesUiState(
                    selectedGradingPeriod = null,
                    sortBy = SortBy.DUE_DATE,
                    courseName = "Test Course",
                    canvasContextColor = Color.RED,
                    gradingPeriods = emptyList()
                ),
                onPreferenceChangeSaved = { _, _ -> },
                navigationActionClick = {}
            )
        }

        composeTestRule.onNodeWithText("Save")
            .assertIsNotEnabled()

        composeTestRule.onNodeWithText("Group")
            .performClick()

        composeTestRule.onNodeWithText("Save")
            .assertIsEnabled()
    }
}
