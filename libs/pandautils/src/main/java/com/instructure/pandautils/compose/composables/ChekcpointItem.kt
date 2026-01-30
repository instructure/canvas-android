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
 */

package com.instructure.pandautils.compose.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.pandautils.R
import com.instructure.pandautils.features.grades.SubmissionStateLabel
import com.instructure.pandautils.utils.DisplayGrade


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CheckpointItem(
    discussionCheckpointUiState: DiscussionCheckpointUiState,
    contextColor: Color,
    showGrade: Boolean = true,
    colorOverride: Int? = null
) {
    val textColor = colorOverride?.let { colorResource(id = it) } ?: contextColor

    Column(
        modifier = Modifier
            .padding(top = 8.dp)
            .semantics(true) {}
    ) {
        Text(
            text = discussionCheckpointUiState.name,
            color = if (colorOverride != null) {
                colorResource(id = colorOverride)
            } else {
                colorResource(id = R.color.textDarkest)
            },
            fontSize = 16.sp,
            modifier = Modifier.testTag("checkpointName")
        )
        FlowRow {
            Text(
                text = discussionCheckpointUiState.dueDate,
                color = if (colorOverride != null) {
                    colorResource(id = colorOverride)
                } else {
                    colorResource(id = R.color.textDark)
                },
                fontSize = 14.sp,
                modifier = Modifier.testTag("checkpointDueDate_${discussionCheckpointUiState.name}")
            )
            if (discussionCheckpointUiState.submissionStateLabel != SubmissionStateLabel.None) {
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    Modifier
                        .height(16.dp)
                        .width(1.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(
                            color = if (colorOverride != null) {
                                colorResource(id = colorOverride)
                            } else {
                                colorResource(id = R.color.borderMedium)
                            }
                        )
                        .align(Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.width(4.dp))
                SubmissionState(
                    submissionStateLabel = discussionCheckpointUiState.submissionStateLabel,
                    testTag = "checkpointSubmissionStateLabel",
                    colorOverride = colorOverride
                )
            }
        }
        if (showGrade) {
            val gradeText = discussionCheckpointUiState.displayGrade.text
            if (gradeText.isNotEmpty()) {
                Text(
                    text = gradeText,
                    color = textColor,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .semantics {
                            contentDescription = discussionCheckpointUiState.displayGrade.contentDescription
                        }
                        .testTag("checkpointGradeText")
                )
            }
        } else {
            Text(
                pluralStringResource(
                    R.plurals.assignmentListMaxpoints,
                    discussionCheckpointUiState.pointsPossible,
                    discussionCheckpointUiState.pointsPossible
                ),
                color = textColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

data class DiscussionCheckpointUiState(
    val name: String,
    val dueDate: String,
    val submissionStateLabel: SubmissionStateLabel,
    val displayGrade: DisplayGrade,
    val pointsPossible: Int
)

@Preview(showBackground = true)
@Composable
private fun CheckpointItemPreview() {
    CheckpointItem(
        discussionCheckpointUiState = DiscussionCheckpointUiState(
            name = "Checkpoint 1",
            dueDate = "Due Sep 30",
            submissionStateLabel = SubmissionStateLabel.Submitted,
            displayGrade = DisplayGrade(
                text = "95/100",
                contentDescription = "Grade 95 out of 100"
            ),
            pointsPossible = 100
        ),
        contextColor = Color.Blue
    )
}
