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
package com.instructure.ngc.features.submission.supportingpane

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.instui.compose.InstUITheme
import com.instructure.ngc.features.rubric.NgcRubricCriterion
import com.instructure.ngc.features.rubric.NgcRubricRating
import com.instructure.ngc.features.rubric.NgcRubricUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.compose.ui.graphics.Color as ComposeColor

@RunWith(AndroidJUnit4::class)
class SubmissionRubricScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val courseColor = ComposeColor(0xFF2573DF)

    private fun setContent(rubric: NgcRubricUiState?) {
        composeTestRule.setContent {
            InstUITheme(courseColor = courseColor) {
                SubmissionRubricScreen(rubric = rubric)
            }
        }
    }

    private val sampleRubric = NgcRubricUiState(
        criteria = listOf(
            NgcRubricCriterion(
                title = "Criteria 1",
                description = "Use of space",
                ratings = listOf(
                    NgcRubricRating(pointsLabel = "6", title = "Excellent", detail = "Great", isAssessed = true),
                ),
                assessedScore = "6",
                maxScore = "6",
                comment = null,
            ),
        ),
    )

    @Test
    fun showsEmptyMessageWhenRubricIsNull() {
        setContent(rubric = null)

        composeTestRule.onNodeWithText("There is no rubric for this assignment").assertIsDisplayed()
    }

    @Test
    fun showsRubricContentWhenRubricPresent() {
        setContent(rubric = sampleRubric)

        composeTestRule.onNodeWithText("Criteria 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Excellent").assertIsDisplayed()
    }

    @Test
    fun doesNotShowEmptyMessageWhenRubricPresent() {
        setContent(rubric = sampleRubric)

        composeTestRule.onNodeWithText("There is no rubric for this assignment").assertDoesNotExist()
    }
}
