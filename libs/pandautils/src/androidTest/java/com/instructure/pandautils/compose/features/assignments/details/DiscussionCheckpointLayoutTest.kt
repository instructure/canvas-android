/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.pandautils.compose.features.assignments.details

import android.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.instructure.pandares.R
import com.instructure.pandautils.compose.composables.SubmissionStateLabel
import com.instructure.pandautils.features.assignments.details.DiscussionCheckpointViewState
import com.instructure.pandautils.features.assignments.details.composables.DiscussionCheckpointLayout
import org.junit.Rule
import org.junit.Test

class DiscussionCheckpointLayoutTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun checkpointsAreDisplayedCorrectly() {
        val checkpoints = listOf(
            DiscussionCheckpointViewState(
                name = "Reply to topic",
                stateLabel = SubmissionStateLabel.Graded,
                grade = "5 / 5 pts",
                courseColor = Color.RED
            ),
            DiscussionCheckpointViewState(
                name = "Additional replies (3)",
                stateLabel = SubmissionStateLabel.Late,
                grade = "3 / 5 pts",
                courseColor = Color.RED
            )
        )

        composeTestRule.setContent {
            DiscussionCheckpointLayout(checkpoints = checkpoints)
        }

        composeTestRule.onNodeWithTag("checkpointItem-0")
            .assertIsDisplayed()

        composeTestRule.onNode(hasTestTag("checkpointName") and hasText("Reply to topic"), useUnmergedTree = true)
            .assertIsDisplayed()

        composeTestRule.onNode(hasTestTag("checkpointStatus") and hasText("Graded"), useUnmergedTree = true)
            .assertIsDisplayed()

        composeTestRule.onNode(hasTestTag("checkpointGrade") and hasText("5 / 5 pts"), useUnmergedTree = true)
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("checkpointItem-1")
            .assertIsDisplayed()

        composeTestRule.onNode(hasTestTag("checkpointName") and hasText("Additional replies (3)"), useUnmergedTree = true)
            .assertIsDisplayed()

        composeTestRule.onNode(hasTestTag("checkpointStatus") and hasText("Late"), useUnmergedTree = true)
            .assertIsDisplayed()

        composeTestRule.onNode(hasTestTag("checkpointGrade") and hasText("3 / 5 pts"), useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun emptyCheckpointsListRendersNothing() {
        composeTestRule.setContent {
            DiscussionCheckpointLayout(checkpoints = emptyList())
        }

        composeTestRule.onNodeWithTag("checkpointItem-0")
            .assertDoesNotExist()
    }

    @Test
    fun singleCheckpointIsDisplayedWithoutDivider() {
        val checkpoints = listOf(
            DiscussionCheckpointViewState(
                name = "Reply to topic",
                stateLabel = SubmissionStateLabel.Companion.Graded,
                grade = "5 / 5 pts",
                courseColor = Color.BLUE
            )
        )

        composeTestRule.setContent {
            DiscussionCheckpointLayout(checkpoints = checkpoints)
        }

        composeTestRule.onNodeWithTag("checkpointItem-0")
            .assertIsDisplayed()

        composeTestRule.onNode(hasTestTag("checkpointName") and hasText("Reply to topic"), useUnmergedTree = true)
            .assertIsDisplayed()

        composeTestRule.onNode(hasTestTag("checkpointStatus") and hasText("Graded"), useUnmergedTree = true)
            .assertIsDisplayed()

        composeTestRule.onNode(hasTestTag("checkpointGrade") and hasText("5 / 5 pts"), useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun checkpointWithCustomStatusIsDisplayedCorrectly() {
        val checkpoints = listOf(
            DiscussionCheckpointViewState(
                name = "Reply to topic",
                stateLabel = SubmissionStateLabel.Custom(
                    iconRes = R.drawable.ic_complete,
                    colorRes = R.color.textWarning,
                    label = "In Review"
                ),
                grade = "10 / 10 pts",
                courseColor = Color.GREEN
            )
        )

        composeTestRule.setContent {
            DiscussionCheckpointLayout(checkpoints = checkpoints)
        }

        composeTestRule.onNodeWithTag("checkpointItem-0")
            .assertIsDisplayed()

        composeTestRule.onNode(hasTestTag("checkpointName") and hasText("Reply to topic"), useUnmergedTree = true)
            .assertIsDisplayed()

        composeTestRule.onNode(hasTestTag("checkpointStatus") and hasText("In Review"), useUnmergedTree = true)
            .assertIsDisplayed()

        composeTestRule.onNode(hasTestTag("checkpointGrade") and hasText("10 / 10 pts"), useUnmergedTree = true)
            .assertIsDisplayed()
    }
}
