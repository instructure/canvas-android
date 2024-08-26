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
package com.instructure.pandautils.features.inbox.details.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.composables.CanvasAppBar
import com.instructure.pandautils.features.inbox.details.InboxDetailsAction
import com.instructure.pandautils.features.inbox.details.InboxDetailsUiState

@Composable
fun InboxDetailsScreen(
    title: String,
    uiState: InboxDetailsUiState,
    actionHandler: (InboxDetailsAction) -> Unit
) {
    CanvasTheme {
        Scaffold(
            backgroundColor = colorResource(id = R.color.backgroundLightest),
            topBar = {
                CanvasAppBar(
                    title = title,
                    navIconRes = R.drawable.ic_back_arrow,
                    navIconContentDescription = stringResource(id = R.string.contentDescription_back),
                    navigationActionClick = { actionHandler(InboxDetailsAction.CloseFragment) },
                    actions = {
                    },
                )
            },
            content = { padding ->
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    InboxDetailsScreenContent(padding, uiState, actionHandler)
                }
            }
        )
    }
}

@Composable
private fun InboxDetailsScreenContent(
    padding: PaddingValues,
    uiState: InboxDetailsUiState,
    actionHandler: (InboxDetailsAction) -> Unit
) {
    Text(text = "InboxDetailsScreenContent ${uiState.conversationId}")
}