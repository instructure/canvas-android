/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.features.dashboard.widget.courseinvitation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.instructure.pandautils.R
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun CourseInvitationsWidget(
    refreshSignal: SharedFlow<Unit>,
    modifier: Modifier = Modifier
) {
    val viewModel: CourseInvitationsViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    CourseInvitationsContent(
        modifier = modifier,
        uiState = uiState
    )
}

@Composable
private fun CourseInvitationsContent(
    modifier: Modifier = Modifier,
    uiState: CourseInvitationsUiState
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            text = "Course Invitations (${uiState.invitations.size})",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = colorResource(R.color.textDarkest)
        )

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            text = when {
                uiState.loading -> "Loading invitations..."
                uiState.error -> "Error loading invitations"
                uiState.invitations.isEmpty() -> "No pending invitations"
                else -> "You have ${uiState.invitations.size} course invitation(s)"
            },
            fontSize = 14.sp,
            color = colorResource(R.color.textDark)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CourseInvitationsContentPreview() {
    CourseInvitationsContent(
        uiState = CourseInvitationsUiState(
            loading = false,
            error = false,
            invitations = emptyList()
        )
    )
}