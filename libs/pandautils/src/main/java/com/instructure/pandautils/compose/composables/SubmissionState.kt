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

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
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
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.drawableId

sealed class SubmissionStateLabel {
    abstract val iconRes: Int
    abstract val colorRes: Int

    data class Predefined(
        @DrawableRes override val iconRes: Int,
        @ColorRes override val colorRes: Int,
        @StringRes val labelRes: Int
    ) : SubmissionStateLabel()

    data class Custom(
        override val iconRes: Int,
        override val colorRes: Int,
        val label: String
    ) : SubmissionStateLabel()

    companion object {
        val NotSubmitted = Predefined(R.drawable.ic_unpublish, R.color.backgroundDark, R.string.notSubmitted)
        val Missing = Predefined(R.drawable.ic_unpublish, R.color.textDanger, R.string.missingSubmissionLabel)
        val Late = Predefined(R.drawable.ic_clock, R.color.textWarning, R.string.lateSubmissionLabel)
        val Submitted = Predefined(R.drawable.ic_complete, R.color.textSuccess, R.string.submitted)
        val Graded = Predefined(R.drawable.ic_complete_solid, R.color.textSuccess, R.string.gradedSubmissionLabel)
        val Excused = Predefined(R.drawable.ic_complete_solid, R.color.textWarning, R.string.gradingStatus_excused)
        val None = Predefined(0, 0, 0)
    }
}


@Composable
fun SubmissionState(
    submissionStateLabel: SubmissionStateLabel,
    testTag: String = "",
    colorOverride: Int? = null,
    fontSize: TextUnit = 14.sp
) {
    if (submissionStateLabel != SubmissionStateLabel.None) {
        val color = colorOverride ?: submissionStateLabel.colorRes
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = submissionStateLabel.iconRes),
                contentDescription = null,
                tint = colorResource(id = color),
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
                color = colorResource(id = color),
                fontSize = fontSize,
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
