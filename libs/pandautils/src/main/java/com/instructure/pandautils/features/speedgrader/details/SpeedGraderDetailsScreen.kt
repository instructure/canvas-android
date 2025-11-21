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
package com.instructure.pandautils.features.speedgrader.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.EmptyContent
import com.instructure.pandautils.features.speedgrader.details.studentnotes.StudentNotes
import com.instructure.pandautils.features.speedgrader.details.studentnotes.StudentNotesViewModel
import com.instructure.pandautils.features.speedgrader.details.submissiondetails.SubmissionDetails
import com.instructure.pandautils.features.speedgrader.details.submissiondetails.SubmissionDetailsViewModel
import com.instructure.pandautils.utils.ScreenState

@Composable
fun SpeedGraderDetailsScreen() {
    val submissionDetailsViewModel: SubmissionDetailsViewModel = hiltViewModel()
    val submissionDetailsUiState by submissionDetailsViewModel.uiState.collectAsStateWithLifecycle()
    var submissionDetailsExpanded by remember { mutableStateOf(true) }

    val studentNotesViewModel: StudentNotesViewModel = hiltViewModel()
    val studentNotesUiState by studentNotesViewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        if (submissionDetailsUiState.state != ScreenState.Empty) {
            SubmissionDetails(
                uiState = submissionDetailsUiState,
                expanded = submissionDetailsExpanded,
                onExpandToggle = { submissionDetailsExpanded = !submissionDetailsExpanded },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (studentNotesUiState.state != ScreenState.Empty) {
            StudentNotes(
                showTopDivider = submissionDetailsUiState.state != ScreenState.Empty && submissionDetailsExpanded,
                uiState = studentNotesUiState,
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (submissionDetailsUiState.state == ScreenState.Empty &&
            studentNotesUiState.state == ScreenState.Empty
        ) {
            EmptyContent(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp, horizontal = 16.dp),
                emptyMessage = stringResource(R.string.detailsEmptyMessage),
                imageRes = R.drawable.ic_panda_space
            )
        }
    }
}
