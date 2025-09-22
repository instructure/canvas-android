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

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.pandautils.R
import com.instructure.pandautils.features.grades.SubmissionStateLabel
import com.instructure.pandautils.utils.drawableId


@Composable
fun SubmissionState(submissionStateLabel: SubmissionStateLabel, testTag: String) {
    if (submissionStateLabel != SubmissionStateLabel.None) {
        Row {
            Icon(
                painter = painterResource(id = submissionStateLabel.iconRes),
                contentDescription = null,
                tint = colorResource(id = submissionStateLabel.colorRes),
                modifier = Modifier
                    .size(16.dp)
                    .align(Alignment.CenterVertically)
                    .semantics {
                        drawableId = submissionStateLabel.iconRes
                    }
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = when (submissionStateLabel) {
                    is SubmissionStateLabel.Predefined -> stringResource(id = submissionStateLabel.labelRes)
                    is SubmissionStateLabel.Custom -> submissionStateLabel.label
                },
                color = colorResource(id = submissionStateLabel.colorRes),
                fontSize = 14.sp,
                modifier = Modifier.testTag(testTag)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SubmissionStatePreview_NotSubmitted() {
    SubmissionState(
        submissionStateLabel = SubmissionStateLabel.NotSubmitted,
        testTag = "notSubmitted"
    )
}

@Preview(showBackground = true)
@Composable
private fun SubmissionStatePreview_Missing() {
    SubmissionState(
        submissionStateLabel = SubmissionStateLabel.Missing,
        testTag = "missing"
    )
}

@Preview(showBackground = true)
@Composable
private fun SubmissionStatePreview_Late() {
    SubmissionState(
        submissionStateLabel = SubmissionStateLabel.Late,
        testTag = "late"
    )
}

@Preview(showBackground = true)
@Composable
private fun SubmissionStatePreview_Submitted() {
    SubmissionState(
        submissionStateLabel = SubmissionStateLabel.Submitted,
        testTag = "submitted"
    )
}

@Preview(showBackground = true)
@Composable
private fun SubmissionStatePreview_Graded() {
    SubmissionState(
        submissionStateLabel = SubmissionStateLabel.Graded,
        testTag = "graded"
    )
}

@Preview(showBackground = true)
@Composable
private fun SubmissionStatePreview_Excused() {
    SubmissionState(
        submissionStateLabel = SubmissionStateLabel.Excused,
        testTag = "excused"
    )
}

@Preview(showBackground = true)
@Composable
private fun SubmissionStatePreview_Custom() {
    SubmissionState(
        submissionStateLabel = SubmissionStateLabel.Custom(
            iconRes = R.drawable.ic_flag,
            colorRes = R.color.textInfo,
            label = "Custom label"
        ),
        testTag = "custom"
    )
}
