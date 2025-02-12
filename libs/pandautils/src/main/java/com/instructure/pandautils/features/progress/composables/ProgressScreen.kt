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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.models.CanvasTheme
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.features.progress.ProgressState
import com.instructure.pandautils.features.progress.ProgressUiState
import com.instructure.pandautils.features.progress.ProgressAction
import kotlin.math.roundToInt

@Composable
fun ProgressScreen(
    progressUiState: ProgressUiState,
    actionHandler: (ProgressAction) -> Unit
) {
    CanvasTheme {
        Scaffold(
            modifier = Modifier.clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
            containerColor = colorResource(id = R.color.backgroundLightest),
            topBar = {
                ProgressTopBar(
                    title = progressUiState.title,
                    state = progressUiState.state,
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
    state: ProgressState,
    actionHandler: (ProgressAction) -> Unit
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { actionHandler(ProgressAction.Close) }) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = stringResource(id = R.string.a11y_closeProgress),
                    tint = colorResource(id = R.color.textDarkest)
                )
            }
            Text(
                text = title,
                fontSize = 16.sp,
                color = colorResource(id = R.color.textDarkest),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.weight(1f))
            if (state == ProgressState.COMPLETED || state == ProgressState.FAILED) {
                TextButton(
                    modifier = Modifier.padding(end = 12.dp),
                    onClick = { actionHandler(ProgressAction.Close) }) {
                    Text(text = stringResource(id = R.string.done), color = colorResource(id = R.color.textDarkest))
                }
            } else {
                TextButton(
                    modifier = Modifier.padding(end = 12.dp),
                    onClick = { actionHandler(ProgressAction.Cancel) }) {
                    Text(text = stringResource(id = R.string.cancel), color = colorResource(id = R.color.textDarkest))
                }
            }
        }
        Divider(color = colorResource(id = R.color.backgroundMedium))
    }

}

@Composable
fun ProgressContent(
    modifier: Modifier = Modifier,
    progressTitle: String,
    progress: Float,
    note: String?,
    state: ProgressState
) {
    Column(modifier = modifier) {
        ProgressIndicator(
            modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp),
            progressTitle = progressTitle,
            progress = progress,
            state = state
        )
        Divider(color = colorResource(id = R.color.backgroundMedium))
        Text(
            text = stringResource(R.string.progressMessage),
            modifier = Modifier.padding(16.dp),
            color = colorResource(id = R.color.textDarkest)
        )
        note?.let {
            Note(modifier = Modifier.padding(16.dp), note = it)
        }
    }
}

@Composable
fun Note(modifier: Modifier = Modifier, note: String) {
    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
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
fun ProgressIndicator(modifier: Modifier = Modifier, progressTitle: String, progress: Float, state: ProgressState) {
    val title = when (state) {
        ProgressState.COMPLETED -> stringResource(id = R.string.success)
        ProgressState.FAILED -> stringResource(id = R.string.updateFailed)
        else -> "$progressTitle ${progress.roundToInt()}%"
    }
    val progressColor = when (state) {
        ProgressState.COMPLETED -> colorResource(id = R.color.backgroundSuccess)
        ProgressState.FAILED -> colorResource(id = R.color.backgroundDanger)
        else -> colorResource(id = R.color.backgroundInfo)
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Text(
            text = title,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            fontSize = 14.sp,
            color = colorResource(id = R.color.textDarkest)
        )
        LinearProgressIndicator(
            progress = progress / 100f,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            color = progressColor,
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
            progress = 40f,
            note = "Modules and items that have already been processed will not be reverted to their previous state when the process is discontinued.",
            state = ProgressState.RUNNING
        ),
        actionHandler = {}
    )
}