/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
 *
 */

package com.instructure.pandautils.features.progress.composables

import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.features.progress.ProgressState
import com.instructure.pandautils.features.progress.ProgressUiState
import com.instructure.pandautils.features.progress.ProgressViewModelAction

@Composable
fun ProgressScreen(
    progressUiState: ProgressUiState,
    actionHandler: (ProgressViewModelAction) -> Unit
) {
    CanvasTheme {
        Scaffold(
            backgroundColor = colorResource(id = R.color.backgroundLightest),
            topBar = {
                ProgressTopBar(
                    title = progressUiState.title,
                    buttonText = progressUiState.buttonTitle,
                    actionHandler = actionHandler
                )
            }
        ) { padding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                color = colorResource(id = R.color.backgroundLightest)
            ) {
                ProgressContent(
                    modifier = Modifier.padding(16.dp, 24.dp),
                    progressTitle = progressUiState.progressTitle,
                    progress = progressUiState.progress,
                    note = progressUiState.note,
                    state = progressUiState.state
                )
            }
        }
    }
}

@Composable
fun ProgressTopBar(
    modifier: Modifier = Modifier,
    title: String,
    buttonText: String,
    actionHandler: (ProgressViewModelAction) -> Unit
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { actionHandler(ProgressViewModelAction.Close) }) {
                Icon(Icons.Filled.Close, contentDescription = "Close")
            }
            Text(
                text = title,
                fontSize = 16.sp,
                color = colorResource(id = R.color.textDarkest),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = { actionHandler(ProgressViewModelAction.Cancel) }) {
                Text(text = buttonText, color = colorResource(id = R.color.textDarkest))
            }
        }
        Divider()
    }

}

@Composable
fun ProgressContent(
    modifier: Modifier = Modifier,
    progressTitle: String,
    progress: Long,
    note: String?,
    state: ProgressState
) {
    Column {
        ProgressIndicator(modifier = modifier, progressTitle = progressTitle, progress = progress, state = state)
        Divider()
        Text(
            text = stringResource(R.string.progressMessage),
            modifier = Modifier.padding(16.dp),
            color = colorResource(id = R.color.textDarkest)
        )
        note?.let {
            Note(note = it)
        }
    }
}

@Composable
fun Note(note: String) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = stringResource(id = R.string.noteTitle),
            color = colorResource(id = R.color.textDark),
            fontSize = 14.sp
        )
        Text(
            modifier = Modifier.padding(top = 4.dp),
            text = note,
            color = colorResource(id = R.color.textDarkest),
            fontSize = 16.sp
        )

    }
}

@Composable
fun ProgressIndicator(modifier: Modifier = Modifier, progressTitle: String, progress: Long, state: ProgressState) {
    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Text(
            text = "$progressTitle $progress%",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            fontSize = 14.sp,
            color = colorResource(id = R.color.textDarkest)
        )
        LinearProgressIndicator(
            progress = progress / 100f,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            color = if (state == ProgressState.COMPLETED) {
                colorResource(id = R.color.backgroundSuccess)
            } else {
                colorResource(id = R.color.backgroundInfo)
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProgressScreenPreview() {
    ProgressScreen(
        progressUiState = ProgressUiState(
            title = "All modules and items",
            progressTitle = "Publishing",
            progress = 40L,
            note = "Modules and items that have already been processed will not be reverted to their previous state when the process is discontinued.",
            buttonTitle = "Cancel",
            state = ProgressState.RUNNING
        ),
        actionHandler = {}
    )
}