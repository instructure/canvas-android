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

package com.instructure.pandautils.compose.features.grades

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.instructure.composetest.hasDrawable
import com.instructure.espresso.assertTextColor
import com.instructure.instui.compose.InstUITheme
import com.instructure.pandares.R
import com.instructure.pandautils.compose.composables.DiscussionCheckpointUiState
import com.instructure.pandautils.compose.composables.SubmissionStateLabel
import com.instructure.pandautils.designsystem.DesignSystem
import com.instructure.pandautils.designsystem.LocalDesignSystem
import com.instructure.pandautils.features.grades.AssignmentUiState
import com.instructure.pandautils.features.grades.GradesAction
import com.instructure.pandautils.features.grades.components.GradesAssignmentItem
import com.instructure.pandautils.utils.DisplayGrade
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class GradesAssignmentItemTest {

    @get:Rule
    var composeTestRule = createComposeRule()

    @Test
    fun assertNotSubmittedAssignment() {
        var labelColor = Color(0)

        composeTestRule.setContent {
            labelColor = colorResource(id = R.color.textDark)
            GradesAssignmentItem(
                uiState = getUiState(),
                actionHandler = {},
                contextColor = android.graphics.Color.RED,
                showWhatIfScore = false
            )
        }

        composeTestRule.onNodeWithText("Assignment")
            .assertIsDisplayed()
        composeTestRule.onNode(hasDrawable(R.drawable.ic_assignment))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("No due date")
            .assertIsDisplayed()
        composeTestRule.onNode(hasDrawable(R.drawable.ic_unpublish), true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Not Submitted", useUnmergedTree = true)
            .assertIsDisplayed()
            .assertTextColor(labelColor)
        composeTestRule.onNodeWithText("-/15", useUnmergedTree = true)
            .assertIsDisplayed()
            .assertContentDescriptionEquals("Content description")
            .assertTextColor(Color(android.graphics.Color.RED))
    }

    @Test
    fun assertSubmittedAssignment() {
        var labelColor = Color(0)

        composeTestRule.setContent {
            labelColor = colorResource(id = R.color.textSuccess)
            GradesAssignmentItem(
                uiState = getUiState().copy(
                    submissionStateLabel = SubmissionStateLabel.Submitted
                ),
                actionHandler = {},
                contextColor = android.graphics.Color.RED,
                showWhatIfScore = false
            )
        }

        composeTestRule.onNode(hasDrawable(R.drawable.ic_complete), true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Submitted", useUnmergedTree = true)
            .assertIsDisplayed()
            .assertTextColor(labelColor)
    }

    @Test
    fun assertMissingAssignment() {
        var labelColor = Color(0)

        composeTestRule.setContent {
            labelColor = colorResource(id = R.color.textDanger)
            GradesAssignmentItem(
                uiState = getUiState().copy(
                    submissionStateLabel = SubmissionStateLabel.Missing
                ),
                actionHandler = {},
                contextColor = android.graphics.Color.RED,
                showWhatIfScore = false
            )
        }

        composeTestRule.onNode(hasDrawable(R.drawable.ic_unpublish), true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Missing", useUnmergedTree = true)
            .assertIsDisplayed()
            .assertTextColor(labelColor)
    }

    @Test
    fun assertLateAssignment() {
        var labelColor = Color(0)

        composeTestRule.setContent {
            labelColor = colorResource(id = R.color.textWarning)
            GradesAssignmentItem(
                uiState = getUiState().copy(
                    submissionStateLabel = SubmissionStateLabel.Late
                ),
                actionHandler = {},
                contextColor = android.graphics.Color.RED,
                showWhatIfScore = false
            )
        }

        composeTestRule.onNode(hasDrawable(R.drawable.ic_clock), true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Late", useUnmergedTree = true)
            .assertIsDisplayed()
            .assertTextColor(labelColor)
    }

    @Test
    fun assertGradedAssignment() {
        var labelColor = Color(0)

        composeTestRule.setContent {
            labelColor = colorResource(id = R.color.textSuccess)
            GradesAssignmentItem(
                uiState = getUiState().copy(
                    submissionStateLabel = SubmissionStateLabel.Graded
                ),
                actionHandler = {},
                contextColor = android.graphics.Color.RED,
                showWhatIfScore = false
            )
        }

        composeTestRule.onNode(hasDrawable(R.drawable.ic_complete_solid), true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Graded", useUnmergedTree = true)
            .assertIsDisplayed()
            .assertTextColor(labelColor)
    }

    @Test
    fun assertCustomStatusAssignment() {
        var labelColor = Color(0)

        composeTestRule.setContent {
            labelColor = colorResource(id = R.color.textInfo)
            GradesAssignmentItem(
                uiState = getUiState().copy(
                    submissionStateLabel = SubmissionStateLabel.Custom(R.drawable.ic_flag, R.color.textInfo, "Custom Status")
                ),
                actionHandler = {},
                contextColor = android.graphics.Color.RED,
                showWhatIfScore = false
            )
        }

        composeTestRule.onNode(hasDrawable(R.drawable.ic_flag), true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Custom Status", useUnmergedTree = true)
            .assertIsDisplayed()
            .assertTextColor(labelColor)
    }

    @Test
    fun assertWhatIfScoreDisplayedWhenEnabled() {
        composeTestRule.setContent {
            GradesAssignmentItem(
                uiState = getUiState().copy(
                    score = 10.0,
                    displayGrade = DisplayGrade("10/15", "10 out of 15"),
                    whatIfScore = 14.0
                ),
                actionHandler = {},
                contextColor = android.graphics.Color.RED,
                showWhatIfScore = true
            )
        }

        // Regular grade should be displayed
        composeTestRule.onNodeWithText("10/15", useUnmergedTree = true)
            .assertIsDisplayed()

        // What-if score should be displayed
        composeTestRule.onNodeWithText("What-if: 14/15", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun assertWhatIfScoreNotDisplayedWhenDisabled() {
        composeTestRule.setContent {
            GradesAssignmentItem(
                uiState = getUiState().copy(
                    score = 10.0,
                    displayGrade = DisplayGrade("10/15", "10 out of 15"),
                    whatIfScore = 14.0
                ),
                actionHandler = {},
                contextColor = android.graphics.Color.RED,
                showWhatIfScore = false
            )
        }

        // Regular grade should be displayed
        composeTestRule.onNodeWithText("10/15", useUnmergedTree = true)
            .assertIsDisplayed()

        // What-if score should NOT be displayed
        composeTestRule.onNodeWithText("What-if: 14/15", useUnmergedTree = true)
            .assertDoesNotExist()
    }

    @Test
    fun assertWhatIfScoreNotDisplayedWhenNull() {
        composeTestRule.setContent {
            GradesAssignmentItem(
                uiState = getUiState().copy(
                    score = 10.0,
                    displayGrade = DisplayGrade("10/15", "10 out of 15"),
                    whatIfScore = null
                ),
                actionHandler = {},
                contextColor = android.graphics.Color.RED,
                showWhatIfScore = true
            )
        }

        // Regular grade should be displayed
        composeTestRule.onNodeWithText("10/15", useUnmergedTree = true)
            .assertIsDisplayed()

        // What-if score should NOT be displayed when null
        composeTestRule.onNodeWithText("What-if: 14/15", useUnmergedTree = true)
            .assertDoesNotExist()
    }

    @Test
    fun assertWhatIfScoreDisplayedWithoutExistingGrade() {
        composeTestRule.setContent {
            GradesAssignmentItem(
                uiState = getUiState().copy(
                    score = null,
                    displayGrade = DisplayGrade("", ""),
                    whatIfScore = 12.0
                ),
                actionHandler = {},
                contextColor = android.graphics.Color.RED,
                showWhatIfScore = true
            )
        }

        // What-if score should be displayed
        composeTestRule.onNodeWithText("What-if: 12/15", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun assertWhatIfScoreWithZeroScore() {
        composeTestRule.setContent {
            GradesAssignmentItem(
                uiState = getUiState().copy(
                    score = 0.0,
                    displayGrade = DisplayGrade("0/15", "0 out of 15"),
                    whatIfScore = 10.0
                ),
                actionHandler = {},
                contextColor = android.graphics.Color.RED,
                showWhatIfScore = true
            )
        }

        // Regular grade should be displayed
        composeTestRule.onNodeWithText("0/15", useUnmergedTree = true)
            .assertIsDisplayed()

        // What-if score should be displayed
        composeTestRule.onNodeWithText("What-if: 10/15", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun assertWhatIfScoreWithFullScore() {
        composeTestRule.setContent {
            GradesAssignmentItem(
                uiState = getUiState().copy(
                    score = 15.0,
                    displayGrade = DisplayGrade("15/15", "15 out of 15"),
                    whatIfScore = 15.0
                ),
                actionHandler = {},
                contextColor = android.graphics.Color.RED,
                showWhatIfScore = true
            )
        }

        // Regular grade should be displayed
        composeTestRule.onNodeWithText("15/15", useUnmergedTree = true)
            .assertIsDisplayed()

        // What-if score should be displayed
        composeTestRule.onNodeWithText("What-if: 15/15", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun assertInstUICheckpointsDisplayedWhenExpanded() {
        composeTestRule.setContent {
            InstUIContent {
                GradesAssignmentItem(
                    uiState = getCheckpointUiState(expanded = true),
                    actionHandler = {},
                    contextColor = android.graphics.Color.RED,
                    showWhatIfScore = false
                )
            }
        }

        composeTestRule.onNodeWithText("Reply to topic", useUnmergedTree = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Due Jul 6, 2025 12:00", useUnmergedTree = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("-/5", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun assertInstUICheckpointsHiddenWhenCollapsed() {
        composeTestRule.setContent {
            InstUIContent {
                GradesAssignmentItem(
                    uiState = getCheckpointUiState(expanded = false),
                    actionHandler = {},
                    contextColor = android.graphics.Color.RED,
                    showWhatIfScore = false
                )
            }
        }

        composeTestRule.onNodeWithText("Reply to topic", useUnmergedTree = true)
            .assertDoesNotExist()
    }

    @Test
    fun assertInstUICheckpointRowClickTriggersAssignmentClick() {
        var action: GradesAction? = null
        composeTestRule.setContent {
            InstUIContent {
                GradesAssignmentItem(
                    uiState = getCheckpointUiState(expanded = true),
                    actionHandler = { action = it },
                    contextColor = android.graphics.Color.RED,
                    showWhatIfScore = false
                )
            }
        }

        composeTestRule.onNodeWithText("Reply to topic", useUnmergedTree = true)
            .performClick()

        assertEquals(GradesAction.AssignmentClick(1L), action)
    }

    @Test
    fun assertInstUICheckpointAccordionWithCommentsTogglesExpand() {
        var action: GradesAction? = null
        composeTestRule.setContent {
            InstUIContent {
                GradesAssignmentItem(
                    uiState = getCheckpointUiState(expanded = false).copy(commentCount = 3),
                    actionHandler = { action = it },
                    contextColor = android.graphics.Color.RED,
                    showWhatIfScore = false
                )
            }
        }

        composeTestRule.onNodeWithTag("expandDiscussionCheckpoints", useUnmergedTree = true)
            .performClick()

        assertEquals(GradesAction.ToggleCheckpointsExpanded(1L), action)
    }

    @Test
    fun assertInstUICheckpointAccordionWithoutCommentsTogglesExpand() {
        var action: GradesAction? = null
        composeTestRule.setContent {
            InstUIContent {
                GradesAssignmentItem(
                    uiState = getCheckpointUiState(expanded = false),
                    actionHandler = { action = it },
                    contextColor = android.graphics.Color.RED,
                    showWhatIfScore = false
                )
            }
        }

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val expandDescription = context.getString(
            com.instructure.pandautils.R.string.content_description_expand_content_with_param,
            context.getString(com.instructure.pandautils.R.string.a11y_discussion_checkpoints)
        )
        composeTestRule.onNodeWithContentDescription(expandDescription, useUnmergedTree = true)
            .performClick()

        assertEquals(GradesAction.ToggleCheckpointsExpanded(1L), action)
    }

    @Test
    fun assertInstUIWhatIfPencilShownAndOpensDialog() {
        var action: GradesAction? = null
        composeTestRule.setContent {
            InstUIContent {
                GradesAssignmentItem(
                    uiState = getUiState().copy(whatIfScore = 12.0),
                    actionHandler = { action = it },
                    contextColor = android.graphics.Color.RED,
                    showWhatIfScore = true
                )
            }
        }

        composeTestRule.onNodeWithTag("editWhatIfScore", useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        assertEquals(GradesAction.ShowWhatIfScoreDialog(1L), action)
    }

    @Test
    fun assertInstUIWhatIfPencilHiddenWhenDisabled() {
        composeTestRule.setContent {
            InstUIContent {
                GradesAssignmentItem(
                    uiState = getUiState().copy(whatIfScore = 12.0),
                    actionHandler = {},
                    contextColor = android.graphics.Color.RED,
                    showWhatIfScore = false
                )
            }
        }

        composeTestRule.onNodeWithTag("editWhatIfScore", useUnmergedTree = true)
            .assertDoesNotExist()
    }

    @Test
    fun assertInstUIWhatIfScoreReplacesGradeOnScoredRow() {
        composeTestRule.setContent {
            InstUIContent {
                GradesAssignmentItem(
                    uiState = getUiState().copy(
                        score = 10.0,
                        displayGrade = DisplayGrade("10/15", "10 out of 15"),
                        whatIfScore = 14.0
                    ),
                    actionHandler = {},
                    contextColor = android.graphics.Color.RED,
                    showWhatIfScore = true
                )
            }
        }

        // In What-If mode only the what-if score is shown; the real grade is replaced.
        composeTestRule.onNodeWithText("14/15", useUnmergedTree = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("10/15", useUnmergedTree = true)
            .assertDoesNotExist()
    }

    @Composable
    private fun InstUIContent(content: @Composable () -> Unit) {
        InstUITheme(courseColor = Color.Blue) {
            CompositionLocalProvider(LocalDesignSystem provides DesignSystem.InstUI) {
                content()
            }
        }
    }

    private fun getCheckpointUiState(expanded: Boolean) = getUiState().copy(
        checkpointsExpanded = expanded,
        checkpoints = listOf(
            DiscussionCheckpointUiState(
                name = "Reply to topic",
                dueDate = "Due Jul 6, 2025 12:00",
                submissionStateLabel = SubmissionStateLabel.NotSubmitted,
                displayGrade = DisplayGrade("-/5", "-/5"),
                pointsPossible = 5
            )
        )
    )

    private fun getUiState() = AssignmentUiState(
        id = 1,
        iconRes = R.drawable.ic_assignment,
        name = "Assignment",
        dueDate = "No due date",
        submissionStateLabel = SubmissionStateLabel.NotSubmitted,
        displayGrade = DisplayGrade("-/15", "Content description"),
        score = null,
        maxScore = 15.0,
        whatIfScore = null
    )
}
