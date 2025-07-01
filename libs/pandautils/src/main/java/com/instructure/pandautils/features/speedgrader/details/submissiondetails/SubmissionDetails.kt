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

package com.instructure.pandautils.features.speedgrader.details.submissiondetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.GroupHeader
import com.instructure.pandautils.features.speedgrader.composables.Loading
import com.instructure.pandautils.utils.ScreenState


@Composable
internal fun SubmissionDetails(
    attemptId: Long?,
    modifier: Modifier = Modifier,
    submissionDetailsViewModel: SubmissionDetailsViewModel = hiltViewModel()
) {
    val uiState by submissionDetailsViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = attemptId) {
        uiState.loadSubmissionDetails(attemptId)
    }

    if (uiState.state != ScreenState.Empty) {
        SubmissionDetails(uiState, modifier)
    }
}

@Composable
private fun SubmissionDetails(
    uiState: SubmissionDetailsUiState,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(true) }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.padding(bottom = if (expanded) 16.dp else 0.dp)
    ) {
        GroupHeader(
            name = stringResource(R.string.speedGraderSubmissionDetails),
            expanded = expanded,
            onClick = {
                expanded = !expanded
            }
        )

        if (expanded) {
            when (uiState.state) {
                ScreenState.Loading -> {
                    Loading(stringResource(R.string.speedGraderSubmissionDetilsLoading))
                }

                ScreenState.Content -> {
                    WordCount(
                        wordCount = uiState.wordCount,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                else -> {}
            }
        }
    }
}

@Composable
private fun WordCount(
    wordCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.speedGraderWordCount),
            color = colorResource(id = R.color.textDarkest),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "$wordCount",
            color = colorResource(id = R.color.textDarkest),
            fontSize = 14.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SubmissionDetailsLoadingPreview() {
    SubmissionDetails(
        uiState = SubmissionDetailsUiState(
            loadSubmissionDetails = {},
            state = ScreenState.Loading
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun SubmissionDetailsPreview() {
    SubmissionDetails(
        uiState = SubmissionDetailsUiState(
            loadSubmissionDetails = {},
            state = ScreenState.Content,
            wordCount = 312
        )
    )
}
