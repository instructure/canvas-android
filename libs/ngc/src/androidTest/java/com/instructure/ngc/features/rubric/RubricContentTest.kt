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
package com.instructure.ngc.features.rubric

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.instui.compose.InstUITheme
import com.instructure.instui.compose.text.Text
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.compose.ui.graphics.Color as ComposeColor

@RunWith(AndroidJUnit4::class)
class RubricContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val courseColor = ComposeColor(0xFF2573DF)

    private fun setContent(
        rubric: NgcRubricUiState,
        header: (@Composable () -> Unit)? = null,
    ) {
        composeTestRule.setContent {
            InstUITheme(courseColor = courseColor) {
                RubricContent(rubric = rubric, header = header)
            }
        }
    }

    private fun rubric(
        title: String = "Criteria 1",
        description: String? = "Use of space",
        ratingTitle: String? = "Excellent",
        assessedScore: String? = "6",
        comment: String? = null,
    ) = NgcRubricUiState(
        criteria = listOf(
            NgcRubricCriterion(
                title = title,
                description = description,
                ratings = listOf(
                    NgcRubricRating(pointsLabel = "6", title = ratingTitle, detail = "Great", isAssessed = true),
                    NgcRubricRating(pointsLabel = "3", title = "Good", detail = "Ok", isAssessed = false),
                ),
                assessedScore = assessedScore,
                maxScore = "6",
                comment = comment,
            ),
        ),
    )

    @Test
    fun showsCriterionTitleAndDescription() {
        setContent(rubric(title = "Criteria 1", description = "Use of space"))

        composeTestRule.onNodeWithText("Criteria 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Use of space").assertIsDisplayed()
    }

    @Test
    fun showsRatingsWhenExpandedByDefault() {
        setContent(rubric())

        composeTestRule.onNodeWithText("Excellent").assertIsDisplayed()
        composeTestRule.onNodeWithText("Good").assertIsDisplayed()
    }

    @Test
    fun showsTeacherCommentWhenPresent() {
        setContent(rubric(comment = "Nice work"))

        composeTestRule.onNodeWithText("Nice work").assertIsDisplayed()
    }

    @Test
    fun rendersHeaderSlotWhenProvided() {
        setContent(rubric(), header = { Text("Rubric header") })

        composeTestRule.onNodeWithText("Rubric header").assertIsDisplayed()
    }

    @Test
    fun rendersMultipleCriteria() {
        val twoCriteria = NgcRubricUiState(
            criteria = listOf(
                NgcRubricCriterion(
                    title = "First criterion",
                    description = null,
                    ratings = emptyList(),
                    assessedScore = null,
                    maxScore = "5",
                    comment = null,
                ),
                NgcRubricCriterion(
                    title = "Second criterion",
                    description = null,
                    ratings = emptyList(),
                    assessedScore = null,
                    maxScore = "5",
                    comment = null,
                ),
            ),
        )
        setContent(twoCriteria)

        composeTestRule.onNodeWithText("First criterion").assertIsDisplayed()
        composeTestRule.onNodeWithText("Second criterion").assertIsDisplayed()
    }
}
