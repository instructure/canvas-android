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
package com.instructure.pandautils.features.assignments.details.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.CanvasDivider
import com.instructure.pandautils.compose.composables.SubmissionState
import com.instructure.pandautils.features.assignments.details.DiscussionCheckpointViewState
import com.instructure.pandautils.features.grades.SubmissionStateLabel

@Composable
fun DiscussionCheckpointLayout(
    checkpoints: List<DiscussionCheckpointViewState>,
    modifier: Modifier = Modifier
) {
    if (checkpoints.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp)
            .background(
                color = colorResource(id = R.color.backgroundLightest),
                shape = RoundedCornerShape(6.dp)
            )
            .border(
                width = 1.dp,
                color = colorResource(id = R.color.borderMedium),
                shape = RoundedCornerShape(6.dp)
            )
            .padding(16.dp)
    ) {
        checkpoints.forEachIndexed { index, checkpoint ->
            CheckpointItem(
                checkpoint = checkpoint,
                modifier = Modifier.testTag("checkpointItem-$index")
            )
            if (index < checkpoints.lastIndex) {
                CanvasDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

@Composable
private fun CheckpointItem(
    checkpoint: DiscussionCheckpointViewState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(vertical = 4.dp)
            .fillMaxWidth()
            .semantics(true) {},
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = checkpoint.name,
                fontSize = 16.sp,
                color = colorResource(id = R.color.textDarkest),
                modifier = Modifier.testTag("checkpointName")
            )

            SubmissionState(
                submissionStateLabel = checkpoint.stateLabel,
                testTag = "checkpointStatus"
            )
        }

        Text(
            text = checkpoint.grade,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(checkpoint.courseColor),
            modifier = Modifier
                .padding(start = 8.dp)
                .testTag("checkpointGrade")
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DiscussionCheckpointLayoutPreview() {
    DiscussionCheckpointLayout(
        checkpoints = listOf(
            DiscussionCheckpointViewState(
                name = "Reply to topic",
                stateLabel = SubmissionStateLabel.Graded,
                grade = "5/5",
                courseColor = android.graphics.Color.RED
            ),
            DiscussionCheckpointViewState(
                name = "Additional replies (3)",
                stateLabel = SubmissionStateLabel.Graded,
                grade = "2.5/5",
                courseColor = android.graphics.Color.RED
            )
        )
    )
}
